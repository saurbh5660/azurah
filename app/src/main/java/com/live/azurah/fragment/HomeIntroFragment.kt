package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.live.azurah.activity.HomeActivity
import com.live.azurah.databinding.FragmentHomeIntroBinding
import com.live.azurah.retrofit.ApiConstants

class HomeIntroFragment : Fragment() {
    private lateinit var binding: FragmentHomeIntroBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeIntroBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){
            ivNext.setOnClickListener {
                ApiConstants.isMute = false
                (requireActivity() as HomeActivity).binding.llDash.performClick()
                (requireActivity() as HomeActivity).replaceIntroFragment(DashboardIntroFragment())
            }
            tvSkip.setOnClickListener {
                ApiConstants.isMute = false
                (requireActivity() as HomeActivity).removeFragment()
                (requireActivity() as HomeActivity).faithBuilderDialog()

            }

        }
    }

}