package com.live.azurah.fragment

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.live.azurah.R
import com.live.azurah.activity.HomeActivity
import com.live.azurah.activity.ReportPostActivity
import com.live.azurah.activity.ReportUserActivity
import com.live.azurah.adapter.CommentAdapter
import com.live.azurah.adapter.Follower
import com.live.azurah.adapter.MentionAdapter
import com.live.azurah.adapter.PostDetailAdapter
import com.live.azurah.autoPlay.ScrollListener
import com.live.azurah.autoPlay.VideoMultiplePostAutoPlayHelper
import com.live.azurah.databinding.DialogEditMessageBinding
import com.live.azurah.databinding.DialogShareBinding
import com.live.azurah.databinding.DialogSharePrivateBinding
import com.live.azurah.databinding.FragmentHomeBinding
import com.live.azurah.databinding.ItemBottomSheetCommentBinding
import com.live.azurah.databinding.MenuReportBinding
import com.live.azurah.databinding.MenuReportCommentBinding
import com.live.azurah.databinding.MenuReportDeleteDialogBinding
import com.live.azurah.databinding.MenuReportMemberBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.model.CommentCommonResponse
import com.live.azurah.model.CommentResponse
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.CountResponse
import com.live.azurah.model.FollowFollowingResponse
import com.live.azurah.model.FullImageModel
import com.live.azurah.model.PostCommentListResposne
import com.live.azurah.model.PostResponse
import com.live.azurah.model.UserTag
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.ShowImagesDialogFragment
import com.live.azurah.util.containsBannedWord
import com.live.azurah.util.formatCount
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.removeMentionIfMatches
import com.live.azurah.util.setColoredUsername
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.showCustomToast
import com.live.azurah.util.showKeyboard
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(),
    Observer<Resource<Any>>, ScrollListener {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var postAdapter: PostDetailAdapter
    private var list = ArrayList<PostResponse.Body.Data>()
    private val commentList = ArrayList<CommentResponse>()

    //    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var videoAutoPlayHelper: VideoMultiplePostAutoPlayHelper? = null
    private var commentAdapter: CommentAdapter? = null
    private var mBottomSheetBinding: ItemBottomSheetCommentBinding? = null
    private var parentId = 0
    private var taggedId = 0
    private var mainPos = -1
    private var username = ""
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    private var showDialog = true
    private var currentCommentPage = 1
    private var totalCommentPageCount = 0
    private var resetCommentPage = false
    private lateinit var sharedViewModel: SharedViewModel
    private var mentionAdapter: MentionAdapter? = null
    private var mentionList = mutableListOf<Follower>()
    private val dummyFollowers = ArrayList<Follower>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()

        showDialog = false
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        videoAutoPlayHelper = VideoMultiplePostAutoPlayHelper(
            binding.rvPosts,
            list,
            requireContext(),
            this
            )
        videoAutoPlayHelper?.startObserving()
        binding.swipeRefreshLayout.gone()
        binding.shimmerLayout.visible()
        binding.shimmerLayout.startShimmer()
        getPosts()
        setAdapter()
    }

    private fun initListener() {
        with(binding) {

            swipeRefreshLayout.setOnRefreshListener {
                resetPage = true
                showDialog = false
                getPosts()
                swipeRefreshLayout.isRefreshing = true
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun setAdapter() {
        postAdapter = PostDetailAdapter(requireContext(), binding.rvPosts)
        binding.rvPosts.adapter = postAdapter
        postAdapter.submitList(list)

        postAdapter.onTagClick={tag->
            with(requireActivity() as HomeActivity){
                replaceFragment(SearchHomeFragment(tag))
            }
        }

        postAdapter.onPostImages = { parentPos, pos, images ->
            val imageList = images?.filter { it?.type == 1 }
                ?.map { FullImageModel(image = it?.image, type = 1) } as ArrayList
            val fullImageDialog = ShowImagesDialogFragment.newInstance(imageList, pos)
            fullImageDialog.show(parentFragmentManager, "FullImageDialog")
        }

        postAdapter.onSuggestionSeeAll = { pos ->
            with(requireActivity() as HomeActivity) {
                replaceFragment(SuggetionForYouFragment())
            }
        }
        postAdapter.onLikeUnlike = { pos, model ->
            val map = HashMap<String, String>()
            map["post_id"] = model.id.toString()
            map["status"] = model.is_like.toString()
            viewModel.postLikeUnlike(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                    }

                    Status.LOADING -> {
                    }

                    Status.ERROR -> {
                        showCustomSnackbar(
                            requireActivity(),
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
            viewModel.postBookmark(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                    }

                    Status.LOADING -> {
                    }

                    Status.ERROR -> {
                        showCustomSnackbar(
                            requireActivity(),
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
            map["status"] = model.isFollowByMe.toString()
            viewModel.followUnfollow(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                    }

                    Status.LOADING -> {
                    }

                    Status.ERROR -> {
                        showCustomSnackbar(
                            requireActivity(),
                            binding.root,
                            value.message.toString()
                        )
                    }
                }
            }
        }

        postAdapter.likeListener = { _, model ->
            with(requireActivity() as HomeActivity) {
                val bundle = Bundle()
                bundle.putString("postId", model.id.toString())
                val fragment = UserLikesFragment()
                fragment.arguments = bundle
                replaceFragment(fragment)
            }
        }

        postAdapter.commentListener = { pos, model ->
            commentsBottomSheet(model)
        }

        postAdapter.menuListener = { pos, model, view ->
            setPopUpWindow(view, model)
        }

        postAdapter.shareListener = { pos, model ->
             if (model.user?.profile_type == 2){
                 showShareDialog(pos,model.id.toString())
             }else{
                 if (model.user?.id.toString() == getPreference("id","")){
                     showShareDialog(pos,model.id.toString())
                 }else{
                     showSharePrivateDialog()
                 }

             }
        }
    }

    private fun setPopUpWindowDelete(
        view1: View,
        pos: Int,
        repPos: Int,
        commentId: String,
        model: PostResponse.Body.Data,
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
            ivUser.imageTintList = ContextCompat.getColorStateList(requireActivity(), R.color.black)
            tvNotDone.text = "Delete Comment"
            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                deletePost(pos, repPos, commentId, model, notificationId)

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
        with(view) {
            clDeleteUser.setOnClickListener {
                myPopupWindow.dismiss()
                deletePost(pos, repPos, commentId, model, notificationId)

            }

            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(requireActivity(), ReportUserActivity::class.java).apply {
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
                startActivity(Intent(requireActivity(), ReportUserActivity::class.java).apply {
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
                startActivity(Intent(requireContext(), ReportPostActivity::class.java).apply {
                    putExtra("from", "post")
                    putExtra("id", model.id.toString())
                    putExtra("reportedTo", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())
                })
                Log.d("dggggfdg", model.user?.username.toString())
            }

            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(requireContext(), ReportUserActivity::class.java).apply {
                    putExtra("from", "post")
                    putExtra("id", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())
                })
            }

            ivReportUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(requireContext(), ReportPostActivity::class.java).apply {
                    putExtra("from", "post")
                    putExtra("id", model.id.toString())
                    putExtra("reportedTo", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())
                })
            }

            ivUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(requireContext(), ReportUserActivity::class.java).apply {
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
                startActivity(Intent(requireActivity(), ReportUserActivity::class.java).apply {
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
                startActivity(Intent(requireActivity(), ReportUserActivity::class.java).apply {
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
        val dialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme)
        mBottomSheetBinding = ItemBottomSheetCommentBinding.inflate(layoutInflater, null, false)
        dialog.setContentView(mBottomSheetBinding!!.root)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
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

            if ((model.comment_count ?: 0) > 0) {
                mBottomSheetBinding?.tvTotalComments?.text = buildString {
                    append("Comments (")
                    append(formatCount(model.comment_count ?: 0))
                    append(")")
                }
            }

            ivImage.loadImage(
                ApiConstants.IMAGE_BASE_URL + getPreference("image", ""),
                R.drawable.profile_icon
            )
            ivSend.setOnClickListener {
                if (containsBannedWord(mBottomSheetBinding!!.etMessage.text.toString().trim())) {
                    showCustomSnackbar(
                        requireContext(),
                        it.rootView,
                        "Your comment contains banned or inappropriate words. Please remove them before posting."
                    )
                    return@setOnClickListener
                }

                val rawComment = mBottomSheetBinding!!.etMessage.text.toString().trim()
                val cleanedComment = rawComment.replace(Regex("@\\w+"), "").trim()

                if (cleanedComment.isEmpty()) {
                    showCustomSnackbar(
                        requireContext(),
                        it.rootView,
                        "Please enter comment"
                    )
                    return@setOnClickListener
                }

                addComment(model.id ?: 0, parentId, model)
                Log.d("fgdgfdh", parentId.toString())
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
        viewModel.addPostComment(map, requireActivity()).observe(viewLifecycleOwner) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                    LoaderDialog.dismiss()
                    when (value.data) {
                        is CommentCommonResponse -> {
                            with(binding) {
                                val res = value.data.body
                                Log.d("dgsdgdfhgdfg", parentId.toString())
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
                                this@HomeFragment.parentId = 0
                                taggedId = 0
                                mainPos = -1
                                if (commentList.isEmpty()) {
                                    mBottomSheetBinding?.tvNoDataFound?.visible()
                                } else {
                                    mBottomSheetBinding?.tvNoDataFound?.gone()

                                }
                                model.comment_count = (model.comment_count ?: 0) + 1
                                postAdapter.notifyDataSetChanged()
                                if ((model.comment_count ?: 0) > 0) {
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
                    LoaderDialog.dismiss()
                    showCustomSnackbar(requireContext(), binding.root, value.message.toString())
                }

                Status.LOADING -> {
                    LoaderDialog.show(requireActivity())
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
        viewModel.postCommentList(map, requireActivity()).observe(viewLifecycleOwner) { value ->
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
                                    /* mBottomSheetBinding?.tvTotalComments?.text = buildString {
                                         append("Comments (")
                                         append(formatCount(res?.total_count ?: 0))
                                         append(")")
                                     }*/
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
                    showCustomSnackbar(requireContext(), binding.root, value.message.toString())
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
                    Log.d("dsfgdgdfgdf", currentCommentPage.toString())
                    Log.d("dsfgdgdfgdf", totalCommentPageCount.toString())
                    Log.d("dsfgdgdfgdf", isApiRunning.toString())
                    if (currentCommentPage <= totalCommentPageCount && !isApiRunning) {
                        resetCommentPage = false
                        showDialog = false
                        Log.d("dsfgdgdfgdf", "dfgdffdfdh")
                        getCommentList(model)
                    }
                }
            }
        })

        commentList.clear()
        commentAdapter = CommentAdapter(requireContext(), commentList)
        mBottomSheetBinding!!.rvComments.adapter = commentAdapter
        commentAdapter?.menuListener = { pos, view, repPos, mainComment, item, notificationId ->
            if (model.user?.id.toString() == getPreference("id", "")) {
                if (item != null) {
                    if (item.user?.id.toString() == getPreference("id", "")) {
                        setPopUpWindowDelete(
                            view,
                            pos,
                            repPos,
                            item.id.toString(),
                            model,
                            notificationId
                        )
                    } else {
                        setPopUpWindowReportDelete(
                            view,
                            pos,
                            repPos,
                            item.id.toString(),
                            model,
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
                            model,
                            notificationId
                        )
                    } else {
                        setPopUpWindowReportDelete(
                            view,
                            pos,
                            repPos,
                            mainComment.id.toString(),
                            model,
                            mainComment,
                            null,
                            notificationId
                        )
                    }
                }
            } else {
                if (item != null) {
                    if (item.user?.id.toString() == getPreference("id", "")) {
                        setPopUpWindowDelete(
                            view,
                            pos,
                            repPos,
                            item.id.toString(),
                            model,
                            notificationId
                        )
                    } else {
                        setPopUpComment(view, mainComment, item)
                    }
                } else {
                    if (mainComment.user?.id.toString() == getPreference("id", "")) {
                        setPopUpWindowDelete(
                            view,
                            pos,
                            repPos,
                            mainComment.id.toString(),
                            model,
                            notificationId
                        )
                    } else {
                        setPopUpComment(view, mainComment, null)
                    }
                }
            }
        }

        commentAdapter?.onLikeUnlike = { pos, id, status, postId ->
            val map = HashMap<String, String>()
            map["post_id"] = postId
            map["post_comment_id"] = id
            map["status"] = status
            viewModel.postCommentLikeUnlike(map, requireActivity())
                .observe(viewLifecycleOwner) { value ->
                    when (value.status) {
                        Status.SUCCESS -> {

                        }

                        Status.LOADING -> {

                        }

                        Status.ERROR -> {
                            showCustomSnackbar(
                                requireActivity(),
                                binding.root,
                                value.message.toString()
                            )
                        }
                    }
                }
        }

        commentAdapter?.onCommentEdit = { pos, repPos, commentId, desc, notificationId ->
            showMessageDialog(pos, repPos, commentId, model.id.toString(), desc, notificationId)
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
                 *//* setColoredUsername(
                    mBottomSheetBinding!!.etMessage,
                    replyModel.user.username ?: ""
                )*//*
                Log.d("kjfdsjbds", replyModel.user.first_name.toString())
                Log.d("kjfdsjbds", replyModel.user.username.toString())
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
                        ContextCompat.getColorStateList(requireContext(), R.color.blue)
                    mBottomSheetBinding!!.ivSend.isEnabled = true
                } else {
                    mBottomSheetBinding!!.ivSend.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.button_grey)
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

                    val colorSpan =
                        ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue))
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
                                s.delete(
                                    mention.range.first,
                                    mention.range.last + 2
                                ) // +2 to include space
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


    /* private fun showMessageDialog(
         pos: Int,
         repPos: Int,
         commentId: String,
         postId: String,
         desc: String,
         notificationId: String
     ) {
         val customDialog = Dialog(requireContext())
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
             deletePost(pos, repPos, commentId, list[pos], notificationId)
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
                     requireContext(),
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
             viewModel.postCommentEdit(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                 when (value.status) {
                     Status.SUCCESS -> {
                         LoaderDialog.dismiss()
                         when (value.data) {
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
                                showCustomSnackbar(requireContext(), it, "Changes Saved.")
                            }
                        }

                    }

                    Status.LOADING -> {
                        LoaderDialog.show(requireActivity())
                    }

                    Status.ERROR -> {
                        LoaderDialog.dismiss()
                        showCustomSnackbar(
                            requireActivity(),
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
                        ContextCompat.getColorStateList(requireContext(), R.color.blue)
                    resetBinding.tvReplies.isEnabled = true
                } else {
                    resetBinding.tvReplies.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.button_grey)
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


    private fun showMessageDialog(
        pos: Int,
        repPos: Int,
        commentId: String,
        postId: String,
        desc: String,
        notificationId: String
    ) {
        val customDialog = Dialog(requireContext())
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
            deletePost(pos, repPos, commentId, list[pos], notificationId)
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
        applyInitialMentionStyling(resetBinding.tvDescription, initialComment)

        resetBinding.tvReplies.setOnClickListener {
            if (containsBannedWord(resetBinding.tvDescription.text.toString().trim())) {
                showCustomSnackbar(
                    requireContext(),
                    it,
                    "Your comment contains banned or inappropriate words. Please remove them before posting."
                )
                return@setOnClickListener
            }
            val rawComment = resetBinding.tvDescription.text.toString().trim()
            val cleanedComment = rawComment.replace(Regex("@\\w+"), "").trim()

            if (cleanedComment.isEmpty()) {
                showCustomSnackbar(
                    requireContext(),
                    it.rootView,
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
            viewModel.postCommentEdit(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                        LoaderDialog.dismiss()
                        when (value.data) {
                            is CommentCommonResponse -> {
                                Log.d("dsgdgdg", "afdgGDSgds")
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
                                showCustomSnackbar(requireContext(), it, "Changes Saved.")
                            }
                        }
                    }

                    Status.LOADING -> {
                        LoaderDialog.show(requireActivity())
                    }

                    Status.ERROR -> {
                        LoaderDialog.dismiss()
                        showCustomSnackbar(
                            requireActivity(),
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
        setupEditDialogMentionTextWatcher(
            resetBinding.tvDescription,
            resetBinding.rvMentions,
            mentionList,
            mentionAdapter
        )

        // Enable/disable reply button based on initial text
        resetBinding.tvReplies.isEnabled = initialComment.isNotEmpty()
        resetBinding.tvReplies.backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            if (initialComment.isNotEmpty()) R.color.blue else R.color.button_grey
        )
    }

    private fun applyInitialMentionStyling(editText: EditText, text: String) {
        val mentionPattern = Regex("@([A-Za-z0-9_]+)")
        val spannable = SpannableStringBuilder(text)

        mentionPattern.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val colorSpan =
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue))
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
                val replyButton =
                    (editText.rootView.findViewById<View>(R.id.tvReplies) as? android.widget.Button)
                replyButton?.let { button ->
                    if (s.toString().isNotEmpty()) {
                        button.backgroundTintList =
                            ContextCompat.getColorStateList(requireContext(), R.color.blue)
                        button.isEnabled = true
                    } else {
                        button.backgroundTintList =
                            ContextCompat.getColorStateList(requireContext(), R.color.button_grey)
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
                    val colorSpan =
                        ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue))
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

    private fun deletePost(
        pos: Int,
        repPos: Int,
        commentId: String,
        model: PostResponse.Body.Data,
        notificationId: String
    ) {
        viewModel.postCommentDelete(commentId, requireActivity(), notificationId)
            .observe(viewLifecycleOwner) { value ->
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
                                showCustomSnackbar(
                                    requireContext(),
                                    binding.root,
                                    "Comment Deleted."
                                )

                                if (commentList.isNotEmpty()) {
                                    mBottomSheetBinding!!.tvNoDataFound.gone()
                                } else {
                                    mBottomSheetBinding!!.tvNoDataFound.visible()
                                }
                                if ((model.comment_count ?: 0) > 0) {
                                    model.comment_count = (model.comment_count ?: 0) - 1
                                    mBottomSheetBinding?.tvTotalComments?.text = buildString {
                                        append("Comments (")
                                        append(formatCount(model.comment_count ?: 0))
                                        append(")")
                                    }
                                }

                            }
                        }

                    }

                    Status.LOADING -> {
                        LoaderDialog.show(requireActivity())
                    }

                    Status.ERROR -> {
                        LoaderDialog.dismiss()
                        showCustomSnackbar(
                            requireActivity(),
                            binding.root,
                            value.message.toString()
                        )
                    }
                }
            }
    }

    private fun sureDialog(model: PostResponse.Body.Data) {
        val customDialog = Dialog(requireContext())
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
            viewModel.userBlock(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                        LoaderDialog.dismiss()
                        when (value.data) {
                            is CommonResponse -> {
                                val res = value.data.body
                                showCustomSnackbar(
                                    requireActivity(),
                                    binding.root,
                                    "@" + model.user?.username + " has been blocked."
                                )
                                val userid = model.user?.id.toString()
                                list.removeAll { it.user?.id.toString() == userid }
                                postAdapter.notifyDataSetChanged()
                            }
                        }
                    }

                    Status.LOADING -> {
                        LoaderDialog.show(requireActivity())
                    }

                    Status.ERROR -> {
                        LoaderDialog.dismiss()
                        showCustomSnackbar(
                            requireActivity(),
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

    private fun showShareDialog(pos: Int, id: String) {
        val customDialog = Dialog(requireContext())
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
//            append("https://app.azrius.co.uk/admin/public-post/")
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
        val customDialog = Dialog(requireContext())
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
        viewModel.getPostList(map, requireActivity()).observe(viewLifecycleOwner, this)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
                LoaderDialog.dismiss()
                binding.swipeRefreshLayout.isRefreshing = false
                binding.shimmerLayout.gone()
                binding.swipeRefreshLayout.visible()
                binding.shimmerLayout.stopShimmer()
                when (value.data) {
                    is PostResponse -> {
                        with(binding) {
                            val res = value.data.body
                            sharedViewModel.setPostData(res)
                            /*res?.data?.forEach {
                                it.post_images?.reversed()
                            }*/
                            if (resetPage) {
                                list.clear()
                            }
                            list.addAll(res?.data ?: ArrayList())

                            /*  if (videoAutoPlayHelper == null) {
                                  Log.d("fsdsfsds","dsfdsgsdg")
                                  videoAutoPlayHelper = VideoMultiplePostAutoPlayHelper(
                                      binding.rvPosts,
                                      list,
                                      requireContext()
                                  )
                                  videoAutoPlayHelper?.startObserving()
                              } else {
                                  Log.d("fsdsfsds","fadsfdsgsdgdss")
                                  videoAutoPlayHelper!!.setList(list)
                              }*/
                            videoAutoPlayHelper!!.setList(list)
                            postAdapter.notifyDataSetChanged()
                            if (list.isNotEmpty()) {
                                currentPage = (res?.current_page ?: 0) + 1
                                tvNoDataFound.gone()
                            } else {
                                tvNoDataFound.visible()
                            }
                            totalPageCount = res?.total_pages ?: 0
                        }
                    }
                }
            }

            Status.LOADING -> {
                /*  if (showDialog){
                      LoaderDialog.show(this)
                  }*/
            }

            Status.ERROR -> {
                isApiRunning = false
                LoaderDialog.dismiss()
                binding.swipeRefreshLayout.isRefreshing = false
                showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                binding.shimmerLayout.gone()
                binding.shimmerLayout.stopShimmer()
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
        binding.shimmerLayout.stopShimmer()
    }

    override fun onResume() {
        super.onResume()
        getList()
        with(requireActivity() as HomeActivity) {
            val containerFragment =
                requireActivity().supportFragmentManager.findFragmentById(this.binding.fragmentContainer.id)
            if (containerFragment == null) {
                videoAutoPlayHelper?.restartPlayer()
            }
        }

//        postAdapter.notifyDataSetChanged()

        viewModel.getCounts(requireActivity()).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data) {
                        is CountResponse -> {
                            val res = it.data.body
                            sharedViewModel.setCount(res)
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    showCustomSnackbar(requireActivity(), binding.root, it.message.toString())

                }
            }
        }

    }

    fun pauseVideo() {
        videoAutoPlayHelper?.pausePlayer()
    }

    fun resumeVideo() {
        videoAutoPlayHelper?.restartPlayer()
    }

    private fun getList() {
        val map = HashMap<String, String>()
        map["page"] = "1"
        map["limit"] = "100000"
        map["type"] = "1"
        map["user_id"] = getPreference("id", "")

        viewModel.userFollowFollowingList(map, requireActivity())
            .observe(viewLifecycleOwner) { value ->
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
        Log.d("dsffdsfd", currentPage.toString())
        Log.d("dsffdsfd", totalPageCount.toString())
        Log.d("dsffdsfd", isApiRunning.toString())
        if (currentPage <= totalPageCount && !isApiRunning) {
            resetPage = false
            showDialog = false
            getPosts()
        }
    }


}