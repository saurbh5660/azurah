package com.live.azurah.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.live.azurah.activity.BibleQuizActivity
import com.live.azurah.databinding.FragmentBibleBinding

class BibleFragment : Fragment() {
    private lateinit var binding: FragmentBibleBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBibleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.quizCard.tvAboutQuiz.setOnClickListener {
            AboutQuizBottomSheet().show(childFragmentManager, AboutQuizBottomSheet::class.java.simpleName)
        }
        binding.quizCard.tvStartQuiz.setOnClickListener {
            startActivity(Intent(requireActivity(), BibleQuizActivity::class.java))
        }
    }
}
