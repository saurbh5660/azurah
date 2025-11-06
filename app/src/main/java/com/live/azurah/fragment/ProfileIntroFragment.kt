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

        with(binding){
            ivNext.setOnClickListener {
                (requireActivity() as HomeActivity).binding.llHome.performClick()
                (requireActivity() as HomeActivity).removeFragment()
                (requireActivity() as HomeActivity).faithBuilderDialog()

            }
            tvSkip.setOnClickListener {
                (requireActivity() as HomeActivity).removeFragment()
                (requireActivity() as HomeActivity).faithBuilderDialog()
            }

        }
    }
}