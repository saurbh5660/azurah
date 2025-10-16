package com.live.azurah.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.live.azurah.R
import com.live.azurah.adapter.ViewPagerAdapter
import com.live.azurah.databinding.ActivityMyPostPrayerTestimonyBinding
import com.live.azurah.fragment.MyPostPrayerFragment
import com.live.azurah.fragment.MyPostTestimonyFragment
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class MyPostPrayerTestimonyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyPostPrayerTestimonyBinding
    private val list = arrayOf("Prayer Request","Testimonies")
    private lateinit var fragmentList: MutableList<Fragment>
    private lateinit var viewpagerAdapter: ViewPagerAdapter
    private var type = "0"
    private var search = ""
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPostPrayerTestimonyBinding.inflate(layoutInflater)
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

        initData()
        initFragment()
        TabLayoutMediator(binding.tbLayout, binding.viewPager) { tab, position ->
            tab.text = list[position]
        }.attach()

        initListener()
    }

    private fun initListener() {
        with(binding){
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            ivCross.setOnClickListener {
                etSearch.setText("")
            }
            etSearch.addTextChangedListener(object: TextWatcher {
                var delay : Long = 1000
                var timer = Timer()
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    timer.cancel()
                    timer.purge()
                    if (s.toString().isNotBlank()){
                        ivCross.visibility = View.VISIBLE
                    }else{
                        ivCross.visibility = View.GONE
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            lifecycleScope.launch(Dispatchers.Main){
                                search = s.toString().trim()
                                sharedViewModel.setSearchChat(search)
                            }
                        }
                    }, delay)
                }

            })
        }
    }

    private fun initData() {
        fragmentList = ArrayList()
        fragmentList.add(MyPostPrayerFragment())
        fragmentList.add(MyPostTestimonyFragment())
    }
    private fun initFragment() {
        viewpagerAdapter = ViewPagerAdapter(fragmentList, this)
        binding.viewPager.adapter = viewpagerAdapter
    }

    fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, fragment).addToBackStack(null).commit()
    }


}