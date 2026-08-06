package com.live.azurah.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
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
import com.live.azurah.R
import com.live.azurah.adapter.DiscussionAdapter
import com.live.azurah.databinding.ActivityBibleDiscussionBinding
import com.live.azurah.databinding.ItemBottomSheetCommentBinding
import com.live.azurah.model.DiscussionListResponse
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.viewmodel.CommonViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.live.azurah.adapter.DiscussionCommentAdapter
import com.live.azurah.model.DiscussionCommentListResponse
import android.view.inputmethod.InputMethodManager
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
        binding.tvStudyMeta.text = "${questTitle.uppercase()} • DAY $dayNo • QUESTION $questionIndex OF 2"
        binding.tvQuestion.text = discussionDesc
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
                commonViewModel.likeUnlikeDiscussion(map, this).observe(this) {}
            }
        )
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = adapter
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
                            
                            // Let's filter if there's any logic. The API returns all discussions for the challenge.
                            // Maybe we just filter to the current discussionId? Or they are all for it.
                            val filtered = allDiscussions.filter { it.bible_quest_challenge_id == challengeId }
                            allDiscussions = filtered
                            
                            binding.tvResponseCount.text = "${allDiscussions.size} responses"
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
        map["bible_quest_id"] = "1" // This should probably be dynamic, but challengeId is what matters most
        map["bible_quest_challenge_id"] = challengeId.toString()
        map["title"] = ""
        map["description"] = description
        
        commonViewModel.addDiscussion(map, this).observe(this) { response ->
             when (response.status) {
                Status.SUCCESS -> {
                    binding.etComment.setText("")
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.etComment.windowToken, 0)
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
        val items = if (showFollowingOnly) {
            // Need following logic in API or user object, but for now we just show all or none if not supported
            allDiscussions 
        } else {
            allDiscussions
        }
        adapter.submitList(items)
    }

    private fun openCommentsBottomSheet(discussion: DiscussionListResponse.DiscussionData) {
        val bottomSheet = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        val bsBinding = ItemBottomSheetCommentBinding.inflate(layoutInflater)
        bottomSheet.setContentView(bsBinding.root)
        bottomSheet.show()
        
        lateinit var commentAdapter: DiscussionCommentAdapter
        commentAdapter = DiscussionCommentAdapter(
            onLikeClick = { item, _ ->
                val map = HashMap<String, String>()
                map["discussion_comment_id"] = item.id.toString()
                commonViewModel.likeUnlikeDiscussionComment(map, this).observe(this) {}
            },
            onDeleteClick = { item, _ ->
                item.id?.let {
                    commonViewModel.deleteDiscussionComment(it, this).observe(this) { response ->
                        if (response.status == Status.SUCCESS) {
                            fetchComments(discussion.id ?: 0, commentAdapter, bsBinding)
                        }
                    }
                }
            }
        )
        
        bsBinding.rvComments.layoutManager = LinearLayoutManager(this)
        bsBinding.rvComments.adapter = commentAdapter
        
        fetchComments(discussion.id ?: 0, commentAdapter, bsBinding)
        
        bsBinding.ivSend.setOnClickListener {
            val text = bsBinding.etMessage.text.toString().trim()
            if (text.isNotEmpty() && discussion.id != null) {
                val map = HashMap<String, String>()
                map["discussion_id"] = discussion.id.toString()
                map["comment"] = text
                map["mentions"] = "[]"
                
                commonViewModel.addDiscussionComment(map, this).observe(this) { response ->
                    if (response.status == Status.SUCCESS) {
                        bsBinding.etMessage.setText("")
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(bsBinding.etMessage.windowToken, 0)
                        fetchComments(discussion.id, commentAdapter, bsBinding)
                    }
                }
            }
        }
        
        bsBinding.ivCross.setOnClickListener { bottomSheet.dismiss() }
    }
    
    private fun fetchComments(discussionId: Int, adapter: DiscussionCommentAdapter, bsBinding: ItemBottomSheetCommentBinding) {
        commonViewModel.getDiscussionCommentList(1, 100, discussionId, "discussion", this).observe(this) { response ->
             when (response.status) {
                 Status.SUCCESS -> {
                     response.data?.let { data ->
                         if (data is DiscussionCommentListResponse && data.success == true) {
                             val comments = data.body?.data ?: emptyList()
                             adapter.submitList(comments)
                             bsBinding.tvTotalComments.text = "${comments.size} comments"
                         }
                     }
                 }
                 else -> {}
             }
        }
    }

    companion object {
        const val EXTRA_QUESTION_INDEX = "question_index"
        const val EXTRA_CHALLENGE_ID = "challenge_id"
        const val EXTRA_QUEST_TITLE = "quest_title"
        const val EXTRA_DAY_NO = "day_no"
        const val EXTRA_DISCUSSION_ID = "discussion_id"
        const val EXTRA_DISCUSSION_DESC = "discussion_desc"

        fun createIntent(context: Context, questionIndex: Int, challengeId: Int, questTitle: String, dayNo: Int, discussionId: Int, discussionDesc: String): Intent {
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
