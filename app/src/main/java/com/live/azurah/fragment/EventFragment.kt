package com.live.azurah.fragment


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.live.azurah.activity.BookmarkEventActivity
import com.live.azurah.adapter.CategoryAdapter
import com.live.azurah.adapter.EventAdapter
import com.live.azurah.adapter.EventPopularAdapter
import com.live.azurah.adapter.EventUpcomingAdapter
import com.live.azurah.databinding.FragmentEventBinding
import com.live.azurah.model.AddBookmarkResponse
import com.live.azurah.model.CategoryModel
import com.live.azurah.model.CountResponse
import com.live.azurah.model.EventCategoryResponse
import com.live.azurah.model.EventListResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentEventBinding
    private val catList = ArrayList<CategoryModel>()
    private val upcomingList = ArrayList<EventListResponse.Body.UpcomingEvents.Data>()
    private val popularList = ArrayList<EventListResponse.Body.PopularEvents.Data>()
    private val recommendedList = ArrayList<EventListResponse.Body.RecommendedEvents.Data>()
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var adapter: CategoryAdapter? = null
    private var eventUpcomingAdapter: EventAdapter? = null
    private var eventPopularAdapter: EventPopularAdapter? = null
    private var eventRecommendedAdapter: EventUpcomingAdapter? = null
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
//        getCategoryList()
        binding.rvEvents.gone()
        binding.shimmerLayout.visible()
        binding.shimmerLayout.startShimmer()
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        getEvents()

        binding.tvRecommendationsSeeAll.setOnClickListener {
            startActivity(Intent(requireActivity(), BookmarkEventActivity::class.java).apply {
                putExtra("from","2")
            })
        }

        binding.tvUpcomingViewMore.setOnClickListener {
            startActivity(Intent(requireActivity(), BookmarkEventActivity::class.java).apply {
                putExtra("from","0")
            })
        }

        binding.tvPopularSeeAll.setOnClickListener {
            startActivity(Intent(requireActivity(), BookmarkEventActivity::class.java).apply {
                putExtra("from","1")
            })
        }

      binding.etSearch.setOnClickListener {
          startActivity(Intent(requireActivity(), BookmarkEventActivity::class.java).apply {
              putExtra("title","All")
              putExtra("id","0")
              putExtra("from","3")
          })
      }
    }

    private fun getCategoryList() {
        viewModel.eventCategoryList(requireActivity()).observe(viewLifecycleOwner, this)
    }

    private fun getEvents() {
        val map = HashMap<String,String>()
        map["type"] = "0"
        map["page"] = "1"
        map["limit"] = "50"
        map["category_id"] = "0"

        viewModel.eventList(map,requireActivity()).observe(viewLifecycleOwner, this)
    }

    private fun setAdapter() {
        eventRecommendedAdapter = EventUpcomingAdapter(requireContext(),recommendedList)
        binding.rvCurrentEvents.adapter = eventRecommendedAdapter

        eventPopularAdapter = EventPopularAdapter(requireContext(),popularList)
        binding.rvPopularEvents.adapter = eventPopularAdapter

        eventPopularAdapter?.bookmarkListener = { pos, model ->
            val map = HashMap<String, String>()
            map["event_id"] = model.id.toString()
            if (model.isBookmark == 1) {
                map["status"] = "0"
            } else {
                map["status"] = "1"
            }
            viewModel.eventBookmark(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is AddBookmarkResponse -> {
                                popularList[pos].isBookmark =
                                    (value?.data.body?.isBooking ?: "0").toString().toInt()
                                eventPopularAdapter?.notifyItemChanged(pos)
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

        eventUpcomingAdapter = EventAdapter(requireContext(),upcomingList)
        binding.rvEvents.adapter = eventUpcomingAdapter

        adapter = CategoryAdapter(requireContext(),catList,1)
        binding.rvCategory.adapter = adapter
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is EventCategoryResponse -> {
                        getEvents()
                        catList.clear()
                        value.data.body?.data?.let {
                           val list =  it.map { CategoryModel(name = it.name.toString(), id = it.id.toString()) }
                            catList.addAll(list)
//                            catList.firstOrNull()?.isSelected = true
                        }
                        adapter?.notifyDataSetChanged()
                    }
                    is EventListResponse -> {
                        binding.shimmerLayout.gone()
                        binding.rvEvents.visible()
                        binding.shimmerLayout.stopShimmer()
                        upcomingList.clear()
                        popularList.clear()
                        recommendedList.clear()
                        value.data.body?.let {
                            it.upcomingEvents?.data?.let { it1 -> upcomingList.addAll(it1) }

                            with(binding){
                                if (it.isShowEventPopular == "0"){
                                    tvPopular.gone()
                                    tvPopularSeeAll.gone()
                                    rvPopularEvents.gone()
                                }else{
                                    tvPopular.visible()
                                    tvPopularSeeAll.visible()
                                    rvPopularEvents.visible()
                                    it.popularEvents?.data?.let { it1 -> popularList.addAll(it1) }
                                    eventPopularAdapter?.notifyDataSetChanged()

                                }

                                if (it.isShowEventRecommended == "0"){
                                    tvRecommended.gone()
                                    tvRecommendationsSeeAll.gone()
                                    rvCurrentEvents.gone()
                                }else{
                                    tvRecommended.visible()
                                    tvRecommendationsSeeAll.visible()
                                    rvCurrentEvents.visible()
                                    it.recommendedEvents?.data?.let { it1 -> recommendedList.addAll(it1) }
                                    eventRecommendedAdapter?.notifyDataSetChanged()
                                }
                            }
                        }
                        eventUpcomingAdapter?.notifyDataSetChanged()


                        if (upcomingList.isEmpty()){
                            binding.tvNoDataFound.visible()
                            binding.tvUpcoming.gone()
                        }else{
                            binding.tvNoDataFound.gone()
                            binding.tvUpcoming.visible()
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
                binding.shimmerLayout.gone()
                binding.shimmerLayout.stopShimmer()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.getCounts(requireActivity()).observe(viewLifecycleOwner){
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