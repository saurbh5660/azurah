package com.live.azurah.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.activity.ChallangeActivity
import com.live.azurah.activity.GroupActivity
import com.live.azurah.activity.HomeActivity
import com.live.azurah.activity.QuestActivity
import com.live.azurah.activity.RequestActivity
import com.live.azurah.adapter.SongAdapter
import com.live.azurah.databinding.FragmentDashBoardBinding
import com.live.azurah.model.DashBoardResponse
import com.live.azurah.model.DashboardDataResposne
import com.live.azurah.model.SongModel
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getCurrentDate
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashBoardFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentDashBoardBinding
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var songAdapter : SongAdapter? = null
    private var weekAdapter : SongAdapter? = null
    private var songList = ArrayList<SongModel>()
    private var weekList = ArrayList<SongModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashBoardBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        initListener()
        binding.shimmerLayout.visible()
        binding.shimmerLayout.startShimmer()
        getDashboard()
        getDashboardData()
    }

    private fun getDashboard(){
        val map = HashMap<String, String>()
        map["page"] = "1"
        map["limit"] = "20"
        map["todayDate"] = getCurrentDate()
        viewModel.dashboardList(map,requireActivity()).observe(viewLifecycleOwner,this)
    }

    private fun getDashboardData(){
        val map = HashMap<String, String>()
        map["page"] = "1"
        map["limit"] = "20"
        viewModel.dashboardData(map,requireActivity()).observe(viewLifecycleOwner){value->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is DashboardDataResposne -> {
                            val res = value.data.body
                            with(binding){
                                tvAvailable.text =  res?.allResult?.getOrNull(0)?.title ?: ""
                                tvExplore.text =  res?.allResult?.getOrNull(0)?.description ?: ""
                                ivBible.loadImage(ApiConstants.IMAGE_BASE_URL+res?.allResult?.getOrNull(0)?.image)

                                tvCommunityForum.text =  res?.allResult?.getOrNull(1)?.title ?: ""
                                tvCommuDesc.text =  res?.allResult?.getOrNull(1)?.description ?: ""
                                ivComm.loadImage(ApiConstants.IMAGE_BASE_URL+res?.allResult?.getOrNull(1)?.image)

//                                tvGroupForum.text =  res?.allResult?.getOrNull(2)?.title ?: ""
//                                tvGroupDesc.text =  res?.allResult?.getOrNull(2)?.description ?: ""
//                                ivGroup.loadImage(ApiConstants.IMAGE_BASE_URL+res?.allResult?.getOrNull(2)?.image)

                                tvRequest.text =  res?.allResult?.getOrNull(2)?.title ?: ""
                                tvRequestMessage.text =  res?.allResult?.getOrNull(2)?.description ?: ""
                                ivRequest.loadImage(ApiConstants.IMAGE_BASE_URL+res?.allResult?.getOrNull(2)?.image)

                                tvTestimonies.text =  res?.allResult?.getOrNull(3)?.title ?: ""
                                tvRequestTestimonies.text =  res?.allResult?.getOrNull(3)?.description ?: ""
                                ivTestimonies.loadImage(ApiConstants.IMAGE_BASE_URL+res?.allResult?.getOrNull(3)?.image)

                            }
                        }
                    }
                }
                Status.LOADING -> {
                   LoaderDialog.dismiss()
                }
                Status.ERROR -> {
                   LoaderDialog.dismiss()
                    showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                }
            }
        }
    }

    private fun initListener() {
        with(binding){
            clCommunity.setOnClickListener {
                startActivity(Intent(requireActivity(),QuestActivity::class.java))
            }
            clGroup.setOnClickListener {
                startActivity(Intent(requireActivity(),GroupActivity::class.java))
            }

            clBibleQuest.setOnClickListener {
                startActivity(Intent(requireActivity(),ChallangeActivity::class.java))
            }
            clPrayerRequest.setOnClickListener {
                startActivity(Intent(requireActivity(),RequestActivity::class.java).apply {
                    putExtra("type","0")
                })
            }
            clTestimonies.setOnClickListener {
                startActivity(Intent(requireActivity(),RequestActivity::class.java).apply {
                    putExtra("type","1")

                })
            }
            tvSongViewMore.setOnClickListener {
                with(requireActivity() as HomeActivity){
                    val fragment = SongFragment().apply {
                        arguments = Bundle().apply {
                            putInt("type",1)
                        }
                    }
                    supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, fragment).addToBackStack(null).commit()
                }
            }

            tvSongWeekViewMore.setOnClickListener {
                with(requireActivity() as HomeActivity){
                    val fragment = SongFragment().apply {
                        arguments = Bundle().apply {
                            putInt("type",2)
                        }
                    }
                    supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, fragment).addToBackStack(null).commit()
                }
            }
        }
    }
    private fun setAdapter() {
         songAdapter = SongAdapter(requireContext(),songList, 1)
         binding.rvDaySong.adapter = songAdapter

         weekAdapter = SongAdapter(requireContext(),weekList,2)
         binding.rvSong.adapter = weekAdapter
    }
    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                binding.shimmerLayout.gone()
                binding.shimmerLayout.stopShimmer()
                binding.nestedScrollView.visible()
               LoaderDialog.dismiss()
                when (value.data) {
                    is DashBoardResponse -> {
                        val res = value.data.body
                        with(binding){
                            tvBible.text =  (res?.bibleVerse?.data?.firstOrNull()?.title ?: "").trim()
                            tvMessage.text =  (res?.bibleVerse?.data?.firstOrNull()?.description ?: "").trim()
                            tvVerse.text =  (res?.bibleVerse?.data?.firstOrNull()?.version ?: "").trim()

                            songList.clear()
                            weekList.clear()

                            val songFilter = res?.songOfTheDay?.data?.map { SongModel(
                               songName = it.name,
                                artistName = it.singer_name,
                                song = it.music,
                                image = it.image
                            )}
                            val weekFilter = res?.songOfTheWeek?.data?.map { SongModel(
                                songName = it.name,
                                artistName = it.singer_name,
                                song = it.music,
                                image = it.image
                            )}
                            songList.addAll(songFilter ?: ArrayList())
                            weekList.addAll(weekFilter ?: ArrayList())
                            setAdapter()

                            if (songList.isEmpty()){
                                ivSong.gone()
                                tvSong.gone()
                                tvSongViewMore.gone()
                                clSongDay.gone()
                            }else{
                                ivSong.visible()
                                tvSong.visible()
                                tvSongViewMore.visible()
                                clSongDay.visible()
                            }

                            if (weekList.isEmpty()){
                                ivSongWeek.gone()
                                tvSongWeek.gone()
                                tvSongWeekViewMore.gone()
                                clSongWeek.gone()
                            }else{
                                ivSongWeek.visible()
                                tvSongWeek.visible()
                                tvSongWeekViewMore.visible()
                                clSongWeek.visible()
                            }

                        }
                    }
                }
            }
            Status.LOADING -> {
//                LoaderDialog.show(this)
            }
            Status.ERROR -> {
                binding.shimmerLayout.gone()
                binding.shimmerLayout.stopShimmer()
               LoaderDialog.dismiss()
                showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.ivProfile.loadImage(ApiConstants.IMAGE_BASE_URL+ getPreference("image",""),placeholder = R.drawable.profile_icon)
        if (getPreference("displayNamePreference",1) == 1){
            binding.tvTime.text = buildString {
                append("Hey, ")
                append(getPreference("firstName",""))
                append("!")
            }
        }else{
            binding.tvTime.text = buildString {
                append("Hey, ")
                append(getPreference("firstName",""))
                /*append(" ")
                append(getPreference("lastName",""))*/
                append("!")

            }
        }
    }

}