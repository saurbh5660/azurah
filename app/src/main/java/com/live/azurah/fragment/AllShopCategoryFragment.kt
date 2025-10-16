package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.activity.HomeActivity
import com.live.azurah.adapter.AllShopCategoryAdapter
import com.live.azurah.adapter.ShopCategoryAdapter
import com.live.azurah.databinding.FragmentAllShopCategoryBinding
import com.live.azurah.model.ShopCategoryResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AllShopCategoryFragment : Fragment(),
    Observer<Resource<Any>> {
    private lateinit var binding: FragmentAllShopCategoryBinding
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var categoryList = ArrayList<ShopCategoryResponse.Body.Data>()
    private var categoryAdapter : AllShopCategoryAdapter? = null
    private var showDialog = false
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllShopCategoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showDialog = true
        resetPage = true
        binding.shimmerLayout.visible()
        binding.clSongWeek.gone()
        binding.shimmerLayout.startShimmer()
        getCategory()
        setCatAdapter()
        initListener()
    }

    private fun getCategory(){
        isApiRunning = true
        if (resetPage){
            currentPage = 1
        }
        val map = HashMap<String,String>()
        map["page"] = currentPage.toString()
        map["limit"] = "100"
        viewModel.getShopCategory(map,requireActivity()).observe(viewLifecycleOwner,this)
    }

    private fun initListener() {
        with(binding){
            rvCategories.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (!recyclerView.canScrollVertically(1)) {
                        if (currentPage <= totalPageCount && !isApiRunning) {
                            resetPage = false
                            showDialog = false
                            getCategory()
                        }
                    }
                }
            })

            swipeRefreshLayout.setOnRefreshListener {
                resetPage = true
                showDialog = false
                getCategory()
                swipeRefreshLayout.isRefreshing = true
            }
            backIcon.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun setCatAdapter() {
        categoryAdapter = AllShopCategoryAdapter(requireContext(),categoryList)
        binding.rvCategories.adapter = categoryAdapter

        categoryAdapter?.clickListener = {model, pos ->
            with(requireActivity() as HomeActivity){
                val fragment = CategoryDetailFragment()
                val bundle = Bundle()
                bundle.putString("id",model.id.toString())
                bundle.putString("name",model.name.toString())
                fragment.arguments = bundle
                supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, fragment).addToBackStack(null).commit()
            }
        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
//               LoaderDialog.dismiss()
                binding.swipeRefreshLayout.isRefreshing = false
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.gone()
                binding.clSongWeek.visible()
                when (value.data) {
                    is ShopCategoryResponse -> {
                        if (resetPage) {
                            categoryList.clear()
                        }
                        value.data.body?.data?.let { categoryList.addAll(it) }
                        categoryAdapter?.notifyItemRangeChanged(0,categoryList.size)

                        if (categoryList.isEmpty()){
                            binding.tvNoDataFound.visible()
                        }else{
                            binding.tvNoDataFound.gone()
                            currentPage = (value.data.body?.current_page?:0) + 1
                        }
                        totalPageCount = value.data.body?.total_pages ?: 0

                    }
                }
            }

            Status.LOADING -> {
              /*  if (showDialog){
                    LoaderDialog.show(this)
                }*/
            }

            Status.ERROR -> {
                isApiRunning = false
               LoaderDialog.dismiss()
                binding.swipeRefreshLayout.isRefreshing = false
                showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.gone()
            }
        }
    }

}