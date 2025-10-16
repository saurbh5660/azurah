package com.live.azurah.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.live.azurah.R
import com.live.azurah.activity.HomeActivity
import com.live.azurah.databinding.FragmentProfileIntroBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileIntroFragment : Fragment() {

    private lateinit var binding : FragmentProfileIntroBinding
    private var backPressedTime: Long = 0
    private val doubleBackToExitDuration: Long = 2000
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileIntroBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      /*  val backPressedCallback = object : OnBackPressedCallback(true) {
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
                (requireActivity() as HomeActivity).binding.llHome.performClick()
                (requireActivity() as HomeActivity).removeFragment()
            }
            tvSkip.setOnClickListener {
                (requireActivity() as HomeActivity).removeFragment()
            }

        }
    }
}