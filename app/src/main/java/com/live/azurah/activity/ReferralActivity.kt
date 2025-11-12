package com.live.azurah.activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.live.azurah.R
import com.live.azurah.adapter.ReferFriendAdapter
import com.live.azurah.databinding.ActivityReferralBinding
import com.live.azurah.model.ReferralRewardResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class ReferralActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReferralBinding
    private val viewModel by viewModels<CommonViewModel>()
    private var blockAdapter: ReferFriendAdapter? = null
    private var blockList = ArrayList<ReferralRewardResponse.Body.Referral>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReferralBinding.inflate(layoutInflater)
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
        getReferral()

        binding.tvCopy.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", binding.tvReferralCode.text)
            clipboard.setPrimaryClip(clip)
//            showCustomSnackbar(this, it, "Copied to clipboard!")
        }

        binding.backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnShareLink.setOnClickListener {
           /* val message = """
                Hey! 👋  
                Use my referral code **${binding.tvReferralCode.text}** to sign up and earn rewards!  
                Download the app here: https://play.google.com/store/apps/details?id=${packageName}
            """.trimIndent()*/
            val userName = getPreference("username","")

            val shareLink = buildString {
                append("$userName invited you to join Azrius.\n")
                append("Azrius is a Christian social media platform where you can grow in faith, find community, join Bible Quests, share prayer requests, and explore uplifting content.\n")
                append("Download the app to get started.\n")
                append("https://app.azrius.co.uk/common_api/deepLinking/referral?referral_code=${binding.tvReferralCode.text}")
            }

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Invite via Referral Code")
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareLink)
            startActivity(Intent.createChooser(shareIntent, "Share Referral Code via"))
        }
    }

    private fun getReferral() {
        viewModel.getMyReferralCode(this).observe(this) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                    LoaderDialog.dismiss()
                    when (value.data) {
                        is ReferralRewardResponse -> {
                            val res = value.data.body
                            binding.tvReferralCode.text = res?.myReferralCode ?: ""
                            blockList.clear()
                            blockList.addAll(res?.referralList ?: ArrayList())
                            blockAdapter = ReferFriendAdapter(this,blockList)
                            binding.rvBlock.adapter = blockAdapter

                            if (blockList.isNotEmpty()){
                                binding.tvNoFriends.gone()
                                binding.rvBlock.visible()
                            }else{
                                binding.tvNoFriends.visible()
                                binding.rvBlock.gone()
                            }
                            if ((res?.daysLeft ?: 0) > 1) {
                                binding.tvDaysLeft.text = buildString {
                                    append(res?.daysLeft ?: 0)
                                    append(" days left")
                                }
                            } else if ((res?.daysLeft ?: 0) == 1) {
                                binding.tvDaysLeft.text = buildString {
                                    append(res?.daysLeft ?: 0)
                                    append(" day left")
                                }
                            } else {
                                binding.tvDaysLeft.text = buildString {
                                    append(res?.daysLeft ?: 0)
                                    append(" days left")
                                }
                            }
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
}