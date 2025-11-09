package com.live.azurah.activity

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import android.view.WindowInsets
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.live.azurah.R
import com.live.azurah.adapter.CommentAdapter
import com.live.azurah.adapter.Follower
import com.live.azurah.adapter.MentionAdapter
import com.live.azurah.adapter.PostDetailAdapter
import com.live.azurah.autoPlay.ScrollListener
import com.live.azurah.autoPlay.VideoMultiplePostMyAutoPlayHelper
import com.live.azurah.databinding.ActivityMyPostsBinding
import com.live.azurah.databinding.DialogEditMessageBinding
import com.live.azurah.databinding.DialogShareBinding
import com.live.azurah.databinding.DialogSharePrivateBinding
import com.live.azurah.databinding.ItemBottomSheetCommentBinding
import com.live.azurah.databinding.MenuReportBinding
import com.live.azurah.databinding.MenuReportCommentBinding
import com.live.azurah.databinding.MenuReportDeleteDialogBinding
import com.live.azurah.databinding.MenuReportMemberBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.fragment.SearchHomeFragment
import com.live.azurah.fragment.SuggetionForYouFragment
import com.live.azurah.fragment.UserLikesFragment
import com.live.azurah.model.CommentCommonResponse
import com.live.azurah.model.CommentResponse
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.FollowFollowingResponse
import com.live.azurah.model.HomeSearchResposne
import com.live.azurah.model.PostCommentListResposne
import com.live.azurah.model.PostResponse
import com.live.azurah.model.SavedPostResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.containsBannedWord
import com.live.azurah.util.formatCount
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.removeMentionIfMatches
import com.live.azurah.util.setColoredUsername
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.showKeyboard
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPostsActivity : AppCompatActivity(), Observer<Resource<Any>> , ScrollListener{
    private lateinit var binding: ActivityMyPostsBinding
    private var type = ""
    private lateinit var postAdapter: PostDetailAdapter
    private var list = ArrayList<PostResponse.Body.Data>()
    private val commentList = ArrayList<CommentResponse>()
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var videoAutoPlayHelper: VideoMultiplePostMyAutoPlayHelper? = null
    private var commentAdapter: CommentAdapter? = null
    private var mBottomSheetBinding: ItemBottomSheetCommentBinding? = null
    private var parentId = 0
    private var taggedId = 0
    private var mainPos = -1
    private var username = ""
    private var scrollPos = 0
    private var currentPage = 0
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    private var showDialog = true
    private var currentCommentPage = 1
    private var totalCommentPageCount = 0
    private var resetCommentPage = false
    private var otherUserId = ""
    private var search = ""
    private var mentionAdapter: MentionAdapter? = null
    private var mentionList = mutableListOf<Follower>()
    private val dummyFollowers = ArrayList<Follower>()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostsBinding.inflate(layoutInflater)
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
        type = intent.getStringExtra("type") ?: ""
        list = intent.getSerializableExtra("list") as ArrayList<PostResponse.Body.Data>
        scrollPos = intent.getIntExtra("scrollPos", 0)
        currentPage = intent.getIntExtra("currentPage", 0)
        totalPageCount = intent.getIntExtra("totalPage", 0)
        otherUserId = intent.getStringExtra("otherUserId") ?: ""
        search = intent.getStringExtra("search") ?: ""
        Log.d("sdfdfdsf", type)
        if (type == "1") {
            binding.tvBookmark.text = "My Posts"
            binding.tvBookmark.visibility = View.GONE
        } else if (type == "2") {
            binding.tvBookmark.text = "Posts"
            binding.tvBookmark.visibility = View.GONE
        } else {
            binding.tvBookmark.visibility = View.VISIBLE
            binding.tvBookmark.text = "Saved Posts"
        }
        initListener()
        if (list.isNotEmpty()) {
            setVideoHelper()
            setAdapter()
            binding.rvPosts.scrollToPosition(scrollPos)
        } else {
            setAdapter()
            resetPage = true
            showDialog = true
            getPosts()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            resetPage = true
            showDialog = false
            getPosts()
            binding.swipeRefreshLayout.isRefreshing = true
        }
    }

    private fun initListener() {
        with(binding) {
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun setAdapter() {
        if (type.isEmpty()) {
            type = "0"
        }
        postAdapter = PostDetailAdapter(this, binding.rvPosts, type.toInt())
        binding.rvPosts.adapter = postAdapter
        postAdapter.submitList(list)

        postAdapter.onSuggestionSeeAll = { pos ->
            replaceFragment(SuggetionForYouFragment())
        }

        postAdapter.onTagClick={tag->
            replaceFragment(SearchHomeFragment(tag))
        }

        postAdapter.onLikeUnlike = { pos, model ->
            val map = HashMap<String, String>()
            map["post_id"] = model.id.toString()
            map["status"] = model.is_like.toString()
            viewModel.postLikeUnlike(map, this).observe(this) { value ->
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
        }

        postAdapter.onBookmark = { pos, model ->
            val map = HashMap<String, String>()
            map["post_id"] = model.id.toString()
            map["status"] = model.is_bookmark.toString()
            viewModel.postBookmark(map, this).observe(this) { value ->
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
        }

        postAdapter.followUnfollowListener = { _, model, _ ->
            val map = HashMap<String, String>()
            map["follow_by"] = getPreference("id", "")
            map["follow_to"] = model.id.toString()
            map["status"] = model.isFriend.toString()
            viewModel.followUnfollow(map, this).observe(this) { value ->
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
        }

        postAdapter.likeListener = { _, model ->
            val bundle = Bundle()
            bundle.putString("postId", model.id.toString())
            val fragment = UserLikesFragment()
            fragment.arguments = bundle
            replaceFragment(fragment)
        }

        postAdapter.commentListener = { pos, model ->
            commentsBottomSheet(model)
        }

        postAdapter.menuListener = { pos, model, view ->
            if (type == "1" && otherUserId.isEmpty()) {
                if (getPreference("id", "") == model.user?.id.toString()) {
                    setPopUpWindowDelete(view, model, pos)
                } else {
                    setPopUpWindow(view, model)
                }
            } else if (type == "3") {
                if (getPreference("id", "") == model.user?.id.toString()) {
                    setPopUpWindowDelete(view, model, pos)
                } else {
                    setPopUpWindow(view, model)
                }
            } else {
                setPopUpWindow(view, model)

            }
        }

        postAdapter.shareListener = { pos,model->
            if (model.user?.profile_type == 2){
                if (model.user?.profile_type == 2) {
                    val shareLink = buildString {
                        append("Want to see this post?\n")
                        append("Download Azrius – the Christian social media app where you can explore Bible Quests, share testimonies, join discussions, and connect with others through faith.\n")
                        append("Available now on the App Store and Google Play.\n")
                        append("Post link: https://app.azrius.co.uk/common_api/deepLinking/post?post_id=")
                        append(model.id.toString())
                    }

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareLink)
                    }

                    startActivity(Intent.createChooser(intent, "Share Post"))
                }
//                showShareDialog(pos,model.id.toString())
            }else{
                if (model.user?.id.toString() == getPreference("id","")){
                    if (model.user?.profile_type == 2) {
                        val shareLink = buildString {
                            append("Want to see this post?\n")
                            append("Download Azrius – the Christian social media app where you can explore Bible Quests, share testimonies, join discussions, and connect with others through faith.\n")
                            append("Available now on the App Store and Google Play.\n")
                            append("Post link: https://app.azrius.co.uk/common_api/deepLinking/post?post_id=")
                            append(model.id.toString())
                        }

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareLink)
                        }

                        startActivity(Intent.createChooser(intent, "Share Post"))
                    }
//                    showShareDialog(pos,model.id.toString())
                }else{
                    showSharePrivateDialog()
                }

            }
        }

        /* postAdapter.shareClick = { pos ->
             if (pos == 0 || pos == 3){
                 showShareDialog(pos)
             }else{
                 showSharePrivateDialog()
             }
         }*/
    }

    private fun setPopUpWindow(view1: View, model: PostResponse.Body.Data) {
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
                startActivity(Intent(this@MyPostsActivity, ReportPostActivity::class.java).apply {
                    putExtra("from", "post")
                    putExtra("id", model.id.toString())
                    putExtra("reportedTo", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())

                })
            }

            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@MyPostsActivity, ReportUserActivity::class.java).apply {
                    putExtra("from", "post")
                    putExtra("id", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())

                })
            }

            ivReportUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@MyPostsActivity, ReportPostActivity::class.java).apply {
                    putExtra("from", "post")
                    putExtra("id", model.id.toString())
                    putExtra("reportedTo", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())


                })
            }

            ivUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@MyPostsActivity, ReportUserActivity::class.java).apply {
                    putExtra("from", "post")
                    putExtra("id", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())

                })
            }

            ivBlockUser.setOnClickListener {
                myPopupWindow.dismiss()
                sureDialog(model)
            }

            tvBlockUser.setOnClickListener {
                myPopupWindow.dismiss()
                sureDialog(model)
            }
        }

        myPopupWindow.showAsDropDown(view1, 0, -90)
    }

    private fun setPopUpComment(
        view1: View,
        model: CommentResponse,
        item: CommentResponse.Replies?
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
                startActivity(Intent(this@MyPostsActivity, ReportUserActivity::class.java).apply {
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
                startActivity(Intent(this@MyPostsActivity, ReportUserActivity::class.java).apply {
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
        }
        myPopupWindow.showAsDropDown(view1, 0, -40)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun commentsBottomSheet(model: PostResponse.Body.Data) {
        val dialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        mBottomSheetBinding = ItemBottomSheetCommentBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(mBottomSheetBinding!!.root)
        dialog.show()
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        dialog.behavior.peekHeight = resources.displayMetrics.heightPixels / 2

        mBottomSheetBinding!!.root.setOnApplyWindowInsetsListener { _, insets ->
            val imeHeight = insets.getInsets(WindowInsets.Type.ime()).bottom

            // Adjust only the EditText layout
            mBottomSheetBinding!!.etMessage.translationY = if (imeHeight > 0) {
                -imeHeight.toFloat()
            } else {
                0f
            }

            insets
        }

        mBottomSheetBinding!!.ivCross.setOnClickListener {
            dialog.dismiss()
        }
        observeTextChanges(mBottomSheetBinding!!.etMessage)

        mentionList.clear()
        mentionAdapter = MentionAdapter(mentionList) { user ->
            val text = mBottomSheetBinding!!.etMessage.text.toString()
            val cursorPos = mBottomSheetBinding!!.etMessage.selectionStart
            val atIndex = text.lastIndexOf("@", cursorPos - 1)

            if (atIndex != -1) {
                val newText = text.substring(0, atIndex) + "@${user.username} " +
                        text.substring(cursorPos)
                mBottomSheetBinding!!.etMessage.setText(newText)
                mBottomSheetBinding!!.etMessage.setSelection(atIndex + user.username.length + 2)
            }
            mBottomSheetBinding!!.rvMentions.gone()
        }
        mBottomSheetBinding!!.rvMentions.adapter = mentionAdapter

        setCommentAdapter(model)
        resetCommentPage = true
        getCommentList(model)

        dialog.setCancelable(true)
        dialog.setContentView(mBottomSheetBinding!!.root)
        dialog.show()

        with(mBottomSheetBinding!!) {
            ivImage.loadImage(
                com.live.azurah.retrofit.ApiConstants.IMAGE_BASE_URL + getPreference("image",""),
                R.drawable.profile_icon
            )
            ivSend.setOnClickListener {
                if (containsBannedWord(mBottomSheetBinding!!.etMessage.text.toString().trim())) {
                    showCustomSnackbar(
                        this@MyPostsActivity,
                        it.rootView,
                        "Your comment contains banned or inappropriate words. Please remove them before posting."
                    )
                    return@setOnClickListener
                }

                val rawComment = mBottomSheetBinding!!.etMessage.text.toString().trim()
                val cleanedComment = rawComment.replace(Regex("@\\w+"), "").trim()

                if (cleanedComment.isEmpty()) {
                    showCustomSnackbar(
                        this@MyPostsActivity,
                        it.rootView,
                        "Please enter comment"
                    )
                    return@setOnClickListener
                }

                addComment(model.id ?: 0, parentId,model)
                username = ""
                etMessage.setText("")
            }
        }
    }

    private fun addComment(postId: Int, parentId: Int, model: PostResponse.Body.Data) {
        val map = HashMap<String, String>()
        map["post_id"] = postId.toString()
        map["description"] = mBottomSheetBinding!!.etMessage.text.toString().trim()
        if (parentId != 0) {
            map["parent_transaction_id"] = parentId.toString()
        }
        /*if (taggedId != 0) {
            map["tagged_user_id"] = taggedId.toString()
        }*/

        val comment = mBottomSheetBinding!!.etMessage.text.toString().trim()

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
        viewModel.addPostComment(map, this).observe(this) { value ->
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
                                        Log.d("dflksdhfd", mainPos.toString())
                                        (commentList[mainPos].replies ?: ArrayList()).add(
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
                                this@MyPostsActivity.parentId = 0
                                taggedId = 0
                                mainPos = -1
                                if (commentList.isEmpty()) {
                                    mBottomSheetBinding?.tvNoDataFound?.visible()
                                } else {
                                    mBottomSheetBinding?.tvNoDataFound?.gone()
                                }

                                model.comment_count = (model.comment_count ?:0)+1
                                postAdapter.notifyDataSetChanged()
                                if ((model.comment_count ?: 0) > 0){
                                    mBottomSheetBinding?.tvTotalComments?.text = buildString {
                                        append("Comments (")
                                        append(formatCount(model.comment_count ?: 0))
                                        append(")")
                                    }
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
    }

    private fun getCommentList(model: PostResponse.Body.Data) {
        isApiRunning = true
        if (resetCommentPage) {
            currentCommentPage = 1
        }
        val map = HashMap<String, String>()
        map["limit"] = "15"
        map["page"] = currentCommentPage.toString()
        map["post_id"] = model.id.toString()
        viewModel.postCommentList(map, this).observe(this) { value ->
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
                                    mBottomSheetBinding?.tvNoDataFound?.visible()
                                } else {
                                    mBottomSheetBinding?.tvNoDataFound?.gone()
                                    mBottomSheetBinding?.tvTotalComments?.text = buildString {
                                        append("Comments (")
                                        append(formatCount(res?.total_count ?: 0))
                                        append(")")
                                    }
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
    }

    private fun setCommentAdapter(model: PostResponse.Body.Data) {
        mBottomSheetBinding?.rvComments?.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    if (currentCommentPage <= totalCommentPageCount && !isApiRunning) {
                        resetCommentPage = false
                        showDialog = false
                        getCommentList(model)
                    }
                }
            }
        })

        commentList.clear()
        commentAdapter = CommentAdapter(this, commentList)
        mBottomSheetBinding!!.rvComments.adapter = commentAdapter
        commentAdapter?.menuListener = { pos, view, repPos, mainComment, item,notificationId ->
            if (model.user?.id.toString() == getPreference("id","")){
                if (item != null){
                    if (item.user?.id.toString() == getPreference("id","")){
                        setPopUpCommentWindowDelete(view,pos,repPos,item.id.toString(),notificationId)
                    }else{
                        setPopUpWindowReportDelete(view,pos,repPos,item.id.toString(),model,mainComment,item,notificationId)
                    }
                }else{
                    if (mainComment.user?.id.toString() == getPreference("id","")){
                        setPopUpCommentWindowDelete(view,pos,repPos,mainComment.id.toString(),notificationId)
                    }else{
                        setPopUpWindowReportDelete(view,pos,repPos,mainComment.id.toString(),model,mainComment,null,notificationId)
                    }
                }
            }else{
                if (item != null){
                    if (item.user?.id.toString() == getPreference("id","")){
                        setPopUpCommentWindowDelete(view,pos,repPos,item.id.toString(),notificationId)
                    }else{
                        setPopUpComment(view,mainComment,item)
                    }
                }else{
                    if (mainComment.user?.id.toString() == getPreference("id","")){
                        setPopUpCommentWindowDelete(view,pos,repPos,mainComment.id.toString(),notificationId)
                    }else{
                        setPopUpComment(view,mainComment,null)
                    }
                }
            }
        }

        commentAdapter?.onLikeUnlike = { pos, id, status, postId ->
            val map = HashMap<String, String>()
            map["post_id"] = postId
            map["post_comment_id"] = id
            map["status"] = status
            viewModel.postCommentLikeUnlike(map, this).observe(this) { value ->
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
        }

        commentAdapter?.onCommentEdit = { pos, repPos, commentId, desc,notificationId ->
            showMessageDialog(pos, repPos, commentId, model.id.toString(), desc,notificationId)
        }

        commentAdapter?.replyListener = { pos, view, item, replyModel, replyPosition ->
            mBottomSheetBinding!!.etMessage.isFocusable = true
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
            mBottomSheetBinding!!.etMessage.setText(mentionText)
            mBottomSheetBinding!!.etMessage.setSelection(mentionText.length)

            // Setup the text watcher for this edit text
//            setupMentionTextWatcher(mBottomSheetBinding!!.etMessage)

            mBottomSheetBinding!!.etMessage.showKeyboard()
        }

      /*  commentAdapter?.replyListener = { pos, view, item, replyModel, replyPosition ->
            mBottomSheetBinding!!.etMessage.isFocusable = true
            parentId = item.id ?: 0
            mainPos = pos
            if (replyModel?.user != null) {
                taggedId = replyModel.user.id ?: 0
                username = replyModel.user.username.let { "@$it " }
                mBottomSheetBinding!!.etMessage.setText(username)
                mBottomSheetBinding!!.etMessage.setSelection(mBottomSheetBinding!!.etMessage.text.toString().length)
            } else {
                username = item.user?.username.let { "@$it " }
                taggedId = item.user?.id ?: 0
                mBottomSheetBinding!!.etMessage.setText(username)
                mBottomSheetBinding!!.etMessage.setSelection(mBottomSheetBinding!!.etMessage.text.toString().length)

            }
            mBottomSheetBinding!!.etMessage.showKeyboard()
        }*/
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
                // Handle mention search (your existing code)
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
                            mBottomSheetBinding!!.rvMentions.visible()
                            mentionAdapter?.notifyDataSetChanged()
                        } else {
                            mBottomSheetBinding!!.rvMentions.gone()
                        }
                    } else {
                        mBottomSheetBinding!!.rvMentions.gone()
                    }
                } else {
                    mBottomSheetBinding!!.rvMentions.gone()
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
                    mBottomSheetBinding!!.ivSend.backgroundTintList =
                        ContextCompat.getColorStateList(this@MyPostsActivity, R.color.blue)
                    mBottomSheetBinding!!.ivSend.isEnabled = true
                } else {
                    mBottomSheetBinding!!.ivSend.backgroundTintList =
                        ContextCompat.getColorStateList(this@MyPostsActivity, R.color.button_grey)
                    mBottomSheetBinding!!.ivSend.isEnabled = false
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

                    val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this@MyPostsActivity, R.color.blue))
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

                        // Check if cursor is positioned right after a mention (at the space after mention)
                        val isAfterMention = mentions.any { mention ->
                            currentSelection == mention.range.last + 2 && // +2 because range is 0-based and we want position after space
                                    currentText.getOrNull(mention.range.last + 1) == ' '
                        }

                        if (isAfterMention) {
                            // Find the mention that ends right before cursor
                            val mentionToDelete = mentions.find { mention ->
                                currentSelection == mention.range.last + 2
                            }

                            mentionToDelete?.let { mention ->
                                // Delete the entire mention including the @ symbol and trailing space
                                s.delete(mention.range.first, mention.range.last + 2) // +2 to include space
                                editText.setSelection(mention.range.first)
                                return
                            }
                        }

                        // Check if cursor is at the end of a mention (right after the last character)
                        val isAtMentionEnd = mentions.any { mention ->
                            currentSelection == mention.range.last + 1
                        }

                        if (isAtMentionEnd) {
                            // Find the mention that ends at cursor position
                            val mentionToDelete = mentions.find { mention ->
                                currentSelection == mention.range.last + 1
                            }

                            mentionToDelete?.let { mention ->
                                // Delete the entire mention including the @ symbol
                                s.delete(mention.range.first, mention.range.last + 1)
                                editText.setSelection(mention.range.first)
                                return
                            }
                        }

                        // Check if cursor is within a mention
                        val mentionContainingCursor = mentions.find { mention ->
                            currentSelection > mention.range.first && currentSelection <= mention.range.last + 1
                        }

                        mentionContainingCursor?.let { mention ->
                            // Delete the entire mention
                            s.delete(mention.range.first, mention.range.last + 1)
                            editText.setSelection(mention.range.first)
                            return
                        }

                        // Check if backspace was pressed right after @ symbol (deleting the @)
                        val charBeforeCursor = currentText.getOrNull(currentSelection - 1)
                        if (charBeforeCursor == '@') {
                            // Find if this @ is the start of a potential mention
                            val mentionStartingHere = mentions.find { mention ->
                                mention.range.first == currentSelection - 1
                            }

                            mentionStartingHere?.let { mention ->
                                // Delete the entire mention
                                s.delete(mention.range.first, mention.range.last + 1)
                                editText.setSelection(mention.range.first)
                            }
                        }
                    }
                }
            }
        })
    }

    // Custom SpannableFactory to preserve spans during text changes

    inner class CustomSpannableFactory : Spannable.Factory() {
        fun newEditable(source: CharSequence): Editable {
            return SpannableStringBuilder(source)
        }
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
                              mBottomSheetBinding!!.rvMentions.visible()
                              mentionAdapter?.notifyDataSetChanged()
                          } else {
                              mBottomSheetBinding!!.rvMentions.gone()
                          }
                      } else {
                          mBottomSheetBinding!!.rvMentions.gone()
                      }
                  } else {
                      mBottomSheetBinding!!.rvMentions.gone()
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
                      mBottomSheetBinding!!.ivSend.backgroundTintList =
                          ContextCompat.getColorStateList(requireContext(), R.color.blue)
                      mBottomSheetBinding!!.ivSend.isEnabled = true
                  } else {
                      mBottomSheetBinding!!.ivSend.backgroundTintList =
                          ContextCompat.getColorStateList(requireContext(), R.color.button_grey)
                      mBottomSheetBinding!!.ivSend.isEnabled = false
                  }
              }
          })*/
    }

    /*private fun observeTextChanges(etMessage: EditText) {
        etMessage.addTextChangedListener(object : TextWatcher {
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
                            mBottomSheetBinding!!.rvMentions.visible()
                            mentionAdapter?.notifyDataSetChanged()
                        } else {
                            mBottomSheetBinding!!.rvMentions.gone()
                        }
                    } else {
                        mBottomSheetBinding!!.rvMentions.gone()
                    }
                } else {
                    mBottomSheetBinding!!.rvMentions.gone()
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
                    mBottomSheetBinding!!.ivSend.backgroundTintList =
                        ContextCompat.getColorStateList(this@MyPostsActivity, R.color.blue)
                    mBottomSheetBinding!!.ivSend.isEnabled = true
                } else {
                    mBottomSheetBinding!!.ivSend.backgroundTintList =
                        ContextCompat.getColorStateList(this@MyPostsActivity, R.color.button_grey)
                    mBottomSheetBinding!!.ivSend.isEnabled = false
                }
            }
        })
    }*/

    private fun setPopUpCommentWindowDelete(view1: View, pos: Int, repPos: Int, commentId: String,notificationId:String) {
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
                ContextCompat.getColorStateList(this@MyPostsActivity, R.color.black)
            tvNotDone.text = "Delete Comment"
            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                deletePost(pos, repPos, commentId,notificationId)

            }
        }

        myPopupWindow.showAsDropDown(view1, 0, -80)
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
            deletePost(pos, repPos, commentId,notificationId)

        }

        if (repPos != -1){
            val comment= removeMentionIfMatches(desc ?: "", commentList[pos].replies[repPos].user?.username ?: "")
            resetBinding.tvDescription.setText(comment)
        }else{
            val comment= removeMentionIfMatches(desc ?: "", commentList[pos].user?.username ?: "")
            resetBinding.tvDescription.setText(comment)
        }
        resetBinding.tvReplies.setOnClickListener {
            if (containsBannedWord(resetBinding.tvDescription.text.toString().trim())) {
                showCustomSnackbar(
                    this,
                    it,
                    "Your comment contains banned or inappropriate words. Please remove them before posting."
                )
                return@setOnClickListener
            }
            customDialog.dismiss()
            val map = HashMap<String, String>()
            map["post_id"] = postId
            map["description"] = resetBinding.tvDescription.text.toString().trim()
            map["post_comment_id"] = commentId
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

            viewModel.postCommentEdit(map, this).observe(this) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is CommentCommonResponse -> {
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
                        ContextCompat.getColorStateList(this@MyPostsActivity, R.color.blue)
                    resetBinding.tvReplies.isEnabled = true
                } else {
                    resetBinding.tvReplies.backgroundTintList =
                        ContextCompat.getColorStateList(this@MyPostsActivity, R.color.button_grey)
                    resetBinding.tvReplies.isEnabled = false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        })
    }*/

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
            deletePost(pos, repPos, commentId,notificationId)
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

        // Set the initial text
        resetBinding.tvDescription.setText(initialComment)

        // Apply mention styling to existing mentions in the initial text
        applyInitialMentionStyling(resetBinding.tvDescription, initialComment)

        resetBinding.tvReplies.setOnClickListener {
            if (containsBannedWord(resetBinding.tvDescription.text.toString().trim())) {
                showCustomSnackbar(
                    this,
                    it,
                    "Your comment contains banned or inappropriate words. Please remove them before posting."
                )
                return@setOnClickListener
            }

            val rawComment = resetBinding.tvDescription.text.toString().trim()
            val cleanedComment = rawComment.replace(Regex("@\\w+"), "").trim()

            if (cleanedComment.isEmpty()) {
                showCustomSnackbar(
                    this,
                    it,
                    "Please enter comment"
                )
                return@setOnClickListener
            }

            customDialog.dismiss()
            val map = HashMap<String, String>()
            map["post_id"] = postId
            map["description"] = resetBinding.tvDescription.text.toString().trim()
            map["post_comment_id"] = commentId

            val newComment = resetBinding.tvDescription.text.toString().trim()
            val mentionPattern = Regex("@([A-Za-z0-9_]+)")
            val newMentions = mentionPattern.findAll(newComment)
                .map { it.groupValues[1] }
                .toList()

            if (newMentions.isNotEmpty()) {
                map["tagged_user_id"] = newMentions.joinToString(separator = ",")
            }

            for ((key, value) in map) {
                Log.d("MapValues", "$key = $value")
            }
            viewModel.postCommentEdit(map, this).observe(this) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                        LoaderDialog.dismiss()
                        when (value.data) {
                            is CommentCommonResponse -> {
                                Log.d("dsgdgdg","afdgGDSgds")
                                val res = value.data.body
                                if (repPos != -1) {
                                    commentList[pos].replies[repPos] = CommentResponse.Replies(
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
        customDialog.show()

        val mentionList = mutableListOf<Follower>()
        val mentionAdapter = MentionAdapter(mentionList) { user ->
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

        resetBinding.rvMentions.adapter = mentionAdapter

        // Setup the mention text watcher for the edit dialog
        setupEditDialogMentionTextWatcher(resetBinding.tvDescription, resetBinding.rvMentions, mentionList, mentionAdapter)

        // Enable/disable reply button based on initial text
        resetBinding.tvReplies.isEnabled = initialComment.isNotEmpty()
        resetBinding.tvReplies.backgroundTintList = ContextCompat.getColorStateList(
            this,
            if (initialComment.isNotEmpty()) R.color.blue else R.color.button_grey
        )
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

    private fun setupEditDialogMentionTextWatcher(
        editText: EditText,
        mentionsRecyclerView: RecyclerView,
        mentionList: MutableList<Follower>,
        mentionAdapter: MentionAdapter
    ) {
        val mentionPattern = Regex("@([A-Za-z0-9_]+)")

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
                val replyButton = (editText.rootView.findViewById<View>(R.id.tvReplies) as? android.widget.Button)
                replyButton?.let { button ->
                    if (s.toString().isNotEmpty()) {
                        button.backgroundTintList = ContextCompat.getColorStateList(this@MyPostsActivity, R.color.blue)
                        button.isEnabled = true
                    } else {
                        button.backgroundTintList = ContextCompat.getColorStateList(this@MyPostsActivity, R.color.button_grey)
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
                    val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this@MyPostsActivity, R.color.blue))
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


    private fun deletePost(pos: Int, repPos: Int, commentId: String,notificationId: String) {
        viewModel.postCommentDelete(commentId,this,notificationId).observe(this) { value ->
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
                                mBottomSheetBinding!!.tvNoDataFound.gone()
                            } else {
                                mBottomSheetBinding!!.tvNoDataFound.visible()
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

    private fun sureDialog(model: PostResponse.Body.Data) {
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
            append(model.user?.username)
            append("?")
        }
        confirmationBinding.ivDel.visibility = View.GONE
        confirmationBinding.ivDel.setImageResource(R.drawable.block_user_icon)
        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            val map = HashMap<String, String>()
            map["block_by"] = getPreference("id", "")
            map["block_to"] = model.user?.id.toString()
            map["status"] = "1"
            viewModel.userBlock(map, this).observe(this) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is CommonResponse -> {
                                val res = value.data.body
                                showCustomSnackbar(
                                    this,
                                    binding.root,
                                    model.user?.first_name + " " + model.user?.last_name + "has been blocked."
                                )
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
        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    private fun showShareDialog(pos: Int,id:String) {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val resetBinding = DialogShareBinding.inflate(layoutInflater)
        customDialog.setContentView(resetBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1000)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        resetBinding.tvTitle.text = "Share Post"
        resetBinding.tvMessage.visibility = View.GONE
        resetBinding.btnCopy.visibility = View.VISIBLE
        resetBinding.btnLink.visibility = View.VISIBLE

        resetBinding.btnLink.text = buildString {
            append("https://app.azrius.co.uk/common_api/deepLinking/post?post_id=")
            append(id)
        }
        resetBinding.btnCopy.setOnClickListener {
            customDialog.dismiss()
            copyToClipboard("https://app.azrius.co.uk/common_api/deepLinking/post?post_id=$id")
        }
        resetBinding.ivCross.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()

    }


    private fun showSharePrivateDialog() {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val resetBinding = DialogSharePrivateBinding.inflate(layoutInflater)
        customDialog.setContentView(resetBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        resetBinding.tvTitle.text = "Share Post"
        resetBinding.tvMessage.text = "This post is from a private account and can't be shared."

        resetBinding.ivCross.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    private fun getPosts() {
        isApiRunning = true
        if (resetPage) {
            currentPage = 1
        }
        val map = HashMap<String, String>()
        map["page"] = currentPage.toString()
        map["limit"] = "10"
        if (type == "1") {
            if (otherUserId.isNotEmpty()) {
                map["user_id"] = otherUserId
            } else {
                map["user_id"] = getPreference("id", "")
            }
            viewModel.getPostList(map, this).observe(this, this)
        } else if (type == "2") {
            map["search_string"] = search
            viewModel.getHomeSearch(map, this).observe(this, this)

        } else {
            viewModel.getPostBookmark(map, this).observe(this, this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
               LoaderDialog.dismiss()
                binding.swipeRefreshLayout.isRefreshing = false
                when (value.data) {
                    is PostResponse -> {
                        with(binding) {
                            val res = value.data.body
                            if (resetPage) {
                                list.clear()
                            }
                            list.addAll(res?.data ?: ArrayList())
                            setVideoHelper()
                            postAdapter.notifyDataSetChanged()
                            if (list.isNotEmpty()) {
                                currentPage = (res?.current_page ?: 0) + 1
                                totalPageCount = res?.total_pages ?: 0
                                binding.tvNoDataFound.gone()

                            } else {
                                binding.tvNoDataFound.visible()
                            }
                        }
                    }

                    is SavedPostResponse -> {
                        with(binding) {
                            val res = value.data.body
                            val newMappedList = res?.data?.map {
                                PostResponse.Body.Data(
                                    id = it?.post?.id,
                                    comment_count = it?.post?.comment_count,
                                    created = it?.post?.created,
                                    created_at = it?.post?.created_at,
                                    description = it?.post?.description,
                                    is_bookmark = it?.post?.is_bookmark,
                                    is_like = it?.post?.is_like,
                                    like_count = it?.post?.like_count,
                                    post_images = it?.post?.post_images?.map {
                                        PostResponse.Body.Data.PostImage(
                                            id = it?.id,
                                            image = it?.image,
                                            image_thumb = it?.image_thumb,
                                            post_id = it?.post_id,
                                            type = it?.type
                                        )
                                    },
                                    user_id = it?.post?.user?.id,
                                    user = PostResponse.Body.Data.User(
                                        first_name = it?.post?.user?.first_name,
                                        last_name = it?.post?.user?.last_name,
                                        username = it?.post?.user?.username,
                                        id = it?.post?.user?.id,
                                        image = it?.post?.user?.image,
                                        image_thumb = it?.post?.user?.image_thumb,
                                    )
                                )
                            }
                            if (resetPage) {
                                list.clear()
                            }
                            list.addAll(newMappedList ?: ArrayList())
                            setVideoHelper()
                            postAdapter.notifyDataSetChanged()
                            if (list.isNotEmpty()) {
                                currentPage = (res?.current_page ?: 0) + 1
                                totalPageCount = res?.total_pages ?: 0
                                binding.tvNoDataFound.gone()

                            } else {
                                binding.tvNoDataFound.visible()
                            }
                        }
                    }

                    is HomeSearchResposne -> {
                        with(binding) {
                            val res = value.data.body?.posts
                            val users = value.data.body?.users
                            val newMappedList = res?.data?.map {
                                PostResponse.Body.Data(
                                    id = it.id,
                                    comment_count = it.comment_count,
                                    created = it.created,
                                    created_at = it.created_at,
                                    description = it.description,
                                    is_bookmark = it.is_bookmark,
                                    is_like = it.is_like,
                                    like_count = it.like_count,
                                    post_images = it.post_images?.map {
                                        PostResponse.Body.Data.PostImage(
                                            id = it?.id,
                                            image = it?.image,
                                            image_thumb = it?.image_thumb,
                                            post_id = it?.post_id,
                                            type = it?.type
                                        )
                                    },
                                    user_id = it.user_id,
                                    user = PostResponse.Body.Data.User(
                                        first_name = it.user?.first_name,
                                        last_name = it.user?.last_name,
                                        username = it.user?.username,
                                        id = it.user?.id,
                                        image = it.user?.image,
                                        image_thumb = it.user?.image_thumb,
                                    )
                                )
                            }
                            if (resetPage) {
                                list.clear()
                            }

                            list.addAll(newMappedList ?: ArrayList())
                            setVideoHelper()
                            postAdapter.notifyDataSetChanged()
                            if (list.isNotEmpty()) {
                                currentPage = (res?.current_page ?: 0) + 1
                                totalPageCount = res?.total_pages ?: 0
                                binding.tvNoDataFound.gone()

                            } else {
                                binding.tvNoDataFound.visible()
                            }

                        }
                    }
                }
            }

            Status.LOADING -> {
                if (showDialog) {
                    LoaderDialog.show(this)
                }
            }

            Status.ERROR -> {
                isApiRunning = false
                binding.swipeRefreshLayout.isRefreshing = false
               LoaderDialog.dismiss()
                showCustomSnackbar(this, binding.root, value.message.toString())
            }
        }
    }

    private fun setVideoHelper() {
        if (videoAutoPlayHelper == null) {
            videoAutoPlayHelper = VideoMultiplePostMyAutoPlayHelper(
                binding.rvPosts,
                list,
                this@MyPostsActivity,
                this
            )
            videoAutoPlayHelper?.startObserving()

        } else {
            videoAutoPlayHelper!!.setList(list)
        }
        videoAutoPlayHelper!!.liseSize(list)
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    override fun onPause() {
        super.onPause()
        videoAutoPlayHelper?.pausePlayer()

    }

    override fun onDestroy() {
        super.onDestroy()
        videoAutoPlayHelper?.removePlayer()
    }

    override fun onResume() {
        super.onResume()
        getList()
        videoAutoPlayHelper?.restartPlayer()

    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(binding.clSuggestion.id, fragment)
            .addToBackStack(null).commit()
    }

    private fun setPopUpWindowDelete(view1: View, model: PostResponse.Body.Data, pos: Int) {
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
                ContextCompat.getColorStateList(this@MyPostsActivity, R.color.black)
            tvNotDone.text = "Delete Post"
            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                sureDeleteDialog(model, pos)
            }
        }

        myPopupWindow.showAsDropDown(view1, 0, -80)
    }

    private fun setPopUpWindowReportDelete(
        view1: View,
        pos: Int,
        repPos: Int,
        commentId: String,
        model: PostResponse.Body.Data,
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
        with(view){
            clDeleteUser.setOnClickListener {
                myPopupWindow.dismiss()
                deletePost(pos,repPos,commentId, notificationId)
            }

            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@MyPostsActivity, ReportUserActivity::class.java).apply {
                    putExtra("from", "postComment")
                    if (item != null) {
                        putExtra("id", item.id.toString())
                        putExtra("reportedTo", item.user_id.toString())
                        putExtra("postId", item.post_id.toString())
                        putExtra("username", item.user?.username.toString())

                    }else{
                        putExtra("id", mainComment.id.toString())
                        putExtra("reportedTo", mainComment.user_id.toString())
                        putExtra("postId", mainComment.post_id.toString())
                        putExtra("username", mainComment.user?.username.toString())
                    }

                })
            }
            ivUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@MyPostsActivity, ReportUserActivity::class.java).apply {
                    putExtra("from", "postComment")
                    if (item != null) {
                        putExtra("id", item.id.toString())
                        putExtra("reportedTo", item.user_id.toString())
                        putExtra("postId", item.post_id.toString())
                        putExtra("username", item.user?.username.toString())

                    }else{
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

    private fun sureDeleteDialog(model: PostResponse.Body.Data, pos: Int) {
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

        confirmationBinding.tvMsg.text = "This action cannot be undone"
        confirmationBinding.tvUsernameTaken.text = "Are you sure you want to delete this post?"
        confirmationBinding.tvMsg.visibility = View.GONE
        confirmationBinding.ivDel.visibility = View.VISIBLE
        confirmationBinding.ivDel.setImageResource(R.drawable.del_icon)
        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            viewModel.postDelete(model.id.toString(), this).observe(this) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is CommonResponse -> {
                                val res = value.data.body
                                list.removeAt(pos)
                                postAdapter.notifyDataSetChanged()

                                setVideoHelper()
                                showCustomSnackbar(
                                    this,
                                    binding.root,
                                    "Post deleted successfully."
                                )
                                if (list.isNotEmpty()) {
                                    binding.tvNoDataFound.gone()
                                } else {
                                    binding.tvNoDataFound.visible()
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
        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
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

    override fun listener() {
        if (currentPage <= totalPageCount && !isApiRunning) {
            resetPage = false
            showDialog = false
            getPosts()
        }
    }


}