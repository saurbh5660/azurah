package com.live.azurah.fragment

import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Bundle
import android.text.Layout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.live.azurah.R
import com.live.azurah.activity.ChallangeDetailActivity
import com.live.azurah.databinding.FragmentOverviewBinding
import com.live.azurah.model.BibleQuestViewModel
import com.live.azurah.util.sanitizeHtml
import com.live.azurah.viewmodel.SharedViewModel

class OverviewFragment : Fragment() {
    private lateinit var binding: FragmentOverviewBinding
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOverviewBinding.inflate(inflater,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        sharedViewModel.bibleQuestDetail.observe(viewLifecycleOwner){
           setData(it)
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        setData(sharedViewModel.bibleQuestDetail.value)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setData(data: BibleQuestViewModel.Body?){
        data?.let {
            val cleanedHtml = sanitizeHtml(it.description ?: "")
           /* binding.tvOverview.apply {
                text = HtmlCompat.fromHtml(it.description ?: "",FROM_HTML_MODE_COMPACT)
//                hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
//                justificationMode = LineBreaker.JUSTIFICATION_MODE_NONE
            }*/

            binding.tvOverview.text = buildString {
                append(it.description ?: "")
            }
        }
    }


}