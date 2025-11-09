package com.live.azurah.activity

import android.app.Dialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.adapter.CommentAdapter
import com.live.azurah.adapter.Follower
import com.live.azurah.adapter.MentionAdapter
import com.live.azurah.databinding.ActivityQuestDetailBinding
import com.live.azurah.databinding.DialogEditMessageBinding
import com.live.azurah.databinding.MenuReportBinding
import com.live.azurah.databinding.MenuReportCommentBinding
import com.live.azurah.databinding.MenuReportDeleteDialogBinding
import com.live.azurah.databinding.MenuReportMemberBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.fragment.UserLikesFragment
import com.live.azurah.model.CommentCommonResponse
import com.live.azurah.model.CommentResponse
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.DetailResponse
import com.live.azurah.model.FollowFollowingResponse
import com.live.azurah.model.PostCommentListResposne
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.containsBannedWord
import com.live.azurah.util.formatCount
import com.live.azurah.util.getPreference
import com.live.azurah.util.getRelativeTime
import com.live.azurah.util.gone
import com.live.azurah.util.isInternetAvailable
import com.live.azurah.util.loadImage
import com.live.azurah.util.removeMentionIfMatches
import com.live.azurah.util.setColoredUsername
import com.live.azurah.util.setupSeeMoreText
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.showKeyboard
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuestDetailActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityQuestDetailBinding

    private var from = ""
    private var id = ""
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var data = DetailResponse.Body()
    private var commentAdapter: CommentAdapter? = null
    private val commentList = ArrayList<CommentResponse>()
    private var parentId = 0
    private var taggedId = 0
    private var mainPos = -1
    private var username = ""
    private var isApiRunning = false
    private var showDialog = true
    private var currentCommentPage = 1
    private var totalCommentPageCount = 0
    private var resetCommentPage = false
    private var mentionAdapter: MentionAdapter? = null
    private var mentionList = mutableListOf<Follower>()
    private val dummyFollowers = ArrayList<Follower>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestDetailBinding.inflate(layoutInflater)
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

        from = intent.getStringExtra("from") ?: ""
        id = intent.getStringExtra("id") ?: ""
        initListener()
        when (from) {
            "prayer" -> {
                getPrayerDetail()
            }

            "testimony" -> {
                getTestimonyDetail()
            }

            else -> {
                getCommunityDetail()
            }
        }
        setCommentAdapter()
    }


    private fun getPrayerDetail() {
        viewModel.prayerView(id, this).observe(this, this)
    }

    private fun getTestimonyDetail() {
        viewModel.testimonyView(id, this).observe(this, this)
    }

    private fun getCommunityDetail() {
        viewModel.communityView(id, this).observe(this, this)
    }

    private fun initListener() {
        with(binding) {
            if (from == "prayer" || from == "testimony") {
                tvPrayers.visibility = View.VISIBLE
                dotPrayer.visibility = View.VISIBLE
                ivPrayer.visibility = View.VISIBLE
            } else {
                tvPrayers.visibility = View.GONE
                dotPrayer.visibility = View.GONE
                ivPrayer.visibility = View.GONE

                val marginInPx = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._16sdp)
                val layoutParams = tvComments.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.marginEnd = marginInPx
                tvComments.layoutParams = layoutParams
                val textInPx = resources.getDimension(com.intuit.sdp.R.dimen._11sdp)
                tvComments.textSize = textInPx / resources.displayMetrics.scaledDensity
                tvLikes.textSize = textInPx / resources.displayMetrics.scaledDensity
            }

            tvCat.setOnClickListener {
                setCategoryWindow(it, tvCat.text.toString())

            }
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            tvPrayers.setOnClickListener {
              val text = when (from) {
                    "prayer" -> {
                       "prayer_praise"
                    }

                    "testimony" -> {
                        "testimony_praise"
                    }

                  else -> {
                      ""
                  }
              }
                val bundle = Bundle()
                bundle.putString("postId", id)
                bundle.putString("from", text)
                val fragment = UserLikesFragment()
                fragment.arguments = bundle
                replaceFragment(fragment)
            }

            ivPrayer.setOnClickListener {
                if (isInternetAvailable(this@QuestDetailActivity)) {
                    if (data.is_praise == 1) {
                        data.is_praise = 0
                        data.praiseCount = (data.praiseCount ?: 0).minus(1)
                        ivPrayer.imageTintList =
                            ContextCompat.getColorStateList(this@QuestDetailActivity, R.color.black)

                    } else {
                        data.is_praise = 1
                        data.praiseCount = (data.praiseCount ?: 0).plus(1)
                        ivPrayer.imageTintList = ContextCompat.getColorStateList(
                            this@QuestDetailActivity,
                            R.color.golden_yellow
                        )

                    }
                    val text = if ((data.praiseCount ?: 0) > 1 || (data.praiseCount
                            ?: 0) == 0
                    ) "Prayers" else "Prayer"
                    tvPrayers.text = buildString {
                        append(formatCount(data.praiseCount ?: 0))
                        append(" ")
                        append(text)
                    }

                    val map = HashMap<String, String>()
                    map["status"] = data.is_praise.toString()

                    if (from == "prayer") {
                        map["prayer_id"] = data.id.toString()
                        viewModel.prayerPriseUnpraise(map, this@QuestDetailActivity)
                            .observe(this@QuestDetailActivity) {

                            }
                    } else {
                        map["testimony_id"] = data.id.toString()
                        viewModel.testimonyPriseUnpraise(map, this@QuestDetailActivity)
                            .observe(this@QuestDetailActivity) {

                            }
                    }
                }

            }

            ivLike.setOnClickListener {
                if (data.isLike == 0) {
                    data.isLike = 1
                    data.likeCount = (data.likeCount ?: 0).plus(1)
                    ivLike.setImageResource(R.drawable.selected_heart)
                    ivLike.imageTintList = ContextCompat.getColorStateList(
                        this@QuestDetailActivity,
                        R.color.star_red_color
                    )

                } else {
                    data.isLike = 0
                    data.likeCount = (data.likeCount ?: 0).minus(1)
                    ivLike.setImageResource(R.drawable.unselected_heart)
                    ivLike.imageTintList =
                        ContextCompat.getColorStateList(this@QuestDetailActivity, R.color.black)
                }

                val text =
                    if ((data.likeCount ?: 0) > 1 || (data.likeCount ?: 0) == 0) "Likes" else "Like"
                tvLikes.text = buildString {
                    append(formatCount(data.likeCount ?: 0))
                    append(" ")
                    append(text)
                }

                val map = HashMap<String, String>()
                if (data.isLike == 1) {
                    map["status"] = "0"
                } else {

                    map["status"] = "1"
                }
                if (from == "prayer") {
                    map["prayer_id"] = data.id.toString()
                    viewModel.prayerLikeUnlike(map, this@QuestDetailActivity)
                        .observe(this@QuestDetailActivity) {
                        }
                } else if (from == "testimony") {
                    map["testimony_id"] = data.id.toString()
                    viewModel.testimonyLikeUnlike(map, this@QuestDetailActivity)
                        .observe(this@QuestDetailActivity) {

                        }
                } else {
                    map["community_forum_id"] = data.id.toString()
                    viewModel.communityLikeUnlike(map, this@QuestDetailActivity)
                        .observe(this@QuestDetailActivity) {

                        }
                }
            }

            tvLikes.setOnClickListener {
                if ((data.likeCount ?: 0) > 0) {
                    val bundle = Bundle()
                    bundle.putString("postId", data.id.toString())
                    if (from == "prayer") {
                        bundle.putString("from", "prayer")
                    } else if (from == "testimony") {
                        bundle.putString("from", "testimony")
                    } else {
                        bundle.putString("from", "community")
                    }
                    val fragment = UserLikesFragment()
                    fragment.arguments = bundle
                    replaceFragment(fragment)
                }
            }

            ivMore.setOnClickListener {
                setPopUpWindow(it)
            }
            ivPosts.setOnClickListener {
                if (getPreference("id", "") != this@QuestDetailActivity.data.user?.id.toString()) {
                    startActivity(
                        Intent(
                            this@QuestDetailActivity,
                            OtherUserProfileActivity::class.java
                        ).apply {
                            putExtra("user_id", this@QuestDetailActivity.data.user?.id.toString())
                        })
                }

            }

            tvName.setOnClickListener {
                if (getPreference("id", "") != this@QuestDetailActivity.data.user?.id.toString()) {
                    startActivity(
                        Intent(
                            this@QuestDetailActivity,
                            OtherUserProfileActivity::class.java
                        ).apply {
                            putExtra("user_id", this@QuestDetailActivity.data.user?.id.toString())
                        })
                }
            }
            etMessage.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().isNotEmpty()) {
                        ivSend.backgroundTintList =
                            ContextCompat.getColorStateList(this@QuestDetailActivity, R.color.blue)
                        ivSend.isEnabled = true
                    } else {
                        ivSend.backgroundTintList = ContextCompat.getColorStateList(
                            this@QuestDetailActivity,
                            R.color.button_grey
                        )
                        ivSend.isEnabled = false
                    }
                }
            })
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(binding.container.id, fragment)
            .addToBackStack(null).commit()
    }

    /*    private fun setCommentAdapter() {
            val adapter = CommentAdapter(this,ArrayList())
            binding.rvComments.adapter = adapter

            adapter.menuListener = {pos, view,model,item ->
                setPopUpComment(view)
            }
        }*/

    private fun setPopUpWindow(view1: View) {
        val view = MenuReportBinding.inflate(layoutInflater)

        val myPopupWindow = PopupWindow(
            view.root,
            500,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        with(view) {
            tvHaveDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(
                    Intent(
                        this@QuestDetailActivity,
                        ReportPostActivity::class.java
                    ).apply {
                        putExtra("from", from)
                        putExtra("id", this@QuestDetailActivity.data.id.toString())
                        putExtra("reportedTo", this@QuestDetailActivity.data.userId.toString())
                    })
            }

            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(
                    Intent(
                        this@QuestDetailActivity,
                        ReportUserActivity::class.java
                    ).apply {
                        putExtra("from", from)
                        putExtra("id", this@QuestDetailActivity.data.id.toString())
                        putExtra(
                            "username",
                            this@QuestDetailActivity.data.user?.username.toString()
                        )

                    })
            }

            tvBlockUser.setOnClickListener {
                myPopupWindow.dismiss()
                sureDialog()
            }
        }

        myPopupWindow.showAsDropDown(view1, 0, 6)
    }

    private fun showMessageDialog() {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val resetBinding = DialogEditMessageBinding.inflate(layoutInflater)
        customDialog.setContentView(resetBinding.root)
        customDialog.window?.setGravity(Gravity.BOTTOM)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        resetBinding.tvComments.setOnClickListener {
            customDialog.dismiss()
        }

        resetBinding.tvReplies.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()

        resetBinding.tvDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    resetBinding.tvReplies.backgroundTintList =
                        ContextCompat.getColorStateList(this@QuestDetailActivity, R.color.blue)
                    resetBinding.tvReplies.isEnabled = true
                } else {
                    resetBinding.tvReplies.backgroundTintList = ContextCompat.getColorStateList(
                        this@QuestDetailActivity,
                        R.color.button_grey
                    )
                    resetBinding.tvReplies.isEnabled = false
                }
            }

        })

    }


    private fun sureDialog() {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = SureDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmationBinding.tvMsg.text =
            "Blocking this user will prevent you both from viewing each other’s posts and sending each other messages."
        confirmationBinding.tvUsernameTaken.text = buildString {
            append("Are you sure you want to block ")
            append("@")
            append(data.user?.username)
            append("?")
        }
        confirmationBinding.ivDel.visibility = View.GONE
        confirmationBinding.ivDel.setImageResource(R.drawable.block_user_icon)

        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            val map = HashMap<String, String>()
            map["block_by"] = getPreference("id", "")
            map["block_to"] = data.user?.id.toString()
            map["status"] = "1"
            viewModel.userBlock(map, this).observe(this) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                        LoaderDialog.dismiss()
                        when (value.data) {
                            is CommonResponse -> {
                                val res = value.data.body
                                showCustomSnackbar(this, binding.root, "User Blocked Successfully!")
                            }
                        }
                    }

                    Status.LOADING -> {
                        LoaderDialog.show(this)
                    }

                    Status.ERROR -> {
                        LoaderDialog.dismiss()
                        showCustomSnackbar(this, binding.root, value.message.toString())
                    }
                }
            }
        }
        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    private fun setPopUpComment(view1: View) {
        val view = MenuReportCommentBinding.inflate(layoutInflater)

        val myPopupWindow = PopupWindow(
            view.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        with(view) {
            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(
                    Intent(
                        this@QuestDetailActivity,
                        ReportUserActivity::class.java
                    ).apply {
                        putExtra("from", "1")
                    })
            }
            ivUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(
                    Intent(
                        this@QuestDetailActivity,
                        ReportUserActivity::class.java
                    ).apply {
                        putExtra("from", "1")
                    })
            }

        }

        myPopupWindow.showAsDropDown(view1, 0, -40)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                LoaderDialog.dismiss()
                binding.nvScrollView.visible()
                when (value.data) {
                    is DetailResponse -> {
                        val item = value.data.body
                        item?.let {
                            data = it
                        }
                        with(binding) {
                            clDetail.visible()

                            tvName.text = buildString {
                                append(item?.user?.username ?: "")
                            }

                            tvTitle.text = buildString {
                                append(item?.title ?: "")
                            }

                            when (from) {
                                "testimony" -> {
                                    if (item?.testimonyCategory == null) {
                                        tvCat.gone()
                                    }
                                }

                                "prayer" -> {
                                    if (item?.prayerCategory == null) {
                                        tvCat.gone()
                                    }
                                }

                                else -> {
                                    if (item?.category == null) {
                                        tvCat.gone()
                                    }
                                }
                            }

                            tvCat.text = buildString {
                                if (item?.prayerCategory != null) {
                                    append(item.prayerCategory.name ?: "")
                                } else if (item?.testimonyCategory != null) {
                                    append(item.testimonyCategory.name ?: "")
                                } else {
                                    append(item?.category?.name ?: "")
                                }
                            }

                            if (getPreference("id", "") == item?.user?.id.toString()) {
                                ivMore.gone()
                                Log.d("fsdfsdf", "sdfsfsf")
                                val marginInPx =
                                    resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._16sdp)
                                val layoutParams =
                                    tvCat.layoutParams as ViewGroup.MarginLayoutParams
                                layoutParams.marginEnd = marginInPx
                                tvCat.layoutParams = layoutParams
                            } else {
                                ivMore.visible()
                            }

                            val likeText = if ((item?.likeCount ?: 0) > 1 || (item?.likeCount
                                    ?: 0) == 0
                            ) "Likes" else "Like"
                            tvLikes.text = buildString {
                                append(formatCount(item?.likeCount ?: 0))
                                append(" ")
                                append(likeText)
                            }

                            val commentText =
                                if ((item?.commentCount ?: 0) > 1 || (item?.commentCount
                                        ?: 0) == 0
                                ) "Comments" else "Comment"
                            tvComments.text = buildString {
                                append(formatCount(item?.commentCount ?: 0))
                                append(" ")
                                append(commentText)
                            }

                            if ((item?.commentCount ?: 0) > 0) {
                                tvTotalComments.text = buildString {
                                    append("Comments (")
                                    append(formatCount((item?.commentCount ?: 0)))
                                    append(")")
                                }
                            }

                            val prayerText = if ((item?.praiseCount ?: 0) > 1 || (item?.praiseCount
                                    ?: 0) == 0
                            ) "Prayers" else "Prayer"
                            tvPrayers.text = buildString {
                                append(formatCount(item?.praiseCount ?: 0))
                                append(" ")
                                append(prayerText)
                            }

                            tvTime.text = getRelativeTime(item?.createdAt ?: "")

                            ivPosts.loadImage(
                                ApiConstants.IMAGE_BASE_URL + item?.user?.image,
                                placeholder = R.drawable.profile_icon
                            )

                            ivImage.loadImage(
                                ApiConstants.IMAGE_BASE_URL + getPreference("image", ""),
                                placeholder = R.drawable.profile_icon
                            )
                            tvDescription.text = item?.description ?: ""
//                            setupSeeMoreText(tvDescription, item?.description ?: "", this@QuestDetailActivity)

                            if (item?.isLike == 1) {
                                ivLike.setImageResource(R.drawable.selected_heart)
                                ivLike.imageTintList = ContextCompat.getColorStateList(
                                    this@QuestDetailActivity,
                                    R.color.star_red_color
                                )
                            } else {
                                ivLike.setImageResource(R.drawable.unselected_heart)
                                ivLike.imageTintList = ContextCompat.getColorStateList(
                                    this@QuestDetailActivity,
                                    R.color.black
                                )
                            }

                            if (item?.is_praise == 1) {
                                ivPrayer.imageTintList = ContextCompat.getColorStateList(
                                    this@QuestDetailActivity,
                                    R.color.golden_yellow
                                )
                            } else {
                                ivPrayer.imageTintList = ContextCompat.getColorStateList(
                                    this@QuestDetailActivity,
                                    R.color.black
                                )
                            }

                            setCommentData()
                        }
                    }

                    is CommonResponse -> {
                    }
                }
            }

            Status.LOADING -> {
                LoaderDialog.show(this)
            }

            Status.ERROR -> {
                LoaderDialog.dismiss()
                showCustomSnackbar(this, binding.root, value.message.toString())
                binding.nvScrollView.gone()
            }
        }
    }


    private fun setPopUpComment(
        view1: View,
        model: CommentResponse,
        item: CommentResponse.Replies?,
        notificationId: String
    ) {
        val view = MenuReportCommentBinding.inflate(layoutInflater)

        val myPopupWindow = PopupWindow(
            view.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        with(view) {
            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(
                    Intent(
                        this@QuestDetailActivity,
                        ReportUserActivity::class.java
                    ).apply {
                        putExtra("from", "postComment")
                        if (item != null) {
                            putExtra("id", item.id.toString())
                            putExtra("reportedTo", item.user_id.toString())
                            putExtra("postId", item.post_id.toString())
                            putExtra("username", item.user?.username.toString())

                        } else {
                            putExtra("id", model.id.toString())
                            putExtra("reportedTo", model.user_id.toString())
                            putExtra("postId", model.post_id.toString())
                            putExtra("username", model.user?.username.toString())
                        }

                    })
            }
            ivUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(
                    Intent(
                        this@QuestDetailActivity,
                        ReportUserActivity::class.java
                    ).apply {
                        putExtra("from", "postComment")
                        if (item != null) {
                            putExtra("id", item.id.toString())
                            putExtra("reportedTo", item.user_id.toString())
                            putExtra("postId", item.post_id.toString())
                        } else {
                            putExtra("id", model.id.toString())
                            putExtra("reportedTo", model.user_id.toString())
                            putExtra("postId", model.post_id.toString())
                        }
                    })
            }
        }
        myPopupWindow.showAsDropDown(view1, 0, -40)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun setCommentData() {
        observeTextChanges(binding.etMessage)
        mentionList.clear()
        mentionAdapter = MentionAdapter(mentionList) { user ->
            val text = binding.etMessage.text.toString()
            val cursorPos = binding.etMessage.selectionStart
            val atIndex = text.lastIndexOf("@", cursorPos - 1)

            if (atIndex != -1) {
                val newText = text.substring(0, atIndex) + "@${user.username} " +
                        text.substring(cursorPos)
                binding.etMessage.setText(newText)
                binding.etMessage.setSelection(atIndex + user.username.length + 2)
            }
            binding.rvMentions.gone()
        }
        binding.rvMentions.adapter = mentionAdapter
        setCommentAdapter()
        resetCommentPage = true
        getCommentList()

        with(binding) {
            ivImage.loadImage(
                ApiConstants.IMAGE_BASE_URL + getPreference("image", ""),
                R.drawable.profile_icon
            )
            ivSend.setOnClickListener {
                addComment(data.id ?: 0, parentId)
                Log.d("fgdgfdh", parentId.toString())
                username = ""
                etMessage.setText("")
            }
        }
    }

    private fun addComment(postId: Int, parentId: Int) {
        if (containsBannedWord(binding.etMessage.text.toString().trim())) {
            showCustomSnackbar(
                this,
                binding.root,
                "Your comment contains banned or inappropriate words. Please remove them before posting."
            )
            return
        }
        val rawComment = binding.etMessage.text.toString().trim()
        val cleanedComment = rawComment.replace(Regex("@\\w+"), "").trim()

        if (cleanedComment.isEmpty()) {
            showCustomSnackbar(
                this,
                binding.root,
                "Please enter comment"
            )
            return
        }

        val map = HashMap<String, String>()
        map["description"] = binding.etMessage.text.toString().trim()
        if (parentId != 0) {
            map["parent_transaction_id"] = parentId.toString()
        }
        val comment = binding.etMessage.text.toString().trim()

        val mentionPattern = Regex("@([A-Za-z0-9_]+)")

        val mentionedUsernames = mentionPattern.findAll(comment)
            .map { it.groupValues[1] }
            .toList()
        if (mentionedUsernames.isNotEmpty()) {
            map["tagged_user_id"] = mentionedUsernames.joinToString(separator = ",")
        }
        for ((key, value) in map) {
            Log.d("MapValues", "$key = $value")
        }

        when (from) {
            "prayer" -> {
                map["prayer_id"] = postId.toString()
                viewModel.addPrayerComment(map, this).observe(this, addCommentObserver)
            }

            "testimony" -> {
                map["testimony_id"] = postId.toString()
                viewModel.addTestimonyComment(map, this).observe(this, addCommentObserver)
            }

            else -> {
                map["community_forum_id"] = postId.toString()
                viewModel.addCommunityComment(map, this).observe(this, addCommentObserver)
            }
        }
    }

    private val addCommentObserver = Observer<Resource<Any>> { value ->
        when (value.status) {
            Status.SUCCESS -> {
                LoaderDialog.dismiss()
                when (value.data) {
                    is CommentCommonResponse -> {
                        with(binding) {
                            val res = value.data.body
                            if (parentId == 0) {
                                res?.let { commentList.add(0, it) }
                                commentAdapter?.notifyItemInserted(0)
                            } else {
                                res?.let {
                                    (commentList[mainPos].replies).add(
                                        CommentResponse.Replies(
                                            user = it.user,
                                            is_like = it.is_like,
                                            like_count = it.like_count,
                                            description = it.description,
                                            created_at = it.created_at,
                                            notification_id = it.notification_id,
                                            parent_transaction_id = it.parent_transaction_id,
                                            post_id = it.post_id,
                                            user_id = it.user_id,
                                            id = it.id,
                                            tagged_user_data = it.tagged_user_data,
                                            post_comment_tags = it.post_comment_tags
                                        )
                                    )
                                }
                                commentAdapter?.notifyItemChanged(mainPos)
                            }
                            this@QuestDetailActivity.parentId = 0
                            taggedId = 0
                            mainPos = -1
                            if (commentList.isEmpty()) {
                                tvNoDataFound.visible()
                            } else {
                                tvNoDataFound.gone()

                            }

                            data.commentCount = (data.commentCount ?: 0) + 1

                            if ((data.commentCount ?: 0) > 0) {
                                tvTotalComments.text = buildString {
                                    append("Comments (")
                                    append(formatCount(data.commentCount ?: 0))
                                    append(")")
                                }
                            }

                            val commentText =
                                if ((data.commentCount ?: 0) > 1) "Comments" else "Comment"
                            tvComments.text = buildString {
                                append(formatCount(data.commentCount ?: 0))
                                append(" ")
                                append(commentText)
                            }
                        }
                    }
                }
            }

            Status.ERROR -> {
                LoaderDialog.show(this)
                showCustomSnackbar(this, binding.root, value.message.toString())
            }

            Status.LOADING -> {
                LoaderDialog.show(this)
            }
        }
    }

    private fun getCommentList() {
        isApiRunning = true
        if (resetCommentPage) {
            currentCommentPage = 1
        }
        val map = HashMap<String, String>()
        map["limit"] = "15"
        map["page"] = currentCommentPage.toString()
        when (from) {
            "prayer" -> {
                map["prayer_id"] = data.id.toString()
                viewModel.prayerCommentList(map, this).observe(this, getCommentListObserver)
            }

            "testimony" -> {
                map["testimony_id"] = data.id.toString()
                viewModel.testimonyCommentList(map, this).observe(this, getCommentListObserver)
            }

            else -> {
                map["community_forum_id"] = data.id.toString()
                viewModel.communityCommentList(map, this).observe(this, getCommentListObserver)
            }
        }
    }

    private val getCommentListObserver = Observer<Resource<Any>> { value ->
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
                LoaderDialog.dismiss()
                when (value.data) {
                    is PostCommentListResposne -> {
                        with(binding) {
                            val res = value.data.body
                            if (resetCommentPage) {
                                commentList.clear()
                            }
                            commentList.addAll(res?.data ?: ArrayList())
                            commentAdapter?.notifyItemRangeChanged(0, commentList.size)
                            if (commentList.isEmpty()) {
                                tvNoDataFound.visible()
                            } else {
                                tvNoDataFound.gone()
                            }
                            currentCommentPage = (res?.current_page ?: 0) + 1
                            totalCommentPageCount = res?.total_pages ?: 0
                        }
                    }
                }
            }

            Status.ERROR -> {
                isApiRunning = false
                LoaderDialog.dismiss()
                showCustomSnackbar(this, binding.root, value.message.toString())
            }

            Status.LOADING -> {
                LoaderDialog.dismiss()
            }
        }
    }

    private fun setCommentAdapter() {
        binding.rvComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    if (currentCommentPage <= totalCommentPageCount && !isApiRunning) {
                        resetCommentPage = false
                        showDialog = false
                        getCommentList()
                    }
                }
            }
        })

        commentList.clear()
        commentAdapter = CommentAdapter(this, commentList)
        binding.rvComments.adapter = commentAdapter
        commentAdapter?.menuListener = { pos, view, repPos, mainComment, item, notificationId ->
            if (data.user?.id.toString() == getPreference("id", "")) {
                if (item != null) {
                    if (item.user?.id.toString() == getPreference("id", "")) {
                        setPopUpWindowDelete(view, pos, repPos, item.id.toString(), notificationId)
                    } else {
                        setPopUpWindowReportDelete(
                            view,
                            pos,
                            repPos,
                            item.id.toString(),
                            mainComment,
                            item,
                            notificationId
                        )
                    }
                } else {
                    if (mainComment.user?.id.toString() == getPreference("id", "")) {
                        setPopUpWindowDelete(
                            view,
                            pos,
                            repPos,
                            mainComment.id.toString(),
                            notificationId
                        )
                    } else {
                        setPopUpWindowReportDelete(
                            view,
                            pos,
                            repPos,
                            mainComment.id.toString(),
                            mainComment,
                            null,
                            notificationId
                        )
                    }
                }
            } else {
                if (item != null) {
                    if (item.user?.id.toString() == getPreference("id", "")) {
                        setPopUpWindowDelete(view, pos, repPos, item.id.toString(), notificationId)
                    } else {
                        setPopUpComment(view, mainComment, item, notificationId)
                    }
                } else {
                    if (mainComment.user?.id.toString() == getPreference("id", "")) {
                        setPopUpWindowDelete(
                            view,
                            pos,
                            repPos,
                            mainComment.id.toString(),
                            notificationId
                        )
                    } else {
                        setPopUpComment(view, mainComment, null, notificationId)
                    }
                }
            }
        }

        commentAdapter?.onLikeUnlike = { pos, id, status, postId ->
            val map = HashMap<String, String>()
            map["status"] = status
            when (from) {
                "prayer" -> {
                    map["prayer_id"] = data.id.toString()
                    map["prayer_comment_id"] = id
                    viewModel.prayerCommentLikeUnlike(map, this)
                        .observe(this, commentLikeUnlikeObserver)
                }

                "testimony" -> {
                    map["testimony_id"] = data.id.toString()
                    map["testimony_comment_id"] = id
                    viewModel.testimonyCommentLikeUnlike(map, this)
                        .observe(this, commentLikeUnlikeObserver)
                }

                else -> {
                    map["community_forum_id"] = data.id.toString()
                    map["community_forum_comment_id"] = id
                    viewModel.communityCommentLikeUnlike(map, this)
                        .observe(this, commentLikeUnlikeObserver)
                }
            }
        }

        commentAdapter?.onCommentEdit = { pos, repPos, commentId, desc, notificationId ->
            showMessageDialog(pos, repPos, commentId, data.id.toString(), desc, notificationId)
        }

        /*   commentAdapter?.replyListener = { pos, view, item, replyModel, replyPosition ->
               binding.etMessage.isFocusable = true
               parentId = item.id ?: 0
               mainPos = pos
               if (replyModel?.user != null) {
                   taggedId = replyModel.user.id ?: 0
                   username = replyModel.user.username.let { "@$it " }
                   binding.etMessage.setText(username)
                   binding.etMessage.setSelection(binding.etMessage.text.toString().length)
                   Log.d("kjfdsjbds",replyModel.user.first_name.toString())
                   Log.d("kjfdsjbds",replyModel.user.username.toString())
               } else {
                   username = item.user?.username.let { "@$it " }
                   taggedId = item.user?.id ?: 0
                   binding.etMessage.setText(username)
                   binding.etMessage.setSelection(binding.etMessage.text.toString().length)
               }
               binding.etMessage.showKeyboard()
           }*/


        commentAdapter?.replyListener = { pos, view, item, replyModel, replyPosition ->
            binding.etMessage.isFocusable = true
            parentId = item.id ?: 0
            mainPos = pos

            val usernameToMention = if (replyModel?.user != null) {
                replyModel.user.username
            } else {
                item.user?.username
            }

            taggedId = if (replyModel?.user != null) {
                replyModel.user.id ?: 0
            } else {
                item.user?.id ?: 0
            }

            val mentionText = "@$usernameToMention "
            username = mentionText
            binding.etMessage.setText(mentionText)
            binding.etMessage.setSelection(mentionText.length)

            binding.etMessage.showKeyboard()
        }

    }

    private val commentLikeUnlikeObserver = Observer<Resource<Any>> { value ->
        when (value.status) {
            Status.SUCCESS -> {
            }

            Status.LOADING -> {
            }

            Status.ERROR -> {
                showCustomSnackbar(
                    this,
                    binding.root,
                    value.message.toString()
                )
            }
        }
    }

    private fun setupMentionTextWatcher(editText: EditText) {
        val mentionPattern = Regex("@([A-Za-z0-9_]+)")
        val spannableFactory = CustomSpannableFactory()
        editText.setSpannableFactory(spannableFactory)

        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var lastText = ""
            private var selectionStart = 0
            private var selectionEnd = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!isFormatting) {
                    lastText = s.toString()
                    selectionStart = editText.selectionStart
                    selectionEnd = editText.selectionEnd
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: return
                val cursorPos = editText.selectionStart

                if (cursorPos > 0) {
                    val atIndex = text.lastIndexOf("@", cursorPos - 1)
                    if (atIndex != -1 && (atIndex == 0 || text[atIndex - 1].isWhitespace())) {
                        val query = text.substring(atIndex + 1, cursorPos)

                        mentionList.clear()
                        val filtered = if (query.isEmpty()) {
                            dummyFollowers
                        } else {
                            dummyFollowers.filter {
                                it.username.startsWith(query, ignoreCase = true)
                            }
                        }
                        mentionList.addAll(filtered.distinctBy { it.username })

                        if (mentionList.isNotEmpty()) {
                            binding.rvMentions.visible()
                            mentionAdapter?.notifyDataSetChanged()
                        } else {
                            binding.rvMentions.gone()
                        }
                    } else {
                        binding.rvMentions.gone()
                    }
                } else {
                    binding.rvMentions.gone()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                isFormatting = true

                try {
                    // Apply blue color to mentions
                    applyMentionSpans(s)

                    // Handle backspace to remove entire mention
                    handleMentionDeletion(s)

                } finally {
                    isFormatting = false
                }

                // Enable/disable send button
                if (s.toString().isNotEmpty()) {
                    binding.ivSend.backgroundTintList =
                        ContextCompat.getColorStateList(this@QuestDetailActivity, R.color.blue)
                    binding.ivSend.isEnabled = true
                } else {
                    binding.ivSend.backgroundTintList =
                        ContextCompat.getColorStateList(
                            this@QuestDetailActivity,
                            R.color.button_grey
                        )
                    binding.ivSend.isEnabled = false
                }
            }

            private fun applyMentionSpans(s: Editable) {
                // Remove existing spans
                val existingSpans = s.getSpans(0, s.length, ForegroundColorSpan::class.java)
                existingSpans.forEach { s.removeSpan(it) }

                // Apply new spans to mentions
                mentionPattern.findAll(s.toString()).forEach { matchResult ->
                    val start = matchResult.range.first
                    val end = matchResult.range.last + 1

                    val colorSpan = ForegroundColorSpan(
                        ContextCompat.getColor(
                            this@QuestDetailActivity,
                            R.color.blue
                        )
                    )
                    s.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            private fun handleMentionDeletion(s: Editable) {
                val currentText = s.toString()
                val currentSelection = editText.selectionStart

                // Check if backspace was pressed
                if (lastText.length > currentText.length && currentSelection > 0) {
                    val deletedCharCount = lastText.length - currentText.length

                    if (deletedCharCount == 1) { // Single character deletion (backspace)
                        // Find all mentions in the current text
                        val mentions = mentionPattern.findAll(currentText).toList()

                        // Check if we're deleting within any mention
                        for (mention in mentions) {
                            val mentionStart = mention.range.first
                            val mentionEnd = mention.range.last + 1

                            // If cursor is anywhere within or adjacent to the mention
                            if (currentSelection > mentionStart && currentSelection <= mentionEnd + 1) {
                                // Delete the entire mention
                                s.delete(mentionStart, mentionEnd)
                                editText.setSelection(mentionStart)
                                return
                            }
                        }
                    }
                }
            }
        })
    }

    private fun observeTextChanges(etMessage: EditText) {
        setupMentionTextWatcher(etMessage)
        /*  etMessage.addTextChangedListener(object : TextWatcher {
              override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

              override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                  val text = s?.toString() ?: return
                  val cursorPos = etMessage.selectionStart

                  // ---------- Mention Search ----------
                  if (cursorPos > 0) {
                      val atIndex = text.lastIndexOf("@", cursorPos - 1)
                      if (atIndex != -1 && (atIndex == 0 || text[atIndex - 1].isWhitespace())) {
                          val query = text.substring(atIndex + 1, cursorPos)

                          mentionList.clear()
                          val filtered = if (query.isEmpty()) {
                              dummyFollowers
                          } else {
                              dummyFollowers.filter {
                                  it.username.startsWith(query, ignoreCase = true)
                              }
                          }
                          mentionList.addAll(filtered.distinctBy { it.username })

                          if (mentionList.isNotEmpty()) {
                              binding.rvMentions.visible()
                              mentionAdapter?.notifyDataSetChanged()
                          } else {
                              binding.rvMentions.gone()
                          }
                      } else {
                          binding.rvMentions.gone()
                      }
                  } else {
                      binding.rvMentions.gone()
                  }
              }

              override fun afterTextChanged(s: Editable?) {
                  val currentText = s?.toString() ?: ""

                  if (parentId != 0 && username.isNotEmpty() &&
                      !currentText.startsWith(username.trim())
                  ) {
                      Log.d("dsgghdfh",username.trim())
                      Log.d("dsgghdfh",currentText.trim())
                      parentId = 0
                      username = ""
                  }
                  if (s.toString().isNotEmpty()) {
                      binding.ivSend.backgroundTintList =
                          ContextCompat.getColorStateList(this@QuestDetailActivity, R.color.blue)
                      binding.ivSend.isEnabled = true
                  } else {
                      binding.ivSend.backgroundTintList =
                          ContextCompat.getColorStateList(this@QuestDetailActivity, R.color.button_grey)
                      binding.ivSend.isEnabled = false
                  }
              }
          })*/
    }

    inner class CustomSpannableFactory : Spannable.Factory() {
        fun newEditable(source: CharSequence): Editable {
            return SpannableStringBuilder(source)
        }
    }

    private fun showMessageDialog(
        pos: Int,
        repPos: Int,
        commentId: String,
        postId: String,
        desc: String,
        notificationId: String
    ) {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val resetBinding = DialogEditMessageBinding.inflate(layoutInflater)
        customDialog.setContentView(resetBinding.root)
        customDialog.window?.setGravity(Gravity.BOTTOM)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        resetBinding.tvComments.setOnClickListener {
            customDialog.dismiss()
            deletePost(pos, repPos, commentId, notificationId)
        }

        // Store the initial comment text
        val initialComment = if (repPos != -1) {
            removeMentionIfMatches(
                desc ?: "",
                commentList[pos].replies[repPos].user?.username ?: ""
            )
        } else {
            removeMentionIfMatches(desc ?: "", commentList[pos].user?.username ?: "")
        }

        // Set the initial text with mention styling
        applyInitialMentionStyling(resetBinding.tvDescription, initialComment)

        // Initialize mention list and adapter for the dialog
        val dialogMentionList = mutableListOf<Follower>()
        val dialogMentionAdapter = MentionAdapter(dialogMentionList) { user ->
            val cursorPos = resetBinding.tvDescription.selectionStart
            val text = resetBinding.tvDescription.text
            val lastAtIndex = text?.lastIndexOf("@", cursorPos - 1) ?: -1

            if (lastAtIndex != -1) {
                val newText = text?.substring(0, lastAtIndex) + "@${user.username} " +
                        text?.substring(cursorPos)
                resetBinding.tvDescription.setText(newText)
                resetBinding.tvDescription.setSelection(lastAtIndex + user.username.length + 2)
            }
            resetBinding.rvMentions.visibility = View.GONE
        }

        resetBinding.rvMentions.adapter = dialogMentionAdapter

        resetBinding.tvReplies.setOnClickListener {
            if (containsBannedWord(resetBinding.tvDescription.text.toString().trim())) {
                showCustomSnackbar(
                    this,
                    resetBinding.root,
                    "Your comment contains banned or inappropriate words. Please remove them before posting."
                )
                return@setOnClickListener
            }

            val rawComment = resetBinding.tvDescription.text.toString().trim()
            val cleanedComment = rawComment.replace(Regex("@\\w+"), "").trim()

            if (cleanedComment.isEmpty()) {
                showCustomSnackbar(
                    this,
                    it.rootView,
                    "Please enter comment"
                )
                return@setOnClickListener
            }

            customDialog.dismiss()
            val map = HashMap<String, String>()
            map["description"] = resetBinding.tvDescription.text.toString().trim()
            val oldComment = desc ?: ""
            val newComment = resetBinding.tvDescription.text.toString().trim()

            val mentionPattern = Regex("@([A-Za-z0-9_]+)")

            val oldMentions = mentionPattern.findAll(oldComment)
                .map { it.groupValues[1] }
                .toList()

            val newMentions = mentionPattern.findAll(newComment)
                .map { it.groupValues[1] }
                .toList()

            val allMentions = (oldMentions + newMentions).distinct()

            if (allMentions.isNotEmpty()) {
                map["tagged_user_id"] = allMentions.joinToString(separator = ",")
            }

            for ((key, value) in map) {
                Log.d("MapValues", "$key = $value")
            }
            when (from) {
                "prayer" -> {
                    map["prayer_id"] = postId
                    map["prayer_comment_id"] = commentId
                    viewModel.prayerCommentEdit(map, this).observe(this) { value ->
                        when (value.status) {
                            Status.SUCCESS -> {
                                LoaderDialog.dismiss()
                                when (value.data) {
                                    is CommentCommonResponse -> {
                                        Log.d("dsgdgdg", "afdgGDSgds")
                                        val res = value.data.body
                                        if (repPos != -1) {
                                            commentList[pos].replies[repPos] =
                                                CommentResponse.Replies(
                                                    user = res?.user,
                                                    is_like = res?.is_like,
                                                    like_count = res?.like_count,
                                                    description = res?.description,
                                                    created_at = res?.created_at,
                                                    notification_id = res?.notification_id,
                                                    parent_transaction_id = res?.parent_transaction_id,
                                                    post_id = res?.post_id,
                                                    user_id = res?.user_id,
                                                    id = res?.id,
                                                    tagged_user_data = res?.tagged_user_data,
                                                    post_comment_tags = res?.post_comment_tags
                                                        ?: ArrayList()
                                                )
                                        } else {
                                            commentList[pos] = res ?: CommentResponse()
                                        }
                                        commentAdapter?.notifyItemChanged(pos)
                                        showCustomSnackbar(this, it, "Changes Saved.")
                                    }
                                }

                            }

                            Status.LOADING -> {
                                LoaderDialog.show(this)
                            }

                            Status.ERROR -> {
                                LoaderDialog.dismiss()
                                showCustomSnackbar(
                                    this,
                                    binding.root,
                                    value.message.toString()
                                )
                            }
                        }
                    }
                }

                "testimony" -> {
                    map["testimony_id"] = postId
                    map["testimony_comment_id"] = commentId
                    viewModel.testimonyCommentEdit(map, this).observe(this) { value ->
                        when (value.status) {
                            Status.SUCCESS -> {
                                LoaderDialog.dismiss()
                                when (value.data) {
                                    is CommentCommonResponse -> {
                                        Log.d("dsgdgdg", "afdgGDSgds")
                                        val res = value.data.body
                                        if (repPos != -1) {
                                            commentList[pos].replies[repPos] =
                                                CommentResponse.Replies(
                                                    user = res?.user,
                                                    is_like = res?.is_like,
                                                    like_count = res?.like_count,
                                                    description = res?.description,
                                                    created_at = res?.created_at,
                                                    notification_id = res?.notification_id,
                                                    parent_transaction_id = res?.parent_transaction_id,
                                                    post_id = res?.post_id,
                                                    user_id = res?.user_id,
                                                    id = res?.id,
                                                    tagged_user_data = res?.tagged_user_data,
                                                    post_comment_tags = res?.post_comment_tags
                                                        ?: ArrayList()
                                                )
                                        } else {
                                            commentList[pos] = res ?: CommentResponse()
                                            commentList[pos].description =
                                                resetBinding.tvDescription.text.toString().trim()
                                        }
                                        commentAdapter?.notifyItemChanged(pos)
                                        showCustomSnackbar(this, it, "Changes Saved.")
                                    }

                                    is CommonResponse -> {
                                        val res = value.data.body
                                        if (repPos != -1) {
                                            commentList[pos].replies[repPos].description =
                                                resetBinding.tvDescription.text.toString().trim()
                                        } else {
                                            commentList[pos].description =
                                                resetBinding.tvDescription.text.toString().trim()
                                        }
                                        commentAdapter?.notifyItemChanged(pos)
                                        showCustomSnackbar(this, it, "Changes Saved.")
                                    }
                                }

                            }

                            Status.LOADING -> {
                                LoaderDialog.show(this)
                            }

                            Status.ERROR -> {
                                LoaderDialog.dismiss()
                                showCustomSnackbar(
                                    this,
                                    binding.root,
                                    value.message.toString()
                                )
                            }
                        }
                    }
                }

                else -> {
                    map["community_forum_id"] = postId
                    map["community_forum_comment_id"] = commentId
                    viewModel.communityCommentEdit(map, this).observe(this) { value ->
                        when (value.status) {
                            Status.SUCCESS -> {
                                LoaderDialog.dismiss()
                                when (value.data) {
                                    is CommentCommonResponse -> {
                                        Log.d("dsgdgdg", "afdgGDSgds")
                                        val res = value.data.body
                                        if (repPos != -1) {
                                            commentList[pos].replies[repPos] =
                                                CommentResponse.Replies(
                                                    user = res?.user,
                                                    is_like = res?.is_like,
                                                    like_count = res?.like_count,
                                                    description = res?.description,
                                                    created_at = res?.created_at,
                                                    notification_id = res?.notification_id,
                                                    parent_transaction_id = res?.parent_transaction_id,
                                                    post_id = res?.post_id,
                                                    user_id = res?.user_id,
                                                    id = res?.id,
                                                    tagged_user_data = res?.tagged_user_data,
                                                    post_comment_tags = res?.post_comment_tags
                                                        ?: ArrayList()
                                                )
                                        } else {
                                            commentList[pos] = res ?: CommentResponse()
                                            commentList[pos].description =
                                                resetBinding.tvDescription.text.toString().trim()
                                        }
                                        commentAdapter?.notifyItemChanged(pos)
                                        showCustomSnackbar(this, it, "Changes Saved.")
                                    }
                                }

                            }

                            Status.LOADING -> {
                                LoaderDialog.show(this)
                            }

                            Status.ERROR -> {
                                LoaderDialog.dismiss()
                                showCustomSnackbar(
                                    this,
                                    binding.root,
                                    value.message.toString()
                                )
                            }
                        }
                    }
                }
            }
        }

        customDialog.show()

        // Setup mention functionality for the edit dialog
        setupEditDialogMentionTextWatcher(
            resetBinding.tvDescription,
            resetBinding.rvMentions,
            dialogMentionList,
            dialogMentionAdapter
        )

        // Enable/disable reply button based on initial text
        resetBinding.tvReplies.isEnabled = initialComment.isNotEmpty()
        resetBinding.tvReplies.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (initialComment.isNotEmpty()) R.color.blue else R.color.button_grey
        )
    }

    private fun setupEditDialogMentionTextWatcher(
        editText: EditText,
        mentionsRecyclerView: RecyclerView,
        mentionList: MutableList<Follower>,
        mentionAdapter: MentionAdapter
    ) {
        val mentionPattern = Regex("@([A-Za-z0-9_]+)")
        val spannableFactory = CustomSpannableFactory()
        editText.setSpannableFactory(spannableFactory)

        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var lastText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!isFormatting) {
                    lastText = s.toString()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: return
                val cursorPos = editText.selectionStart

                if (cursorPos > 0) {
                    val atIndex = text.lastIndexOf("@", cursorPos - 1)
                    if (atIndex != -1 && (atIndex == 0 || text[atIndex - 1].isWhitespace())) {
                        val query = text.substring(atIndex + 1, cursorPos)

                        mentionList.clear()
                        val filtered = if (query.isEmpty()) {
                            dummyFollowers
                        } else {
                            dummyFollowers.filter {
                                it.username.startsWith(query, ignoreCase = true)
                            }
                        }
                        mentionList.addAll(filtered.distinctBy { it.username })

                        if (mentionList.isNotEmpty()) {
                            mentionsRecyclerView.visible()
                            mentionAdapter.notifyDataSetChanged()
                        } else {
                            mentionsRecyclerView.gone()
                        }
                    } else {
                        mentionsRecyclerView.gone()
                    }
                } else {
                    mentionsRecyclerView.gone()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                isFormatting = true

                try {
                    // Apply blue color to mentions
                    applyMentionSpans(s)

                    // Handle backspace to remove entire mention
                    handleMentionDeletion(s, editText)

                } finally {
                    isFormatting = false
                }

                // Enable/disable reply button
                val replyButton =
                    editText.rootView.findViewById<android.widget.Button>(R.id.tvReplies)
                replyButton?.let { button ->
                    if (s.toString().isNotEmpty()) {
                        button.backgroundTintList =
                            ContextCompat.getColorStateList(this@QuestDetailActivity, R.color.blue)
                        button.isEnabled = true
                    } else {
                        button.backgroundTintList = ContextCompat.getColorStateList(
                            this@QuestDetailActivity,
                            R.color.button_grey
                        )
                        button.isEnabled = false
                    }
                }
            }

            private fun applyMentionSpans(s: Editable) {
                // Remove existing spans
                val existingSpans = s.getSpans(0, s.length, ForegroundColorSpan::class.java)
                existingSpans.forEach { s.removeSpan(it) }

                // Apply new spans to mentions
                mentionPattern.findAll(s.toString()).forEach { matchResult ->
                    val start = matchResult.range.first
                    val end = matchResult.range.last + 1
                    val colorSpan = ForegroundColorSpan(
                        ContextCompat.getColor(
                            this@QuestDetailActivity,
                            R.color.blue
                        )
                    )
                    s.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            private fun handleMentionDeletion(s: Editable, editText: EditText) {
                val currentText = s.toString()
                val currentSelection = editText.selectionStart

                // Check if backspace was pressed
                if (lastText.length > currentText.length && currentSelection > 0) {
                    val deletedCharCount = lastText.length - currentText.length

                    if (deletedCharCount == 1) { // Single character deletion (backspace)
                        // Find all mentions in the current text
                        val mentions = mentionPattern.findAll(currentText).toList()

                        // Check if we're deleting within any mention
                        for (mention in mentions) {
                            val mentionStart = mention.range.first
                            val mentionEnd = mention.range.last + 1

                            // If cursor is anywhere within or adjacent to the mention
                            if (currentSelection > mentionStart && currentSelection <= mentionEnd + 1) {
                                // Delete the entire mention
                                s.delete(mentionStart, mentionEnd)
                                editText.setSelection(mentionStart)
                                return
                            }
                        }
                    }
                }
            }
        })
    }

    private fun applyInitialMentionStyling(editText: EditText, text: String) {
        val mentionPattern = Regex("@([A-Za-z0-9_]+)")
        val spannable = SpannableStringBuilder(text)

        mentionPattern.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue))
            spannable.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        editText.setText(spannable)
        editText.setSelection(text.length)
    }
    /*  private fun showMessageDialog(
          pos: Int,
          repPos: Int,
          commentId: String,
          postId: String,
          desc: String,
          notificationId: String
      ) {
          val customDialog = Dialog(this)
          customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
          val resetBinding = DialogEditMessageBinding.inflate(layoutInflater)
          customDialog.setContentView(resetBinding.root)
          customDialog.window?.setGravity(Gravity.BOTTOM)
          customDialog.window?.setLayout(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT
          )
          customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

          resetBinding.tvComments.setOnClickListener {
              customDialog.dismiss()
              deletePost(pos,repPos,commentId,notificationId)
          }

          if (repPos != -1) {
              val comment = removeMentionIfMatches(
                  desc ?: "",
                  commentList[pos].replies[repPos].user?.username ?: ""
              )
              resetBinding.tvDescription.setText(comment)
          } else {
              val comment = removeMentionIfMatches(desc ?: "", commentList[pos].user?.username ?: "")
              resetBinding.tvDescription.setText(comment)
          }

          resetBinding.tvReplies.setOnClickListener {
              if (containsBannedWord(resetBinding.tvDescription.text.toString().trim())) {
                  showCustomSnackbar(
                      this,
                      resetBinding.root,
                      "Your comment contains banned or inappropriate words. Please remove them before posting."
                  )
                  return@setOnClickListener
              }
              customDialog.dismiss()
              val map = HashMap<String, String>()
              map["description"] = resetBinding.tvDescription.text.toString().trim()
              val oldComment = desc ?: ""
              val newComment = resetBinding.tvDescription.text.toString().trim()

              val mentionPattern = Regex("@([A-Za-z0-9_]+)")

              val oldMentions = mentionPattern.findAll(oldComment)
                  .map { it.groupValues[1] }
                  .toList()

              val newMentions = mentionPattern.findAll(newComment)
                  .map { it.groupValues[1] }
                  .toList()

              val allMentions = (oldMentions + newMentions).distinct()

              if (allMentions.isNotEmpty()) {
                  map["tagged_user_id"] = allMentions.joinToString(separator = ",")
              }

              for ((key, value) in map) {
                  Log.d("MapValues", "$key = $value")
              }
              when(from) {
                  "prayer" -> {
                      map["prayer_id"] = postId
                      map["prayer_comment_id"] = commentId
                      viewModel.prayerCommentEdit(map, this).observe(this) { value ->
                          when (value.status) {
                              Status.SUCCESS -> {
                                  LoaderDialog.dismiss()
                                  when(value.data){
                                      is CommentCommonResponse -> {
                                          Log.d("dsgdgdg","afdgGDSgds")
                                          val res = value.data.body
                                          if (repPos != -1) {
                                              commentList[pos].replies[repPos] =  CommentResponse.Replies(
                                                  user = res?.user,
                                                  is_like = res?.is_like,
                                                  like_count = res?.like_count,
                                                  description = res?.description,
                                                  created_at = res?.created_at,
                                                  notification_id = res?.notification_id,
                                                  parent_transaction_id = res?.parent_transaction_id,
                                                  post_id = res?.post_id,
                                                  user_id = res?.user_id,
                                                  id = res?.id,
                                                  tagged_user_data = res?.tagged_user_data,
                                                  post_comment_tags = res?.post_comment_tags ?: ArrayList()
                                              )
                                          } else {
                                              commentList[pos] = res ?: CommentResponse()
                                          }
                                          commentAdapter?.notifyItemChanged(pos)
                                          showCustomSnackbar(this, it, "Changes Saved.")
                                      }
                                  }

                              }

                              Status.LOADING -> {
                                  LoaderDialog.show(this)
                              }

                              Status.ERROR -> {
                                  LoaderDialog.dismiss()
                                  showCustomSnackbar(
                                      this,
                                      binding.root,
                                      value.message.toString()
                                  )
                              }
                          }
                      }
                  }
                  "testimony" -> {
                      map["testimony_id"] = postId
                      map["testimony_comment_id"] = commentId
                      viewModel.testimonyCommentEdit(map, this).observe(this) { value ->
                          when (value.status) {
                              Status.SUCCESS -> {
                                  LoaderDialog.dismiss()
                                  when(value.data){
                                      is CommentCommonResponse -> {
                                          Log.d("dsgdgdg","afdgGDSgds")
                                          val res = value.data.body
                                          if (repPos != -1) {
                                              commentList[pos].replies[repPos] =  CommentResponse.Replies(
                                                  user = res?.user,
                                                  is_like = res?.is_like,
                                                  like_count = res?.like_count,
                                                  description = res?.description,
                                                  created_at = res?.created_at,
                                                  notification_id = res?.notification_id,
                                                  parent_transaction_id = res?.parent_transaction_id,
                                                  post_id = res?.post_id,
                                                  user_id = res?.user_id,
                                                  id = res?.id,
                                                  tagged_user_data = res?.tagged_user_data,
                                                  post_comment_tags = res?.post_comment_tags ?: ArrayList()
                                              )
                                          } else {
                                              commentList[pos] = res ?: CommentResponse()
                                              *//*commentList[pos].description =
                                                resetBinding.tvDescription.text.toString().trim()*//*
                                        }
                                        commentAdapter?.notifyItemChanged(pos)
                                        showCustomSnackbar(this, it, "Changes Saved.")
                                    }

                                   *//* is CommonResponse->{
                                        val res = value.data.body
                                        if (repPos != -1){
                                            commentList[pos].replies[repPos].description = resetBinding.tvDescription.text.toString().trim()
                                        }else{
                                            commentList[pos].description = resetBinding.tvDescription.text.toString().trim()
                                        }
                                        commentAdapter?.notifyItemChanged(pos)
                                        showCustomSnackbar(this, it, "Changes Saved.")
                                    }*//*
                                }

                            }

                            Status.LOADING -> {
                                LoaderDialog.show(this)
                            }

                            Status.ERROR -> {
                                LoaderDialog.dismiss()
                                showCustomSnackbar(
                                    this,
                                    binding.root,
                                    value.message.toString()
                                )
                            }
                        }
                    }
                }
                else->{
                    map["community_forum_id"] = postId
                    map["community_forum_comment_id"] = commentId
                    viewModel.communityCommentEdit(map, this).observe(this) { value ->
                        when (value.status) {
                            Status.SUCCESS -> {
                                LoaderDialog.dismiss()
                                when(value.data){
                                    is CommentCommonResponse -> {
                                        Log.d("dsgdgdg","afdgGDSgds")
                                        val res = value.data.body
                                        if (repPos != -1) {
                                            commentList[pos].replies[repPos] =  CommentResponse.Replies(
                                                user = res?.user,
                                                is_like = res?.is_like,
                                                like_count = res?.like_count,
                                                description = res?.description,
                                                created_at = res?.created_at,
                                                notification_id = res?.notification_id,
                                                parent_transaction_id = res?.parent_transaction_id,
                                                post_id = res?.post_id,
                                                user_id = res?.user_id,
                                                id = res?.id,
                                                tagged_user_data = res?.tagged_user_data,
                                                post_comment_tags = res?.post_comment_tags ?: ArrayList()
                                            )
                                        } else {
                                            commentList[pos] = res ?: CommentResponse()
                                            *//*commentList[pos].description =
                                                resetBinding.tvDescription.text.toString().trim()*//*
                                        }
                                        commentAdapter?.notifyItemChanged(pos)
                                        showCustomSnackbar(this, it, "Changes Saved.")
                                    }
                                }

                            }

                            Status.LOADING -> {
                                LoaderDialog.show(this)
                            }

                            Status.ERROR -> {
                                LoaderDialog.dismiss()
                                showCustomSnackbar(
                                    this,
                                    binding.root,
                                    value.message.toString()
                                )
                            }
                        }
                    }
                }
            }


        }
        customDialog.show()

        val mentionList = mutableListOf<Follower>() // Fill this from API (like your post_comment_tags)
        val mentionAdapter = MentionAdapter(mentionList) { user ->
            // Insert selected username into EditText
            val cursorPos = resetBinding.tvDescription.selectionStart
            val text = resetBinding.tvDescription.text
            val lastAtIndex = text?.lastIndexOf("@", cursorPos - 1) ?: -1

            if (lastAtIndex != -1) {
                text?.replace(lastAtIndex, cursorPos, "@${user.username} ")
                resetBinding.tvDescription.setSelection(lastAtIndex + user.username.length + 2)
            }
            resetBinding.rvMentions.visibility = View.GONE
        }

        resetBinding.rvMentions.adapter = mentionAdapter

        resetBinding.tvDescription.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: return
                val cursorPos = resetBinding.tvDescription.selectionStart

                if (cursorPos > 0) {
                    val atIndex = text.lastIndexOf("@", cursorPos - 1)
                    if (atIndex != -1 && (atIndex == 0 || text[atIndex - 1].isWhitespace())) {
                        val query = text.substring(atIndex + 1, cursorPos)

                        mentionList.clear()
                        val filtered = if (query.isEmpty()) {
                            dummyFollowers
                        } else {
                            dummyFollowers.filter {
                                it.username.startsWith(query, ignoreCase = true)
                            }
                        }

                        mentionList.addAll(filtered.distinctBy { it.username })

                        if (mentionList.isNotEmpty()) {
                            resetBinding.rvMentions.visible()
                            mentionAdapter?.notifyDataSetChanged()
                        } else {
                            resetBinding.rvMentions.gone()
                        }
                    } else {
                        resetBinding.rvMentions.gone()
                    }
                } else {
                    resetBinding.rvMentions.gone()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                resetBinding.tvReplies.stateListAnimator = null
                if (s.toString().isNotEmpty()) {
                    resetBinding.tvReplies.backgroundTintList =
                        ContextCompat.getColorStateList(this@QuestDetailActivity, R.color.blue)
                    resetBinding.tvReplies.isEnabled = true
                } else {
                    resetBinding.tvReplies.backgroundTintList =
                        ContextCompat.getColorStateList(this@QuestDetailActivity, R.color.button_grey)
                    resetBinding.tvReplies.isEnabled = false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })


        *//* resetBinding.tvDescription.addTextChangedListener(object : TextWatcher {
             override fun beforeTextChanged(
                 s: CharSequence?,
                 start: Int,
                 count: Int,
                 after: Int
             ) {

             }

             override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
             }

             override fun afterTextChanged(s: Editable?) {
                 resetBinding.tvReplies.stateListAnimator = null
                 if (s.toString().isNotEmpty()) {
                     resetBinding.tvReplies.backgroundTintList =
                         ContextCompat.getColorStateList(requireContext(), R.color.blue)
                     resetBinding.tvReplies.isEnabled = true
                 } else {
                     resetBinding.tvReplies.backgroundTintList =
                         ContextCompat.getColorStateList(requireContext(), R.color.button_grey)
                     resetBinding.tvReplies.isEnabled = false
                 }
             }
         })*//*
    }*/


    private fun setPopUpWindowDelete(
        view1: View,
        pos: Int,
        repPos: Int,
        commentId: String,
        notificationId: String
    ) {
        val view = MenuReportMemberBinding.inflate(layoutInflater)

        val myPopupWindow = PopupWindow(
            view.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        with(view) {
            ivUser.setImageResource(R.drawable.del_icon)
            ivUser.imageTintList =
                ContextCompat.getColorStateList(this@QuestDetailActivity, R.color.black)
            tvNotDone.text = "Delete Comment"
            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                deletePost(pos, repPos, commentId, notificationId)

            }
        }

        myPopupWindow.showAsDropDown(view1, 0, -80)
    }

    private fun setPopUpWindowReportDelete(
        view1: View,
        pos: Int,
        repPos: Int,
        commentId: String,
        mainComment: CommentResponse,
        item: CommentResponse.Replies?,
        notificationId: String
    ) {
        val view = MenuReportDeleteDialogBinding.inflate(layoutInflater)

        val myPopupWindow = PopupWindow(
            view.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        with(view) {
            clDeleteUser.setOnClickListener {
                myPopupWindow.dismiss()
                deletePost(pos, repPos, commentId, notificationId)
            }

            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(
                    Intent(
                        this@QuestDetailActivity,
                        ReportUserActivity::class.java
                    ).apply {
                        putExtra("from", "postComment")
                        if (item != null) {
                            putExtra("id", item.id.toString())
                            putExtra("reportedTo", item.user_id.toString())
                            putExtra("postId", item.post_id.toString())
                            putExtra("username", item.user?.username.toString())

                        } else {
                            putExtra("id", mainComment.id.toString())
                            putExtra("reportedTo", mainComment.user_id.toString())
                            putExtra("postId", mainComment.post_id.toString())
                            putExtra("username", mainComment.user?.username.toString())
                        }

                    })
            }
            ivUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(
                    Intent(
                        this@QuestDetailActivity,
                        ReportUserActivity::class.java
                    ).apply {
                        putExtra("from", "postComment")
                        if (item != null) {
                            putExtra("id", item.id.toString())
                            putExtra("reportedTo", item.user_id.toString())
                            putExtra("postId", item.post_id.toString())
                            putExtra("username", item.user?.username.toString())

                        } else {
                            putExtra("id", mainComment.id.toString())
                            putExtra("reportedTo", mainComment.user_id.toString())
                            putExtra("postId", mainComment.post_id.toString())
                            putExtra("username", mainComment.user?.username.toString())

                        }
                    })
            }
        }

        myPopupWindow.showAsDropDown(view1, 0, -80)
    }

    private fun deletePost(pos: Int, repPos: Int, commentId: String, notificationId: String) {
        when (from) {
            "prayer" -> {
                viewModel.prayerCommentDelete(commentId, this, notificationId)
                    .observe(this) { value ->
                        when (value.status) {
                            Status.SUCCESS -> {
                                LoaderDialog.dismiss()
                                when (value.data) {
                                    is CommonResponse -> {
                                        val res = value.data.body
                                        if (repPos != -1) {
                                            commentList[pos].replies.removeAt(repPos)
                                        } else {
                                            commentList.removeAt(pos)
                                        }
                                        commentAdapter?.notifyDataSetChanged()
                                        showCustomSnackbar(this, binding.root, "Comment Deleted.")

                                        if (commentList.isNotEmpty()) {
                                            binding.tvNoDataFound.gone()
                                        } else {
                                            binding.tvNoDataFound.visible()
                                        }

                                        data.commentCount = (data.commentCount ?: 0) - 1

                                        if ((data.commentCount ?: 0) > 0) {
                                            binding.tvTotalComments.text = buildString {
                                                append("Comments (")
                                                append(formatCount(data.commentCount ?: 0))
                                                append(")")
                                            }
                                        }

                                        val commentText = if ((data.commentCount
                                                ?: 0) > 1
                                        ) "Comments" else "Comment"
                                        binding.tvComments.text = buildString {
                                            append(formatCount(data.commentCount ?: 0))
                                            append(" ")
                                            append(commentText)
                                        }
                                    }
                                }

                            }

                            Status.LOADING -> {
                                LoaderDialog.show(this)
                            }

                            Status.ERROR -> {
                                LoaderDialog.dismiss()
                                showCustomSnackbar(
                                    this,
                                    binding.root,
                                    value.message.toString()
                                )
                            }
                        }
                    }
            }

            "testimony" -> {
                viewModel.testimonyCommentDelete(commentId, this, notificationId)
                    .observe(this) { value ->
                        when (value.status) {
                            Status.SUCCESS -> {
                                LoaderDialog.dismiss()
                                when (value.data) {
                                    is CommonResponse -> {
                                        val res = value.data.body
                                        if (repPos != -1) {
                                            commentList[pos].replies.removeAt(repPos)
                                        } else {
                                            commentList.removeAt(pos)
                                        }
                                        commentAdapter?.notifyDataSetChanged()
                                        showCustomSnackbar(this, binding.root, "Comment Deleted.")

                                        if (commentList.isNotEmpty()) {
                                            binding.tvNoDataFound.gone()
                                        } else {
                                            binding.tvNoDataFound.visible()
                                        }

                                        data.commentCount = (data.commentCount ?: 0) - 1

                                        if ((data.commentCount ?: 0) > 0) {
                                            binding.tvTotalComments.text = buildString {
                                                append("Comments (")
                                                append(formatCount(data.commentCount ?: 0))
                                                append(")")
                                            }
                                        }

                                        val commentText = if ((data.commentCount
                                                ?: 0) > 1
                                        ) "Comments" else "Comment"
                                        binding.tvComments.text = buildString {
                                            append(formatCount(data.commentCount ?: 0))
                                            append(" ")
                                            append(commentText)
                                        }
                                    }
                                }

                            }

                            Status.LOADING -> {
                                LoaderDialog.show(this)
                            }

                            Status.ERROR -> {
                                LoaderDialog.dismiss()
                                showCustomSnackbar(
                                    this,
                                    binding.root,
                                    value.message.toString()
                                )
                            }
                        }
                    }
            }

            else -> {
                viewModel.communityCommentDelete(commentId, this, notificationId)
                    .observe(this) { value ->
                        when (value.status) {
                            Status.SUCCESS -> {
                                LoaderDialog.dismiss()
                                when (value.data) {
                                    is CommonResponse -> {
                                        val res = value.data.body
                                        if (repPos != -1) {
                                            commentList[pos].replies.removeAt(repPos)
                                        } else {
                                            commentList.removeAt(pos)
                                        }
                                        commentAdapter?.notifyDataSetChanged()
                                        showCustomSnackbar(this, binding.root, "Comment Deleted.")

                                        if (commentList.isNotEmpty()) {
                                            binding.tvNoDataFound.gone()
                                        } else {
                                            binding.tvNoDataFound.visible()
                                        }

                                        data.commentCount = (data.commentCount ?: 0) - 1

                                        if ((data.commentCount ?: 0) > 0) {
                                            binding.tvTotalComments.text = buildString {
                                                append("Comments (")
                                                append(formatCount(data.commentCount ?: 0))
                                                append(")")
                                            }
                                        }

                                        val commentText = if ((data.commentCount
                                                ?: 0) > 1
                                        ) "Comments" else "Comment"
                                        binding.tvComments.text = buildString {
                                            append(formatCount(data.commentCount ?: 0))
                                            append(" ")
                                            append(commentText)
                                        }
                                    }
                                }

                            }

                            Status.LOADING -> {
                                LoaderDialog.show(this)
                            }

                            Status.ERROR -> {
                                LoaderDialog.dismiss()
                                showCustomSnackbar(
                                    this,
                                    binding.root,
                                    value.message.toString()
                                )
                            }
                        }
                    }
            }
        }
    }

    private fun setCategoryWindow(view1: View, text: String) {
        val balloon = Balloon.Builder(this)
            .setWidthRatio(0.5f)
            .setHeight(70)
            .setTextSize(15f)
            .setPaddingLeft(4)
            .setPaddingRight(4)
            .setPaddingTop(8)
            .setPaddingBottom(8)
            .setText(text)
            .setArrowSize(12)
            .setArrowOrientation(ArrowOrientation.BOTTOM)
            .setCornerRadius(12f)
            .setMarginLeft(4)
            .setMarginRight(8)
            .setBackgroundColor(ContextCompat.getColor(this, R.color.black_translucent))
            .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
            .setLifecycleOwner(this)
            .build()

        balloon.bodyWindow.isOutsideTouchable = true
        balloon.showAlignBottom(view1)

    }

    override fun onResume() {
        super.onResume()
        getList()
    }

    private fun getList() {
        val map = HashMap<String, String>()
        map["page"] = "1"
        map["limit"] = "100000"
        map["type"] = "1"
        map["user_id"] = getPreference("id", "")

        viewModel.userFollowFollowingList(map, this)
            .observe(this) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                        LoaderDialog.dismiss()
                        when (value.data) {
                            is FollowFollowingResponse -> {
                                with(binding) {
                                    val res = value.data.body
                                    dummyFollowers.addAll(res?.data?.map {
                                        Follower(
                                            id = (it.id ?: 0).toString(),
                                            username = it.follow_to_user?.username ?: "",
                                            profileImageUrl = (it.follow_to_user?.image ?: "")
                                        )
                                    } ?: emptyList())
                                }
                            }
                        }
                    }

                    Status.LOADING -> {
                    }

                    Status.ERROR -> {
                    }
                }
            }
    }

}