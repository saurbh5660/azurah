package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.live.azurah.R
import com.live.azurah.databinding.FragmentIntro1Binding
import com.live.azurah.viewmodel.SharedViewModel

class IntroFragment1 : Fragment() {
    private lateinit var binding: FragmentIntro1Binding
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIntro1Binding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        initListener()
    }

    private fun initListener() {
        with(binding){
            tvSkip.setOnClickListener {
                sharedViewModel.getWalkthrough(0)
            }

            tvNext.setOnClickListener {
                sharedViewModel.getWalkthrough(1)
            }
        }
    }

}