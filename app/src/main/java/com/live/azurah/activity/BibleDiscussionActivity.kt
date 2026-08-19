package com.live.azurah.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.live.azurah.R
import com.live.azurah.adapter.CommentAdapter
import com.live.azurah.adapter.DiscussionAdapter
import com.live.azurah.adapter.Follower
import com.live.azurah.adapter.MentionAdapter
import com.live.azurah.databinding.ActivityBibleDiscussionBinding
import com.live.azurah.databinding.ItemBottomSheetCommentBinding
import com.live.azurah.model.CommentResponse
import com.live.azurah.model.DiscussionCommentItem
import com.live.azurah.model.DiscussionCommentListResponse
import com.live.azurah.model.DiscussionListResponse
import com.live.azurah.model.FollowFollowingResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.containsBannedWord
import com.live.azurah.util.formatCount
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.showKeyboard
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BibleDiscussionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBibleDiscussionBinding
    private lateinit var adapter: DiscussionAdapter
    private lateinit var commonViewModel: CommonViewModel
    private var showFollowingOnly = false

    private var questionIndex = 1
    private var challengeId = 0
    private var questTitle = ""
    private var dayNo = 1
    private var discussionId = 0
    private var discussionDesc = ""

    private var allDiscussions: List<DiscussionListResponse.DiscussionData> = emptyList()

    private var bottomSheetBinding: ItemBottomSheetCommentBinding? = null
    private var mentionAdapter: MentionAdapter? = null
    private val mentionList = mutableListOf<Follower>()
    private val dummyFollowers = ArrayList<Follower>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBibleDiscussionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        commonViewModel = ViewModelProvider(this)[CommonViewModel::class.java]

        questionIndex = intent.getIntExtra(EXTRA_QUESTION_INDEX, 1)
        challengeId = intent.getIntExtra(EXTRA_CHALLENGE_ID, 0)
        questTitle = intent.getStringExtra(EXTRA_QUEST_TITLE) ?: ""
        dayNo = intent.getIntExtra(EXTRA_DAY_NO, 1)
        discussionId = intent.getIntExtra(EXTRA_DISCUSSION_ID, 0)
        discussionDesc = intent.getStringExtra(EXTRA_DISCUSSION_DESC) ?: ""

        window.statusBarColor = ContextCompat.getColor(this, R.color.dashboard_primary)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(binding.discussionRoot) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.headerSection.updatePadding(top = bars.top + (8 * resources.displayMetrics.density).toInt())
            binding.inputBar.updatePadding(bottom = bars.bottom + (10 * resources.displayMetrics.density).toInt())
            insets
        }

        bindQuestionHeader()
        setupList()
        setupTabs()
        loadFollowers()
        fetchDiscussions()

        binding.tvBack.setOnClickListener { finish() }
        binding.btnSend.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isNotEmpty() && challengeId != 0 && discussionId != 0) {
                addDiscussion(text)
            }
        }
    }

    private fun bindQuestionHeader() {
//        binding.tvStudyMeta.text = "${questTitle.uppercase()} • DAY $dayNo • QUESTION $questionIndex OF 2"
        binding.tvStudyMeta.text = "${questTitle.uppercase()} • DAY $dayNo"
//        binding.tvQuestion.text = discussionDesc
        binding.tvResponseCount.text = "0 responses"
        binding.tvLikeCount.text = "0 likes"
    }

    private fun setupList() {
        adapter = DiscussionAdapter(
            onCommentClick = { discussion, _ ->
                openCommentsBottomSheet(discussion)
            },
            onLikeClick = { discussion, _ ->
                val map = HashMap<String, String>()
                map["discussion_id"] = discussion.id.toString()
                map["status"] = if (discussion.is_like == 1) "0" else "1"
                commonViewModel.likeUnlikeDiscussion(map, this).observe(this) {}
            }
        )
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = adapter
    }

    private fun loadFollowers() {
        val map = HashMap<String, String>()
        map["limit"] = "100"
        map["page"] = "1"
        map["type"] = "following"
        map["user_id"] = getPreference("id", "")

        commonViewModel.userFollowFollowingList(map, this).observe(this) { value ->
            if (value.status == Status.SUCCESS && value.data is FollowFollowingResponse) {
                dummyFollowers.clear()
                dummyFollowers.addAll(
                    value.data.body?.data?.map {
                        Follower(
                            id = (it.follow_to_user?.id ?: 0).toString(),
                            username = it.follow_to_user?.username ?: "",
                            profileImageUrl = it.follow_to_user?.image ?: "",
                            displayNamePreference = it.follow_to_user?.display_name_preference ?: "",
                            firstName = it.follow_to_user?.first_name ?: "",
                            lastName = it.follow_to_user?.last_name ?: ""
                        )
                    } ?: emptyList()
                )
            }
        }
    }

    private fun fetchDiscussions() {
        if (challengeId == 0) return

        commonViewModel.getDiscussionList(1, 50, challengeId, this).observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    response.data?.let { data ->
                        if (data is DiscussionListResponse && data.success == true) {
                            allDiscussions = data.body?.data ?: emptyList()
                            binding.tvResponseCount.text = "${allDiscussions.size} responses"
                            binding.tvQuestion.text = data.body?.title ?: ""

                            val totalLikes = allDiscussions.sumOf { it.like_count ?: 0 }
                            binding.tvLikeCount.text = "$totalLikes likes"
                            refreshComments()
                        }
                    }
                }

                Status.ERROR -> {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }

    private fun addDiscussion(description: String) {
        val map = HashMap<String, String>()
        map["bible_quest_challenge_id"] = challengeId.toString()
        map["title"] = ""
        map["description"] = description

        commonViewModel.addDiscussion(map, this).observe(this) { response ->
            when (response.status) {
                Status.SUCCESS -> {
                    binding.etComment.setText("")
                    hideKeyboard(binding.etComment)
                    fetchDiscussions()
                }

                Status.ERROR -> {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }

    private fun setupTabs() {
        binding.tabAll.setOnClickListener { selectTab(followingOnly = false) }
        binding.tabFollowing.setOnClickListener { selectTab(followingOnly = true) }
        selectTab(followingOnly = false)
    }

    private fun selectTab(followingOnly: Boolean) {
        showFollowingOnly = followingOnly
        val primary = ContextCompat.getColor(this, R.color.dashboard_primary)
        val muted = Color.parseColor("#9AA8B8")
        val bold = ResourcesCompat.getFont(this, R.font.inter_bold)
        val regular = ResourcesCompat.getFont(this, R.font.inter)

        if (followingOnly) {
            binding.tvTabAll.setTextColor(muted)
            binding.tvTabAll.typeface = regular
            binding.underlineAll.visibility = View.INVISIBLE
            binding.tvTabFollowing.setTextColor(primary)
            binding.tvTabFollowing.typeface = bold
            binding.underlineFollowing.visibility = View.VISIBLE
        } else {
            binding.tvTabAll.setTextColor(primary)
            binding.tvTabAll.typeface = bold
            binding.underlineAll.visibility = View.VISIBLE
            binding.tvTabFollowing.setTextColor(muted)
            binding.tvTabFollowing.typeface = regular
            binding.underlineFollowing.visibility = View.INVISIBLE
        }
        refreshComments()
    }

    private fun refreshComments() {
        adapter.submitList(allDiscussions)
    }

    private fun openCommentsBottomSheet(discussion: DiscussionListResponse.DiscussionData) {
        val dialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        bottomSheetBinding = ItemBottomSheetCommentBinding.inflate(layoutInflater, null, false)
        val bsBinding = bottomSheetBinding ?: return
        val discussionId = discussion.id ?: 0
        val commentList = ArrayList<CommentResponse>()

        dialog.setContentView(bsBinding.root)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.peekHeight = resources.displayMetrics.heightPixels / 2
        dialog.setCancelable(true)
        dialog.show()

        bsBinding.root.setOnApplyWindowInsetsListener { _, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            bsBinding.etMessage.translationY = if (imeHeight > 0) -imeHeight.toFloat() else 0f
            insets
        }

        bsBinding.ivCross.setOnClickListener { dialog.dismiss() }
        bsBinding.ivSend.isEnabled = false
        bsBinding.ivImage.loadImage(
            ApiConstants.IMAGE_BASE_URL + getPreference("image", ""),
            R.drawable.profile_icon
        )

        val commentAdapter = CommentAdapter(this, commentList)
        bsBinding.rvComments.layoutManager = LinearLayoutManager(this)
        bsBinding.rvComments.adapter = commentAdapter

        commentAdapter.onLikeUnlike = { _, commentId, status, _ ->
            val map = HashMap<String, String>()
            map["discussion_id"] = discussionId.toString()
            map["discussion_comment_id"] = commentId
            map["status"] = status
            commonViewModel.likeUnlikeDiscussionComment(map, this).observe(this) {}
        }

        commentAdapter.menuListener = { pos, _, repPos, model, _, _ ->
            if (repPos == -1 && model.user_id.toString() == getPreference("id", "")) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete comment")
                    .setMessage("Are you sure you want to delete this comment?")
                    .setPositiveButton("Delete") { _, _ ->
                        model.id?.let { commentId ->
                            commonViewModel.deleteDiscussionComment(commentId, this)
                                .observe(this) { response ->
                                    if (response.status == Status.SUCCESS) {
                                        commentList.removeAt(pos)
                                        commentAdapter.notifyItemRemoved(pos)
                                        updateBottomSheetCommentHeader(bsBinding, commentList.size)
                                        fetchDiscussions()
                                    } else if (response.status == Status.ERROR) {
                                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        commentAdapter.replyListener = { _, _, model, _, _ ->
            val mentionText = "@${model.user?.username.orEmpty()} "
            bsBinding.etMessage.isFocusable = true
            bsBinding.etMessage.setText(mentionText)
            bsBinding.etMessage.setSelection(mentionText.length)
            bsBinding.etMessage.showKeyboard()
        }

        fetchComments(discussionId, commentList, commentAdapter, bsBinding)

        mentionList.clear()
        mentionAdapter = MentionAdapter(mentionList) { user ->
            val text = bsBinding.etMessage.text.toString()
            val cursorPos = bsBinding.etMessage.selectionStart
            val atIndex = text.lastIndexOf("@", cursorPos - 1)
            if (atIndex != -1) {
                val newText = text.substring(0, atIndex) + "@${user.username} " +
                    text.substring(cursorPos)
                bsBinding.etMessage.setText(newText)
                bsBinding.etMessage.setSelection(atIndex + user.username.length + 2)
            }
            bsBinding.rvMentions.gone()
        }
        bsBinding.rvMentions.adapter = mentionAdapter
        setupMentionTextWatcher(bsBinding.etMessage, bsBinding)

        bsBinding.ivSend.setOnClickListener {
            val rawComment = bsBinding.etMessage.text.toString().trim()
            if (containsBannedWord(rawComment)) {
                Toast.makeText(
                    this,
                    "Your comment contains banned or inappropriate words. Please remove them before posting.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val cleanedComment = rawComment.replace(Regex("@\\w+"), "").trim()
            if (cleanedComment.isEmpty()) {
                Toast.makeText(this, "Please enter comment", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addDiscussionComment(
                discussionId,
                rawComment,
                commentList,
                commentAdapter,
                bsBinding
            )
        }
    }

    private fun updateBottomSheetCommentHeader(
        bsBinding: ItemBottomSheetCommentBinding,
        count: Int
    ) {
        bsBinding.tvTotalComments.text = if (count > 0) {
            buildString {
                append("Comments (")
                append(formatCount(count))
                append(")")
            }
        } else {
            "Comments"
        }
        if (count == 0) {
            bsBinding.tvNoDataFound.visible()
        } else {
            bsBinding.tvNoDataFound.gone()
        }
    }

    private fun mapToCommentResponse(
        item: DiscussionCommentItem,
        discussionId: Int
    ): CommentResponse {
        return CommentResponse(
            id = item.id,
            description = item.description,
            like_count = item.like_count,
            is_like = item.is_like,
            created_at = item.created_at,
            user_id = item.user_id,
            post_id = discussionId,
            user = CommentResponse.User(
                id = item.user_id,
                username = item.user?.username,
                image = item.user?.profile_image,
                first_name = item.user?.first_name,
                last_name = item.user?.last_name
            ),
            post_comment_tags = item.discussion_comment_tags,
            tagged_user_data = item.tagged_user_data
        )
    }

    private fun addDiscussionComment(
        discussionId: Int,
        comment: String,
        commentList: ArrayList<CommentResponse>,
        commentAdapter: CommentAdapter,
        bsBinding: ItemBottomSheetCommentBinding
    ) {
        val map = HashMap<String, String>()
        map["discussion_id"] = discussionId.toString()
        map["description"] = comment

        val taggedUserIds = extractTaggedUserIds(comment)
        if (taggedUserIds.isNotEmpty()) {
            map["tagged_user_id"] = taggedUserIds
        }

        commonViewModel.addDiscussionComment(map, this).observe(this) { response ->
            when (response.status) {
                Status.SUCCESS -> {
                    LoaderDialog.dismiss()
                    bsBinding.etMessage.setText("")
                    hideKeyboard(bsBinding.etMessage)
                    fetchComments(discussionId, commentList, commentAdapter, bsBinding)
                    fetchDiscussions()
                }

                Status.ERROR -> {
                    LoaderDialog.dismiss()
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }

                Status.LOADING -> LoaderDialog.show(this)
            }
        }
    }

    private fun extractTaggedUserIds(text: String): String {
        val mentionPattern = Regex("@([A-Za-z0-9_]+)")
        return mentionPattern.findAll(text)
            .map { it.groupValues[1] }
            .mapNotNull { username ->
                dummyFollowers.firstOrNull { it.username.equals(username, ignoreCase = true) }?.id
            }
            .distinct()
            .joinToString(",")
    }

    private fun fetchComments(
        discussionId: Int,
        commentList: ArrayList<CommentResponse>,
        commentAdapter: CommentAdapter,
        bsBinding: ItemBottomSheetCommentBinding
    ) {
        val type = if (showFollowingOnly) "following" else "all"
        commonViewModel.getDiscussionCommentList(1, 100, discussionId, type, this)
            .observe(this) { response ->
                when (response.status) {
                    Status.SUCCESS -> {
                        response.data?.let { data ->
                            if (data is DiscussionCommentListResponse && data.success == true) {
                                val comments = data.body?.data ?: emptyList()
                                commentList.clear()
                                commentList.addAll(
                                    comments.map { mapToCommentResponse(it, discussionId) }
                                )
                                commentAdapter.notifyDataSetChanged()
                                updateBottomSheetCommentHeader(bsBinding, commentList.size)
                            }
                        }
                    }

                    Status.ERROR -> {
                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> {}
                }
            }
    }

    private fun setupMentionTextWatcher(
        editText: EditText,
        bsBinding: ItemBottomSheetCommentBinding
    ) {
        val mentionPattern = Regex("@([A-Za-z0-9_]+)")
        editText.setSpannableFactory(CustomSpannableFactory())

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
                            bsBinding.rvMentions.visible()
                            mentionAdapter?.notifyDataSetChanged()
                        } else {
                            bsBinding.rvMentions.gone()
                        }
                    } else {
                        bsBinding.rvMentions.gone()
                    }
                } else {
                    bsBinding.rvMentions.gone()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                isFormatting = true
                try {
                    applyMentionSpans(s, mentionPattern)
                    handleMentionDeletion(s, editText, mentionPattern, lastText)
                } finally {
                    isFormatting = false
                }

                if (s.toString().isNotEmpty()) {
                    bsBinding.ivSend.backgroundTintList =
                        ContextCompat.getColorStateList(this@BibleDiscussionActivity, R.color.blue)
                    bsBinding.ivSend.isEnabled = true
                } else {
                    bsBinding.ivSend.backgroundTintList =
                        ContextCompat.getColorStateList(this@BibleDiscussionActivity, R.color.button_grey)
                    bsBinding.ivSend.isEnabled = false
                }
            }
        })
    }

    private fun applyMentionSpans(s: Editable, mentionPattern: Regex) {
        val existingSpans = s.getSpans(0, s.length, ForegroundColorSpan::class.java)
        existingSpans.forEach { s.removeSpan(it) }
        mentionPattern.findAll(s.toString()).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.blue))
            s.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun handleMentionDeletion(
        s: Editable,
        editText: EditText,
        mentionPattern: Regex,
        lastText: String
    ) {
        val currentText = s.toString()
        val currentSelection = editText.selectionStart
        if (lastText.length > currentText.length && currentSelection > 0) {
            val deletedCharCount = lastText.length - currentText.length
            if (deletedCharCount == 1) {
                val mentions = mentionPattern.findAll(currentText).toList()
                for (mention in mentions) {
                    val mentionStart = mention.range.first
                    val mentionEnd = mention.range.last + 1
                    if (currentSelection > mentionStart && currentSelection <= mentionEnd + 1) {
                        s.delete(mentionStart, mentionEnd)
                        editText.setSelection(mentionStart)
                        return
                    }
                }
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private inner class CustomSpannableFactory : Spannable.Factory() {
        override fun newSpannable(source: CharSequence): Spannable {
            return SpannableStringBuilder(source)
        }
    }

    companion object {
        const val EXTRA_QUESTION_INDEX = "question_index"
        const val EXTRA_CHALLENGE_ID = "challenge_id"
        const val EXTRA_QUEST_TITLE = "quest_title"
        const val EXTRA_DAY_NO = "day_no"
        const val EXTRA_DISCUSSION_ID = "discussion_id"
        const val EXTRA_DISCUSSION_DESC = "discussion_desc"

        fun createIntent(
            context: Context,
            questionIndex: Int,
            challengeId: Int,
            questTitle: String,
            dayNo: Int,
            discussionId: Int,
            discussionDesc: String
        ): Intent {
            return Intent(context, BibleDiscussionActivity::class.java).apply {
                putExtra(EXTRA_QUESTION_INDEX, questionIndex)
                putExtra(EXTRA_CHALLENGE_ID, challengeId)
                putExtra(EXTRA_QUEST_TITLE, questTitle)
                putExtra(EXTRA_DAY_NO, dayNo)
                putExtra(EXTRA_DISCUSSION_ID, discussionId)
                putExtra(EXTRA_DISCUSSION_DESC, discussionDesc)
            }
        }
    }
}
