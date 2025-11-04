package com.live.azurah.activity

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
import com.live.azurah.adapter.BlockAdapter
import com.live.azurah.databinding.ActivityReferralBinding
import com.live.azurah.model.BlockResposne
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
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
    private var blockAdapter : BlockAdapter? = null
    private var blockList = ArrayList<BlockResposne.Body.Data>()
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
    }

    private fun getReferral(){
        viewModel.getMyReferralCode(this).observe(this){value->
            when (value.status) {
                Status.SUCCESS -> {
                    LoaderDialog.dismiss()
                    when(value.data){
                        is BlockResposne -> {
                            val res = value.data.body
                            blockList.clear()
                            blockList.addAll(res?.data ?: ArrayList())
                            blockAdapter?.notifyDataSetChanged()

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