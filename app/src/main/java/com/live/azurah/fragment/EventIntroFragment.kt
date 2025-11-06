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
import com.live.azurah.databinding.FragmentEventIntroBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class EventIntroFragment : Fragment() {

    private lateinit var binding: FragmentEventIntroBinding
    private var backPressedTime: Long = 0
    private val doubleBackToExitDuration: Long = 2000
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventIntroBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){
            ivNext.setOnClickListener {
                (requireActivity() as HomeActivity).binding.llShop.performClick()
                (requireActivity() as HomeActivity).replaceIntroFragment(ShopIntroFragment())
            }
            tvSkip.setOnClickListener {
                (requireActivity() as HomeActivity).removeFragment()
                (requireActivity() as HomeActivity).faithBuilderDialog()
            }


        }

    }

}