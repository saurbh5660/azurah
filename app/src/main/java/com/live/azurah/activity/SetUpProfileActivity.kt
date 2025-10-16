package com.live.azurah.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.live.azurah.R
import com.live.azurah.databinding.ActivitySetUpProfileBinding
import com.live.azurah.fragment.AddPromptFragment
import com.live.azurah.fragment.BirthdayFragment
import com.live.azurah.fragment.CreateUsernameFragment
import com.live.azurah.fragment.CristianJourneyFragment
import com.live.azurah.fragment.InterestFragment
import com.live.azurah.fragment.LocationFragment
import com.live.azurah.fragment.NewsletterFragment
import com.live.azurah.fragment.UploadProfilePhotoFragment
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetUpProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivitySetUpProfileBinding
    private lateinit var sharedViewModel: SharedViewModel
    private var page = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetUpProfileBinding.inflate(layoutInflater)
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
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        page = intent.getStringExtra("page") ?: "0"

        when(page){
            "1"->{
                supportFragmentManager.beginTransaction().replace(R.id.frameContainer, CreateUsernameFragment()).commit()
            }
            "2"->{
                supportFragmentManager.beginTransaction().replace(R.id.frameContainer, UploadProfilePhotoFragment()).commit()

            }
            "3"->{
                supportFragmentManager.beginTransaction().replace(R.id.frameContainer, BirthdayFragment()).commit()

            }
           /* "3"->{
                supportFragmentManager.beginTransaction().replace(R.id.frameContainer, NewsletterFragment()).commit()

            }*/
            "4"->{
                supportFragmentManager.beginTransaction().replace(R.id.frameContainer, LocationFragment()).commit()

            }
            "5"->{
                supportFragmentManager.beginTransaction().replace(R.id.frameContainer, CristianJourneyFragment()).commit()

            }
            "6"->{
                supportFragmentManager.beginTransaction().replace(R.id.frameContainer, InterestFragment()).commit()

            }
            "7"->{
                supportFragmentManager.beginTransaction().replace(R.id.frameContainer, AddPromptFragment()).commit()

            }
            else->{
                supportFragmentManager.beginTransaction().replace(R.id.frameContainer, CreateUsernameFragment()).commit()
            }
        }
    }


}