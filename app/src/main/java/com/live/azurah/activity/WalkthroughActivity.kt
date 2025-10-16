package com.live.azurah.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.live.azurah.R
import com.live.azurah.adapter.ViewPagerAdapter
import com.live.azurah.databinding.ActivityWalkthroughBinding
import com.live.azurah.fragment.IntroFragment1
import com.live.azurah.fragment.IntroFragment2
import com.live.azurah.fragment.IntroFragment3
import com.live.azurah.util.savePreference1
import com.live.azurah.viewmodel.SharedViewModel


class WalkthroughActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWalkthroughBinding
    private lateinit var fragmentList: MutableList<Fragment>
    private lateinit var viewpagerAdapter: ViewPagerAdapter
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalkthroughBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.blue)
        window.setBackgroundDrawable(ActivityCompat.getDrawable(this,R.drawable.gradient_bg_simple))
        window.navigationBarColor = getColor(R.color.blue)
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
        sharedViewModel.walkThrough.observe(this) {
            if (it != null) {
                when (it) {
                    0 -> {
                        savePreference1("isAlreadyVisited", true)
                        startActivity(Intent(this, SignUpActivity::class.java))
                    }

                    1 -> binding.viewPager.setCurrentItem(1, true)
                    2 -> binding.viewPager.setCurrentItem(2, true)

                }

            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                when(position){
                    0->{
                        binding.tvNext.text = "Next"
                    }
                    1->{
                        binding.tvNext.text = "Next"
                    }
                    2->{
                        binding.tvNext.text = "Get Started!"
                    }
                }

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })


        binding.tvNext.setOnClickListener {
            when(binding.viewPager.currentItem){
                0->{
                    binding.viewPager.setCurrentItem(1,true)
                }
                1->{
                    binding.viewPager.setCurrentItem(2,true)
                }
                2->{
                    savePreference1("isAlreadyVisited",true)
                    startActivity(Intent(this,SignUpActivity::class.java))
                }
            }
        }

        initData()
        initFragment()
        binding.dotsIndicator.attachTo(binding.viewPager)

    }

    private fun initData() {
        fragmentList = ArrayList()
        fragmentList.add(IntroFragment1())
        fragmentList.add(IntroFragment2())
        fragmentList.add(IntroFragment3())
    }

    private fun initFragment() {
        viewpagerAdapter = ViewPagerAdapter(fragmentList, this)
        binding.viewPager.adapter = viewpagerAdapter
    }
}