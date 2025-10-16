package com.live.azurah.activity

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.appcompat.app.AppCompatActivity
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
import com.live.azurah.adapter.PostDetailAdapter
import com.live.azurah.autoPlay.ScrollListener
import com.live.azurah.autoPlay.VideoMultiplePostMyAutoPlayHelper
import com.live.azurah.databinding.ActivityViewPostBinding
import com.live.azurah.databinding.DialogEditMessageBinding
import com.live.azurah.databinding.DialogShareBinding
import com.live.azurah.databinding.DialogSharePrivateBinding
import com.live.azurah.databinding.ItemBottomSheetCommentBinding
import com.live.azurah.databinding.MenuReportBinding
import com.live.azurah.databinding.MenuReportCommentBinding
import com.live.azurah.databinding.MenuReportDeleteDialogBinding
import com.live.azurah.databinding.MenuReportMemberBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.fragment.SuggetionForYouFragment
import com.live.azurah.fragment.UserLikesFragment
import com.live.azurah.model.CommentCommonResponse
import com.live.azurah.model.CommentResponse
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.PostCommentListResposne
import com.live.azurah.model.PostResponse
import com.live.azurah.model.ViewPostResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
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
class ViewPostActivity : AppCompatActivity(), Observer<Resource<Any>>, ScrollListener {
    private lateinit var binding: ActivityViewPostBinding
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
    private var currentPage = 0
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    private var showDialog = true
    private var currentCommentPage = 1
    private var totalCommentPageCount = 0
    private var resetCommentPage = false
    private var otherUserId = ""
    private var postId = ""
    private var from = ""

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPostBinding.inflate(layoutInflater)
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
        postId = intent.getStringExtra("postId") ?: ""
        from = intent.getStringExtra("from") ?: ""
        initListener()
        setAdapter()
        getPosts()
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
            setPopUpWindow(view, model)
        /*    if (type == "1" && otherUserId.isEmpty()) {
                if (getPreference("id", "") == model.user?.id.toString()) {*/
//                    setPopUpWindowDelete(view, model, pos)
              /*  } else {
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

            }*/
        }

        postAdapter.shareListener = { pos,model->
            showCustomSnackbar(this,binding.root,"Share post coming soon.")

           /* if (model.user?.profile_type == 2){
                showShareDialog(pos,model.id.toString())
            }else{
                if (model.user?.id.toString() == getPreference("id","")){
                    showShareDialog(pos,model.id.toString())
                }else{
                    showSharePrivateDialog()
                }

            }*/
        }

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
                startActivity(Intent(this@ViewPostActivity, ReportPostActivity::class.java).apply {
                    putExtra("from", "post")
                    putExtra("id", model.id.toString())
                    putExtra("reportedTo", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())
                })
            }
            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@ViewPostActivity, ReportUserActivity::class.java).apply {
                    putExtra("from", "post")
                    putExtra("id", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())

                })
            }

            ivReportUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@ViewPostActivity, ReportPostActivity::class.java).apply {
                    putExtra("from", "post")
                    putExtra("id", model.id.toString())
                    putExtra("reportedTo", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())
                })
            }

            ivUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@ViewPostActivity, ReportUserActivity::class.java).apply {
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
                startActivity(Intent(this@ViewPostActivity, ReportUserActivity::class.java).apply {
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
                startActivity(Intent(this@ViewPostActivity, ReportUserActivity::class.java).apply {
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
        if (taggedId != 0) {
            map["tagged_user_id"] = taggedId.toString()
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
                                        (commentList[mainPos].replies).add(
                                          CommentResponse.Replies(
                                                user = it.user,
                                                is_like = it.is_like,
                                                like_count = it.like_count,
                                                description = it.description,
                                                created_at = it.created_at,
                                                parent_transaction_id = it.parent_transaction_id,
                                                post_id = it.post_id,
                                                notification_id = it.notification_id,
                                                user_id = it.user_id,
                                                id = it.id,
                                                tagged_user_data = it.tagged_user_data
                                            )
                                        )
                                    }
                                    commentAdapter?.notifyItemChanged(mainPos)
                                }
                                this@ViewPostActivity.parentId = 0
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
                        setPopUpWindowReportDelete(view,pos,repPos,item.id.toString(),mainComment,item,notificationId)
                    }
                }else{
                    if (mainComment.user?.id.toString() == getPreference("id","")){
                        setPopUpCommentWindowDelete(view,pos,repPos,mainComment.id.toString(),notificationId)
                    }else{
                        setPopUpWindowReportDelete(view,pos,repPos,mainComment.id.toString(),mainComment,null,notificationId)
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
            if (replyModel?.user != null) {
                taggedId = replyModel.user.id ?: 0
                username = replyModel.user.username.let { "@$it " }
                setColoredUsername(
                    mBottomSheetBinding!!.etMessage,
                    replyModel.user.username ?: ""
                )
            } else {
                username = item.user?.username.let { "@$it " }
                taggedId = item.user?.id ?: 0
                setColoredUsername(
                    mBottomSheetBinding!!.etMessage,
                    item.user?.username ?: ""
                )
            }
            mBottomSheetBinding!!.etMessage.showKeyboard()

        }
    }

    private fun observeTextChanges(etMessage: EditText) {
        etMessage.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                s.let {
                    if (parentId != 0 && username.trim().isNotEmpty()) {
                        val modifiedText = it.split(" ").firstOrNull { it.startsWith("@") } ?: ""
                        if (modifiedText.trim() != username.trim()) {
                            isUpdating = true
                            val outputText = s.replaceFirst("@[^\\s]+".toRegex(), "")
                            username = ""
                            parentId = 0
                            mainPos = -1
                            taggedId = 0
                            mBottomSheetBinding!!.etMessage.setText(outputText)
                            mBottomSheetBinding!!.etMessage.setSelection(0) // Set cursor at the start
                            isUpdating = false
                            Log.d("sdgsdgsdg", "ksdfsdjfhsdff")
                        }

                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isNotEmpty()) {
                    mBottomSheetBinding!!.ivSend.backgroundTintList =
                        ContextCompat.getColorStateList(this@ViewPostActivity, R.color.blue)
                    mBottomSheetBinding!!.ivSend.isEnabled = true
                } else {
                    mBottomSheetBinding!!.ivSend.backgroundTintList =
                        ContextCompat.getColorStateList(this@ViewPostActivity, R.color.button_grey)
                    mBottomSheetBinding!!.ivSend.isEnabled = false
                }
            }
        })
    }

    private fun setPopUpCommentWindowDelete(view1: View, pos: Int, repPos: Int, commentId: String,notificationId: String) {
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
                ContextCompat.getColorStateList(this@ViewPostActivity, R.color.black)
            tvNotDone.text = "Delete Comment"
            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                deletePost(pos, repPos, commentId,notificationId)

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
        notificationId:String
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
                startActivity(Intent(this@ViewPostActivity, ReportUserActivity::class.java).apply {
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
                startActivity(Intent(this@ViewPostActivity, ReportUserActivity::class.java).apply {
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

        if (repPos != -1){
            val comment= removeMentionIfMatches(desc ?: "", commentList[pos].replies[repPos].user?.username ?: "")
            resetBinding.tvDescription.setText(comment)
        }else{
            val comment= removeMentionIfMatches(desc ?: "", commentList[pos].user?.username ?: "")
            resetBinding.tvDescription.setText(comment)
        }

        resetBinding.tvReplies.setOnClickListener {
            customDialog.dismiss()
            val map = HashMap<String, String>()
            map["post_id"] = postId
            map["description"] = resetBinding.tvDescription.text.toString().trim()
            map["post_comment_id"] = commentId
            viewModel.postCommentEdit(map, this).observe(this) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is CommonResponse -> {
                                val res = value.data.body
                                if (repPos != -1) {
                                    commentList[pos].replies?.get(repPos)?.description =
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
                resetBinding.tvReplies.stateListAnimator = null
                if (s.toString().isNotEmpty()) {
                    resetBinding.tvReplies.backgroundTintList =
                        ContextCompat.getColorStateList(this@ViewPostActivity, R.color.blue)
                    resetBinding.tvReplies.isEnabled = true
                } else {
                    resetBinding.tvReplies.backgroundTintList =
                        ContextCompat.getColorStateList(this@ViewPostActivity, R.color.button_grey)
                    resetBinding.tvReplies.isEnabled = false
                }
            }
        })
    }


    private fun deletePost(pos: Int, repPos: Int, commentId: String,notificationId: String) {
        viewModel.postCommentDelete(commentId, this,notificationId).observe(this) { value ->
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
            append("https://app.azrius.co.uk/admin/public-post/")
            append(id)
        }
        resetBinding.btnCopy.setOnClickListener {
            customDialog.dismiss()
            copyToClipboard("https://app.azrius.co.uk/admin/public-post/"+id)
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
        viewModel.getPostView(postId, this).observe(this, this)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
               LoaderDialog.dismiss()
                when (value.data) {
                    is ViewPostResponse -> {
                        with(binding) {
                            val res = value.data.body
                            res?.let {
                               val post = PostResponse.Body.Data(
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
                                list.add(post)
                            }
                            setVideoHelper()
                            postAdapter.notifyDataSetChanged()

                            if (from == "comment") {
                                commentsBottomSheet(list.first())
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
                this,
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
                ContextCompat.getColorStateList(this@ViewPostActivity, R.color.black)
            tvNotDone.text = "Delete Post"
            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                sureDeleteDialog(model, pos)
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

    override fun listener() {

    }


}