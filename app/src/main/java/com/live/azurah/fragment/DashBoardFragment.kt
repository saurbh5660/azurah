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
import androidx.lifecycle.ViewModelProvider
import android.graphics.Color
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.live.azurah.R
import com.live.azurah.activity.ChallangeActivity
import com.live.azurah.activity.GroupActivity
import com.live.azurah.activity.HomeActivity
import com.live.azurah.activity.QuestActivity
import com.live.azurah.activity.RequestActivity
import com.live.azurah.activity.StreakHistoryActivity
import com.live.azurah.adapter.SongAdapter
import com.live.azurah.databinding.FragmentDashBoardBinding
import com.live.azurah.model.BibleQuestStreakResponse
import com.live.azurah.model.CountResponse
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
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class DashBoardFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentDashBoardBinding
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private lateinit var sharedViewModel: SharedViewModel

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
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        binding.shimmerLayout.visible()
        binding.shimmerLayout.startShimmer()
        getDashboard()
        getDashboardData()
        getBibleQuestStreak()
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
                            // Dashboard cards use the fixed design copy/icons from the layout.
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

    private fun getBibleQuestStreak() {
        viewModel.getBibleQuestStreak(requireActivity()).observe(viewLifecycleOwner) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                    when (value.data) {
                        is BibleQuestStreakResponse -> {
                            val streakData = value.data.body
                            if (streakData != null) {
                                updateStreakUI(streakData)
                            }
                        }
                    }
                }
                Status.LOADING -> {}
                Status.ERROR -> {
                    showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                }
            }
        }
    }

    private fun updateStreakUI(data: BibleQuestStreakResponse.Body) {
        with(binding) {
            val currentStreak = data.currentStreak ?: 0
            tvStreakCount.text = currentStreak.toString()
            tvAvailable.text = if (currentStreak == 1) "🔥 Day Streak" else "🔥 Days Streak"
            tvExplore.text = "Best: ${data.bestStreak ?: 0} days"

            val daysAway = data.daysToNextLevel ?: 0
            val nextLevel = data.nextLevel ?: ""
            val firstName = getPreference("firstName", "")
            val userSuffix = if (firstName.isNotEmpty()) ", $firstName!" else "!"

            if (daysAway > 0) {
                tvQuestHint.text = "⚡ You're $daysAway days away from your next level ($nextLevel) — keep going$userSuffix"
            } else if (nextLevel.isNotEmpty()) {
                tvQuestHint.text = "⚡ Keep going$userSuffix You are on level ${data.growthLevel ?: ""}"
            }

            val daysList = data.currentWeek?.days ?: emptyList()
            for (i in 0 until minOf(7, daysList.size)) {
                val dayData = daysList[i]
                val dayView = llQuestDays.getChildAt(i) as? LinearLayout ?: continue
                val tvCircle = dayView.getChildAt(0) as? TextView ?: continue
                val tvLabel = dayView.getChildAt(1) as? TextView ?: continue

                when (dayData.status?.lowercase()) {
                    "completed" -> {
                        tvCircle.background = AppCompatResources.getDrawable(requireContext(), R.drawable.dashboard_day_filled)
                        tvCircle.text = "🔥"
                        tvCircle.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(com.intuit.sdp.R.dimen._12sdp))
                        tvLabel.text = dayData.day?.uppercase() ?: ""
                        tvLabel.setTextColor(Color.parseColor("#98A6BC"))
                    }
                    "missed" -> {
                        tvCircle.background = AppCompatResources.getDrawable(requireContext(), R.drawable.dashboard_day_missed)
                        tvCircle.text = "•"
                        tvCircle.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(com.intuit.sdp.R.dimen._18sdp))
                        tvCircle.setTextColor(Color.parseColor("#C85A32"))
                        tvLabel.text = dayData.day?.uppercase() ?: ""
                        tvLabel.setTextColor(Color.parseColor("#C85A32"))
                    }
                    "today" -> {
                        tvCircle.background = AppCompatResources.getDrawable(requireContext(), R.drawable.dashboard_day_today)
                        tvCircle.text = "•"
                        tvCircle.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(com.intuit.sdp.R.dimen._18sdp))
                        tvCircle.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
                        tvLabel.text = "TODAY"
                        tvLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
                    }
                    "upcoming" -> {
                        tvCircle.background = AppCompatResources.getDrawable(requireContext(), R.drawable.dashboard_day_empty)
                        tvCircle.text = ""
                        tvLabel.text = dayData.day?.uppercase() ?: ""
                        tvLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.day_unselected_color))
                    }
                    else -> {
                        tvCircle.background = AppCompatResources.getDrawable(requireContext(), R.drawable.dashboard_day_empty)
                        tvCircle.text = ""
                        tvLabel.text = dayData.day?.uppercase() ?: ""
                        tvLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.day_unselected_color))
                    }
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
            ivQuestCalendar.setOnClickListener {
                startActivity(Intent(requireActivity(), StreakHistoryActivity::class.java))
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
            clShop.setOnClickListener {
                (requireActivity() as HomeActivity).replaceFragment(ShopFragment())
            }
            ivHeaderShop.setOnClickListener {
                (requireActivity() as HomeActivity).replaceFragment(ShopFragment())
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
                        with(binding) {
                            val verseItem = res?.bibleVerse?.data?.firstOrNull()
                            if (verseItem != null) {
                                val desc = (verseItem.description ?: "").trim()
                                val title = (verseItem.title ?: "").trim()
                                val version = (verseItem.version ?: "").trim()

                                tvMessage.text = if (desc.isNotEmpty()) "\"$desc\"" else ""
                                tvBible.text = buildString {
                                    if (title.isNotEmpty()) append(title)
                                    if (version.isNotEmpty()) {
                                        if (isNotEmpty()) append(" ")
                                        append(version)
                                    }
                                }
                            } else {
                                tvMessage.text = ""
                                tvBible.text = ""
                            }

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
            binding.tvTime.text = getPreference("firstName","")
        }else{
            binding.tvTime.text = buildString {
                append(getPreference("firstName",""))
                append(" ")
                append(getPreference("lastName",""))

            }
        }

        viewModel.getCounts(requireActivity()).observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data) {
                        is CountResponse -> {
                            val res = it.data.body
                            sharedViewModel.setCount(res)
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    showCustomSnackbar(requireActivity(), binding.root, it.message.toString())

                }
            }
        }

    }

}