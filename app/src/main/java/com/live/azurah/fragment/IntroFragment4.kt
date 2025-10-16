package com.live.azurah.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.live.azurah.activity.LoginActivity
import com.live.azurah.activity.SignUpActivity
import com.live.azurah.databinding.FragmentIntro4Binding

class IntroFragment4 : Fragment() {
    private lateinit var binding: FragmentIntro4Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIntro4Binding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    private fun initListener() {
        with(binding){
            btnStart.setOnClickListener{
                startActivity(Intent(requireActivity(),SignUpActivity::class.java))
            }
            tvSignIn.setOnClickListener{
                startActivity(Intent(requireActivity(),LoginActivity::class.java))
            }
        }
    }
}