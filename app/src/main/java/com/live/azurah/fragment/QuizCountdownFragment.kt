package com.live.azurah.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.live.azurah.activity.BibleQuizActivity
import com.live.azurah.databinding.FragmentQuizCountdownBinding

class QuizCountdownFragment : Fragment() {
    private var _binding: FragmentQuizCountdownBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private var value = 3

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuizCountdownBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top, bottom = bars.bottom)
            insets
        }
        binding.ivBack.setOnClickListener { activity?.finish() }
        tick()
    }

    private fun tick() {
        binding.tvCountdown.text = value.toString()
        if (value > 1) {
            value--
            handler.postDelayed({ tick() }, 1000)
        } else {
            handler.postDelayed({
                (activity as? BibleQuizActivity)?.showQuestionFragment()
            }, 1000)
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        _binding = null
        super.onDestroyView()
    }
}
