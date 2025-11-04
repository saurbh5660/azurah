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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.adapter.AddPostImageAdapter
import com.live.azurah.databinding.ActivityAddPostCaptionBinding
import com.live.azurah.databinding.LayoutPlayerBinding
import com.live.azurah.model.BlockResposne
import com.live.azurah.model.FullImageModel
import com.live.azurah.model.HashTagResponse
import com.live.azurah.model.Hashtag
import com.live.azurah.model.ImageVideoModel
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.ShowImagesDialogFragment
import com.live.azurah.util.containsBannedWord
import com.live.azurah.util.showCustomSnackbar
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
    private val viewModel by viewModels<CommonViewModel>()
    private var hashtagJob: Job? = null

    // Pagination variables
    private var currentPage = 1
    private var isLoading = false
    private var hasMore = true
    private var currentSearchQuery = ""

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
        observeHashtagList()
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

            btnPost.setOnClickListener {
                if (containsBannedWord(binding.etDes.text.toString().trim())) {
                    showCustomSnackbar(
                        this@AddPostCaptionActivity,
                        it,
                        "Your post contains banned or inappropriate words. Please remove them before posting."
                    )
                    return@setOnClickListener
                }
                startActivity(
                    Intent(
                        this@AddPostCaptionActivity,
                        PreviewPostActivity::class.java
                    ).apply {
                        putExtra("images", list)
                        putExtra("desc", binding.etDes.text.toString())
                        putExtra("hashtags", binding.etDes1.text.toString())
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
                    // Check if user typed # symbol
                    val text = s.toString()
                    if (text.contains("#") && text.isNotEmpty()) {
                        val lastHashSymbol = text.lastIndexOf('#')
                        val searchQuery = text.substring(lastHashSymbol + 1)

                        if (searchQuery.isNotEmpty() && searchQuery.length >= 1) {
                            // Reset pagination for new search
                            if (currentSearchQuery != searchQuery) {
                                resetPagination()
                                currentSearchQuery = searchQuery
                            }
                            searchHashtagsWithDebounce(searchQuery, false)
                        } else {
                            binding.rvHashTag.visibility = View.GONE
                            resetPagination()
                        }
                    } else {
                        binding.rvHashTag.visibility = View.GONE
                        resetPagination()
                        hashtagJob?.cancel()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun resetPagination() {
        currentPage = 1
        hasMore = true
        isLoading = false
        hashtagAdapter.clearData()
    }

    private fun searchHashtagsWithDebounce(searchQuery: String, loadMore: Boolean = false) {
        hashtagJob?.cancel()

        hashtagJob = kotlinx.coroutines.MainScope().launch {
            if (!loadMore) {
                delay(500) // 500ms debounce delay only for new searches
            }

            if (isLoading) return@launch

            isLoading = true
            val map = HashMap<String, String>()
            map["limit"] = "10"
            map["page"] = currentPage.toString()
            map["search_string"] = searchQuery

            viewModel.getHashTagList(map, this@AddPostCaptionActivity).observe(this@AddPostCaptionActivity){resource->
                when (resource.status) {
                    Status.LOADING -> {
                        if (currentPage == 1) {
                            // Show loading indicator for initial load if needed
                        }
                    }
                    Status.SUCCESS -> {
                        isLoading = false

                        when(resource.data){
                            is HashTagResponse -> {
                                val res = resource.data.body?.data ?: ArrayList()
                                if (res.isNotEmpty()){
                                    if (currentPage == 1) {
                                        hashtagAdapter.updateList(res)
                                    } else {
                                        hashtagAdapter.addData(res)
                                    }

                                    hasMore = res.size >= 10
                                    if (hasMore) {
                                        currentPage++
                                    }

                                    binding.rvHashTag.visibility = View.VISIBLE
                                }else{
                                    if (currentPage == 1) {
                                        binding.rvHashTag.visibility = View.GONE
                                    }
                                    hasMore = false
                                }

                            }
                        }
                    }
                    Status.ERROR -> {
                        isLoading = false
                        if (currentPage == 1) {
                            binding.rvHashTag.visibility = View.GONE
                        }
                        // Don't show error snackbar for hashtag search
                    }
                }
            }
        }
    }

    private fun observeHashtagList() {

    }

    private fun setupHashtagAdapter() {
        hashtagAdapter = HashtagAdapter(ArrayList()) { hashtag ->
            insertHashtagIntoEditText(hashtag.name ?: "")
        }

        binding.rvHashTag.adapter = hashtagAdapter
        binding.rvHashTag.visibility = View.GONE

        // Add scroll listener for pagination
        binding.rvHashTag.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && hasMore) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= 10) { // Only load more if we have enough items
                        loadMoreHashtags()
                    }
                }
            }
        })
    }

    private fun loadMoreHashtags() {
        if (currentSearchQuery.isNotEmpty() && !isLoading && hasMore) {
            searchHashtagsWithDebounce(currentSearchQuery, true)
        }
    }

    private fun insertHashtagIntoEditText(tag: String) {
        val currentText = binding.etDes1.text.toString()
        val lastHashSymbol = currentText.lastIndexOf('#')

        if (lastHashSymbol != -1) {
            // Replace the text after # with the selected hashtag
            val newText = currentText.substring(0, lastHashSymbol) + "#$tag "
            binding.etDes1.setText(newText)
            binding.etDes1.setSelection(newText.length)
        } else {
            // If no # found, just append the hashtag
            val newText = if (currentText.isEmpty()) {
                "#$tag "
            } else {
                "$currentText #$tag "
            }
            binding.etDes1.setText(newText)
            binding.etDes1.setSelection(newText.length)
        }

        // Hide the hashtag list after selection
        binding.rvHashTag.visibility = View.GONE
        resetPagination()
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
}