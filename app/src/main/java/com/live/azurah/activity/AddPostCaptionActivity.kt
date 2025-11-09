package com.live.azurah.activity

import HashtagAdapter
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.adapter.AddPostImageAdapter
import com.live.azurah.databinding.ActivityAddPostCaptionBinding
import com.live.azurah.databinding.LayoutPlayerBinding
import com.live.azurah.fragment.HashTagFragment
import com.live.azurah.model.BlockResposne
import com.live.azurah.model.FullImageModel
import com.live.azurah.model.HashTagResponse
import com.live.azurah.model.Hashtag
import com.live.azurah.model.ImageVideoModel
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.ShowImagesDialogFragment
import com.live.azurah.util.containsBannedWord
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddPostCaptionActivity : AppCompatActivity(), AddPostImageAdapter.ClickListener {
    private lateinit var binding: ActivityAddPostCaptionBinding
    private var list = ArrayList<ImageVideoModel>()
    private lateinit var adapter: AddPostImageAdapter
    private lateinit var hashtagAdapter: HashtagAdapter
    private var player: ExoPlayer? = null
    var hashTagList = ArrayList<HashTagResponse.Body.Data>()
    private val viewModel by viewModels<CommonViewModel>()
    private var hashtagJob: Job? = null
    private var isInsertingHashtag = false // Flag to prevent reopening after selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostCaptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.white)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.updatePadding(
                left = systemBars.left,
                bottom = systemBars.bottom,
                right = systemBars.right,
                top = systemBars.top
            )
            insets
        }

        list = intent.getSerializableExtra("images") as ArrayList<ImageVideoModel>
        setupHashtagAdapter()
        setAdapter()
        initListener()
    }

    private fun setAdapter() {
        adapter = AddPostImageAdapter(this, list, this, 1)
        binding.rvPostImages.adapter = adapter

        adapter.zoomImageListener = { pos ->
            if (list[pos].type == "2") {
                playVideo(list[pos].video.toString())
            } else {
                val imageList = list.filter { it.type == "1" }
                    .map { FullImageModel(image = it.image, type = 0) } as ArrayList
                val fullImageDialog = ShowImagesDialogFragment.newInstance(imageList, pos)
                fullImageDialog.show(supportFragmentManager, "FullImageDialog")
            }
        }
    }

    private fun initListener() {
        with(binding) {
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            tvSeeMore.setOnClickListener {
                replaceFragment(HashTagFragment())
            }

            btnPost.setOnClickListener {
                if (containsBannedWord(binding.etDes.text.toString().trim())) {
                    showCustomSnackbar(
                        this@AddPostCaptionActivity,
                        it,
                        "Your post contains banned or inappropriate words. Please remove them before posting."
                    )
                    return@setOnClickListener
                }

                // Combine description and hashtags
                val description = binding.etDes.text.toString().trim()
                val hashtags = binding.etDes1.text.toString().trim()

                val combinedText = if (hashtags.isNotEmpty()) {
                    if (description.isNotEmpty()) {
                        "$description\n$hashtags"
                    } else {
                        hashtags
                    }
                } else {
                    description
                }

                startActivity(
                    Intent(
                        this@AddPostCaptionActivity,
                        PreviewPostActivity::class.java
                    ).apply {
                        putExtra("images", list)
                        putExtra("desc", combinedText)
                    })
            }

            etDes.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    updatePostButtonState()
                    updateWordCount(s)
                }
            })

            etDes1.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // If we're inserting a hashtag programmatically, skip processing
                    if (isInsertingHashtag) return

                    val text = s.toString()

                    // Prevent text entry without # at the beginning of each hashtag
                    if (count == 1 && s?.get(start) != '#' && s?.get(start) != ' ') {
                        // Check if we're at the start or after a space (new hashtag position)
                        if (start == 0 || (start > 0 && text[start - 1] == ' ')) {
                            // User is trying to type text without # at hashtag position
                            binding.etDes1.removeTextChangedListener(this)
                            val beforeText = text.substring(0, start)
                            val afterText = text.substring(start + 1)
                            binding.etDes1.setText(beforeText + afterText)
                            binding.etDes1.setSelection(start)
                            binding.etDes1.addTextChangedListener(this)
                            return
                        }
                    }

                    // Prevent spaces within hashtag words (between # and space)
                    if (count == 1 && s?.get(start) == ' ') { // User typed a space
                        // Find the last # before current position
                        val lastHashIndex = text.substring(0, start).lastIndexOf('#')
                        if (lastHashIndex != -1) {
                            // Check if there's already a space after this hashtag
                            val spaceAfterThisHashtag = text.indexOf(' ', lastHashIndex)
                            if (spaceAfterThisHashtag == -1 || spaceAfterThisHashtag > start) {
                                // We're typing within a hashtag word, prevent the space
                                val beforeText = text.substring(0, start)
                                val afterText = text.substring(start + 1)
                                binding.etDes1.removeTextChangedListener(this)
                                binding.etDes1.setText(beforeText + afterText)
                                binding.etDes1.setSelection(start)
                                binding.etDes1.addTextChangedListener(this)
                                return
                            }
                        }
                    }

                    // Regular hashtag search logic
                    if (text.contains("#") && text.isNotEmpty()) {
                        val lastHashSymbol = text.lastIndexOf('#')
                        // Find the next space after the last hashtag to get the current hashtag word
                        val spaceAfterHashtag = text.indexOf(' ', lastHashSymbol)
                        val endOfCurrentHashtag = if (spaceAfterHashtag != -1) spaceAfterHashtag else text.length

                        val searchQuery = text.substring(lastHashSymbol + 1, endOfCurrentHashtag)

                        if (searchQuery.isNotEmpty() && searchQuery.length >= 1) {
                            searchHashtagsWithDebounce(searchQuery)
                        } else {
                            binding.rvHashTag.visibility = View.GONE
                            hashtagJob?.cancel()
                        }
                    } else {
                        binding.rvHashTag.visibility = View.GONE
                        hashtagJob?.cancel()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun searchHashtagsWithDebounce(searchQuery: String) {
        hashtagJob?.cancel()
        hashtagJob = kotlinx.coroutines.MainScope().launch {
            delay(500)

            val map = HashMap<String, String>()
            map["limit"] = "20"
            map["page"] = "1"
            map["search_string"] = searchQuery

            viewModel.getHashTagList(map, this@AddPostCaptionActivity).observe(this@AddPostCaptionActivity){resource->
                when (resource.status) {
                    Status.LOADING -> {
                    }
                    Status.SUCCESS -> {
                        when(resource.data){
                            is HashTagResponse -> {
                                val res = resource.data.body?.data ?: ArrayList()
                                hashTagList = res as ArrayList

                                if (res.isNotEmpty()){
                                    val limitedList = if (res.size > 3) {
                                        binding.tvSeeMore.visible()
                                        res.subList(0, 3)
                                    } else {
                                        binding.tvSeeMore.gone()
                                        res
                                    }
                                    hashtagAdapter.updateList(limitedList)
                                    binding.rvHashTag.visibility = View.VISIBLE
                                } else {
                                    binding.tvSeeMore.gone()
                                    binding.rvHashTag.visibility = View.GONE
                                }
                            }
                        }
                    }
                    Status.ERROR -> {
                        binding.rvHashTag.visibility = View.GONE
                        // Don't show error snackbar for hashtag search
                    }
                }
            }
        }
    }

    private fun setupHashtagAdapter() {
        hashtagAdapter = HashtagAdapter(ArrayList()) { hashtag ->
            insertHashtagIntoEditText(hashtag.name ?: "")
        }

        binding.rvHashTag.adapter = hashtagAdapter
        binding.rvHashTag.visibility = View.GONE
    }

    fun insertHashtagIntoEditText(tag: String) {
        isInsertingHashtag = true

        val currentText = binding.etDes1.text.toString()
        val lastHashSymbol = currentText.lastIndexOf('#')

        if (lastHashSymbol != -1) {
            // Find if there's already text after the last hashtag
            val spaceAfterLastHashtag = currentText.indexOf(' ', lastHashSymbol)
            if (spaceAfterLastHashtag != -1) {
                // Replace the text between # and space with the selected hashtag
                val newText = currentText.substring(0, lastHashSymbol) + "#$tag " + currentText.substring(spaceAfterLastHashtag + 1)
                binding.etDes1.setText(newText)
                binding.etDes1.setSelection(lastHashSymbol + tag.length + 2) // Position after "#tag "
            } else {
                // No space after hashtag, just replace everything after #
                val newText = currentText.substring(0, lastHashSymbol) + "#$tag "
                binding.etDes1.setText(newText)
                binding.etDes1.setSelection(newText.length)
            }
        } else {
            // If no # found, just append the hashtag
            val newText = if (currentText.isEmpty()) {
                "#$tag "
            } else {
                // Check if the last character is a space, if not add one before the new hashtag
                if (currentText.last() == ' ') {
                    "$currentText#$tag "
                } else {
                    "$currentText #$tag "
                }
            }
            binding.etDes1.setText(newText)
            binding.etDes1.setSelection(newText.length)
        }

        // Hide the hashtag list after selection and reset flag after a delay
        binding.rvHashTag.visibility = View.GONE
        binding.tvSeeMore.gone()

        // Reset the flag after text change is complete
        binding.etDes1.post {
            isInsertingHashtag = false
        }
    }

    private fun updatePostButtonState() {
        if (binding.etDes.text.toString().isNotEmpty() && list.isNotEmpty()) {
            binding.btnPost.backgroundTintList = getColorStateList(R.color.blue)
            binding.btnPost.isEnabled = true
        } else {
            binding.btnPost.backgroundTintList = getColorStateList(R.color.button_grey)
            binding.btnPost.isEnabled = false
        }
    }

    private fun updateWordCount(s: Editable?) {
        if (s.toString().trim().isNotEmpty()) {
            val wordCount = countWords(s.toString())
            binding.tvWords.text = (50 - wordCount).toString() + " words"
            if (wordCount > 50) {
                val trimmedText = trimToWordLimit(s.toString(), 50)
                binding.etDes.setText(trimmedText)
                binding.etDes.setSelection(trimmedText.length)
            }
        } else {
            binding.tvWords.text = "50 words"
        }
    }

    override fun onClick(position: Int) {
        // Handle image click if needed
    }

    private fun countWords(text: String): Int {
        val words = text.trim().split("\\s+".toRegex())
        return words.size
    }

    private fun trimToWordLimit(text: String, limit: Int): String {
        val words = text.trim().split("\\s+".toRegex())
        return words.take(limit).joinToString(" ")
    }

    private fun playVideo(video: String) {
        val dialog = Dialog(this, R.style.Theme_Dialog)
        val videoBinding = LayoutPlayerBinding.inflate(layoutInflater)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(videoBinding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        window!!.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT
        )
        window.setGravity(Gravity.CENTER)
        videoBinding.ivClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            releasePlayer()
        }

        startPlayer(videoBinding, video)
        dialog.show()
    }

    private fun startPlayer(binding: LayoutPlayerBinding, url: String) {
        val playerView = binding.playerView
        player = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(url)
        player?.setMediaItem(mediaItem)
        playerView.player = player
        player!!.prepare()
        player!!.repeatMode = Player.REPEAT_MODE_OFF
        player!!.playWhenReady = true
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    private fun pausePlayer() {
        player?.pause()
    }

    private fun resumePlayer() {
        player?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        hashtagJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        resumePlayer()
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(binding.mainContainer.id, fragment)
            .addToBackStack(null).commit()
    }
}