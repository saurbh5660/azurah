package com.live.azurah.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.live.azurah.R
import com.live.azurah.adapter.DiscussionComment
import com.live.azurah.adapter.DiscussionCommentAdapter
import com.live.azurah.databinding.ActivityBibleDiscussionBinding

class BibleDiscussionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBibleDiscussionBinding
    private lateinit var adapter: DiscussionCommentAdapter
    private var showFollowingOnly = false

    private val allComments by lazy { sampleComments().toMutableList() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBibleDiscussionBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.tvBack.setOnClickListener { finish() }
        binding.btnSend.setOnClickListener {
            // UI-only for now
        }
    }

    private fun bindQuestionHeader() {
        val questionIndex = intent.getIntExtra(EXTRA_QUESTION_INDEX, 1)
        if (questionIndex == 2) {
            binding.tvStudyMeta.text = "ESTHER • DAY 5 • QUESTION 2 OF 2"
            binding.tvQuestion.text = "How can you apply Esther's courage this week?"
            binding.tvResponseCount.text = "9 responses"
            binding.tvLikeCount.text = "21 likes"
        } else {
            binding.tvStudyMeta.text = "ESTHER • DAY 5 • QUESTION 1 OF 2"
            binding.tvQuestion.text = "What stood out to you in today's reading?"
            binding.tvResponseCount.text = "${allComments.size} responses"
            binding.tvLikeCount.text = "47 likes"
        }
    }

    private fun setupList() {
        adapter = DiscussionCommentAdapter()
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = adapter
        refreshComments()
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
            allComments.filter { it.isFollowing }
        } else {
            allComments
        }
        adapter.submitList(items)
    }

    private fun sampleComments(): List<DiscussionComment> = listOf(
        DiscussionComment(
            initials = "GW",
            username = "@grace_walker",
            timeAgo = "2 hours ago",
            comment = "Esther's courage really stood out to me 🙏 She risked everything knowing God might not even intervene the way she hoped. That kind of faith is rare.",
            likes = 18,
            replies = 2,
            avatarColor = "#7DB8E8",
            isFollowing = true,
            isTop = true
        ),
        DiscussionComment(
            initials = "KD",
            username = "@kevin_d",
            timeAgo = "4 hours ago",
            comment = "The moment where she says \"if I perish, I perish\" hit me hard. Reminds me that obedience sometimes means stepping into the unknown.",
            likes = 12,
            replies = 1,
            avatarColor = "#A8D4E8",
            isFollowing = false
        ),
        DiscussionComment(
            initials = "SM",
            username = "@sara_m",
            timeAgo = "5 hours ago",
            comment = "I noticed how Esther prepared before she acted — fasting, seeking counsel. Courage isn't impulsive; it's grounded. 🤔",
            likes = 9,
            replies = 0,
            avatarColor = "#F6C332",
            isFollowing = true
        ),
        DiscussionComment(
            initials = "JL",
            username = "@james_lee",
            timeAgo = "Yesterday",
            comment = "Mordecai's trust in God's bigger plan encouraged me. Even when we can't see the ending, God is already working.",
            likes = 7,
            replies = 3,
            avatarColor = "#8BD4C4",
            isFollowing = false
        ),
        DiscussionComment(
            initials = "AR",
            username = "@amy_r",
            timeAgo = "Yesterday",
            comment = "What stood out was community — Esther didn't stand alone. We need people praying with us when decisions are hard.",
            likes = 15,
            replies = 4,
            avatarColor = "#FB7D24",
            isFollowing = true
        )
    )

    companion object {
        const val EXTRA_QUESTION_INDEX = "question_index"

        fun createIntent(context: Context, questionIndex: Int = 1): Intent {
            return Intent(context, BibleDiscussionActivity::class.java).apply {
                putExtra(EXTRA_QUESTION_INDEX, questionIndex)
            }
        }
    }
}
