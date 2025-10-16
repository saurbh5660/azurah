package com.live.azurah.activity

import android.graphics.text.LineBreaker
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import androidx.activity.viewModels
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.databinding.ActivityContentBinding
import com.live.azurah.model.ContentResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.sanitizeHtml
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContentActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityContentBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var type = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentBinding.inflate(layoutInflater)
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

        type = intent.getIntExtra("type",0)

        when(type){
            0->{
                binding.title.text  = getString(R.string.terms_conditions)
                viewModel.termsCondition(this).observe(this,this)
            }
            1->{
                binding.title.text  = getString(R.string.privacy_policy)
                viewModel.privacyPolicy(this).observe(this,this)

            }
            2->{
                binding.title.text  = "Community Guidelines"
                viewModel.termsCondition(this).observe(this,this)
            }
        }

        binding.backIcon.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is ContentResponse -> {
                        val res = value.data.body?.description
                        val cleanedHtml = sanitizeHtml(res ?: "")
                        binding.tvDescription.apply {
                            text = cleanedHtml
                            hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
                            justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE
                        }
                    }
                }
            }
            Status.LOADING -> {
                LoaderDialog.show(this)
            }
            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(this,binding.root, value.message.toString())
            }
        }
    }
}