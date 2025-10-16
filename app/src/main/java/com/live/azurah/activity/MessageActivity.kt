package com.live.azurah.activity

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
import com.live.azurah.R
import com.live.azurah.adapter.ViewPagerAdapter
import com.live.azurah.databinding.ActivityMessageBinding
import com.live.azurah.fragment.GeneralFragment
import com.live.azurah.fragment.GroupChatFragment
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessageActivity : AppCompatActivity() {
    lateinit var binding: ActivityMessageBinding
    private lateinit var fragmentList: MutableList<Fragment>
    private lateinit var viewpagerAdapter: ViewPagerAdapter
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
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

        binding.viewPager.isUserInputEnabled = false
        setGeneral()
        initData()
        initFragment()

        initListener()
    }

    private fun initListener() {
        with(binding){
            clGeneral.setOnClickListener {
                setGeneral()
                binding.viewPager.currentItem = 0
            }
            clGroup.setOnClickListener {
                setGroupChat()
                binding.viewPager.currentItem = 1
            }

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            ivCross.setOnClickListener {
                etSearch.setText("")
            }
            etSearch.addTextChangedListener(object: TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotBlank()){
                        ivCross.visibility = View.VISIBLE
                    }else{
                        ivCross.visibility = View.GONE
                    }
                    sharedViewModel.setSearchChat(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
        }
    }

    private fun setGeneral() {
        with(binding){
            tvGeneral.setTextColor(getColorStateList(R.color.cursor_color))
            tvGroupChat.setTextColor(getColorStateList(R.color.black))
            view1.backgroundTintList = getColorStateList(R.color.blue)
            view2.backgroundTintList = getColorStateList(R.color.mtrl_textinput_default_box_stroke_color)
        }
    }

    private fun setGroupChat() {
        with(binding){
            tvGeneral.setTextColor(getColorStateList(R.color.black))
            tvGroupChat.setTextColor(getColorStateList(R.color.cursor_color))
            view1.backgroundTintList = getColorStateList(R.color.mtrl_textinput_default_box_stroke_color)
            view2.backgroundTintList = getColorStateList(R.color.blue)
        }
    }

    private fun initData() {
        fragmentList = ArrayList()
        fragmentList.add(GeneralFragment())
        fragmentList.add(GroupChatFragment())
    }


    private fun initFragment() {
        viewpagerAdapter = ViewPagerAdapter(fragmentList, this)
        binding.viewPager.adapter = viewpagerAdapter
    }
}