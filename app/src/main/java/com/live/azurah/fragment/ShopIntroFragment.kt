package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.live.azurah.R
import com.live.azurah.activity.HomeActivity
import com.live.azurah.databinding.FragmentShopIntroBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ShopIntroFragment : Fragment() {
   private lateinit var binding: FragmentShopIntroBinding
    private var backPressedTime: Long = 0
    private val doubleBackToExitDuration: Long = 2000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShopIntroBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
     /*   val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime < doubleBackToExitDuration) {
                    requireActivity().finishAffinity()
                    return
                }
                backPressedTime = currentTime
                lifecycleScope.launch {
                    (requireActivity() as HomeActivity).binding.clTapAgain.visibility = View.VISIBLE
                    delay(2000)
                    (requireActivity() as HomeActivity).binding.clTapAgain.visibility = View.GONE
                }
            }
        }


        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), backPressedCallback)
*/


        with(binding){
            ivNext.setOnClickListener {
                (requireActivity() as HomeActivity).binding.llProfile.performClick()
                (requireActivity() as HomeActivity).replaceIntroFragment(ProfileIntroFragment())

            }
            tvSkip.setOnClickListener {
                (requireActivity() as HomeActivity).removeFragment()
            }

        }
    }

}