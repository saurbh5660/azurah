package com.live.azurah.fragment

import HashtagAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.live.azurah.R
import com.live.azurah.activity.AddPostCaptionActivity
import com.live.azurah.databinding.FragmentHashTagBinding
import com.live.azurah.model.HashTagResponse

class HashTagFragment : Fragment() {
    private lateinit var binding: FragmentHashTagBinding
    private lateinit var hashtagAdapter: HashtagAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHashTagBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHashtagAdapter()
        binding.backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupHashtagAdapter() {
        hashtagAdapter = HashtagAdapter(ArrayList()) { hashtag ->
            with(requireActivity() as AddPostCaptionActivity){
                insertHashtagIntoEditText(hashtag.name ?: "")
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
        var list = ArrayList<HashTagResponse.Body.Data>()
        with(requireActivity() as AddPostCaptionActivity){
         list = hashTagList
        }
        binding.rvHashTag.adapter = hashtagAdapter
        hashtagAdapter.updateList(list)

    }
}