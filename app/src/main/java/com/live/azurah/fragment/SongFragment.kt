package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.adapter.SongAdapter
import com.live.azurah.databinding.FragmentSongBinding
import com.live.azurah.model.DashBoardResponse
import com.live.azurah.model.SongModel
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SongFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentSongBinding
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var songAdapter : SongAdapter? = null
    private var songList = ArrayList<SongModel>()

    private var type = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSongBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        type = arguments?.getInt("type",0) ?: 0
        if (type == 1){
            binding.tvSongWeek.text = "Songs of the Day"
            binding.ivSongWeek.setImageResource(R.drawable.song_icon)
        }
        else if (type == 2){
            binding.tvSongWeek.text = "Songs of the Week"
            binding.ivSongWeek.setImageResource(R.drawable.song_week_icon)
        }
        initListener()
        getDashboard()
    }

    private fun initListener() {
        with(binding){
            backIcon.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun getDashboard(){
        val map = HashMap<String, String>()
        map["page"] = "1"
        map["limit"] = "20"
        viewModel.dashboardList(map,requireActivity()).observe(viewLifecycleOwner,this)
    }

    private fun setAdapter() {
        songAdapter = SongAdapter(requireContext(), songList, 3)
        binding.rvSong.adapter = songAdapter
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is DashBoardResponse -> {
                        val res = value.data.body
                        with(binding){
                            songList.clear()

                            val songFilter = res?.songOfTheDay?.data?.map { SongModel(
                                songName = it.name,
                                artistName = it.singer_name,
                                song = it.music,
                                image = it.image
                            )
                            }
                            val weekFilter = res?.songOfTheWeek?.data?.map { SongModel(
                                songName = it.name,
                                artistName = it.singer_name,
                                song = it.music,
                                image = it.image
                            )
                            }
                            if(type == 1){
                                songList.addAll(songFilter ?: ArrayList())
                            }
                            else{
                                songList.addAll(weekFilter ?: ArrayList())
                            }
                            setAdapter()
                        }
                    }
                }
            }
            Status.LOADING -> {
                LoaderDialog.show(requireActivity())
            }
            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
            }
        }
    }
}