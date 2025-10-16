package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.live.azurah.R
import com.live.azurah.activity.ChallangeDetailActivity
import com.live.azurah.databinding.FragmentOverviewBinding
import com.live.azurah.model.BibleQuestViewModel
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        sharedViewModel.bibleQuestDetail.observe(viewLifecycleOwner){
           setData(it)
        }

    }


    override fun onResume() {
        super.onResume()
        setData(sharedViewModel.bibleQuestDetail.value)
    }

    private fun setData(data: BibleQuestViewModel.Body?){
        data?.let {
            binding.tvOverview.text = buildString {
                append(it.description ?: "")
            }
        }
    }


}