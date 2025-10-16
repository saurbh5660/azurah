package com.live.azurah.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.ScaleAnimation
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.live.azurah.R
import com.live.azurah.activity.HomeActivity
import com.live.azurah.activity.ShopDetailFragment
import com.live.azurah.adapter.ShopAdapter
import com.live.azurah.adapter.ShopCategoryAdapter
import com.live.azurah.adapter.ShopSliderAdapter
import com.live.azurah.databinding.FragmentShopBinding
import com.live.azurah.model.AddWishlistResponse
import com.live.azurah.model.CountResponse
import com.live.azurah.model.ProductResponse
import com.live.azurah.model.ShopBannerModel
import com.live.azurah.model.ShopBannerResponse
import com.live.azurah.model.ShopCategoryResponse
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
class ShopFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentShopBinding
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var bannerList = ArrayList<ShopBannerResponse.Body.Data>()
    private var categoryList = ArrayList<ShopCategoryResponse.Body.Data>()
    private var productList = ArrayList<ProductResponse.Body.Data>()
    private var categoryAdapter: ShopCategoryAdapter? = null
    private var productAdapter: ShopAdapter? = null
    private lateinit var receiver : BroadcastReceiver
    private var showDialog = false
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    private lateinit var sharedViewModel: SharedViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        setCatAdapter()
        initListener()
        showDialog = false
        binding.shimmerLayout.visible()
        binding.shimmerLayout.startShimmer()
        getBanner()

        receiver = object : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (p1?.action == "Shop"){
                    showDialog = false
                    resetPage = true
                    getProduct()
                    getCount()
                }
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, IntentFilter("Shop"))

    }

    private fun initListener() {
        with(binding) {
            etSearch.setOnClickListener {
                with(requireActivity() as HomeActivity) {
                    binding.viewPager.setCurrentItem(5, false)
                }
            }
            tvViewAll.setOnClickListener {
                replaceFragment(AllShopCategoryFragment())
            }
          /*  binding.nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, oldScrollY ->
                if (v.getChildAt(v.childCount - 1) != null) {
                    if ((scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight)) &&
                        scrollY > oldScrollY
                    ) {
                        if (currentPage <= totalPageCount && !isApiRunning) {
                            resetPage = false
                            showDialog = false
                            getProduct()
                        }
                    }
                }
            })*/
        }
    }

    private fun getBanner() {
        val map = HashMap<String, String>()
        map["page"] = "1"
        map["limit"] = "10"
        viewModel.getBanner(map, requireActivity()).observe(viewLifecycleOwner, this)
    }

    private fun getCategory() {
        val map = HashMap<String, String>()
        map["page"] = "1"
        map["limit"] = "50"
        viewModel.getShopCategory(map, requireActivity()).observe(viewLifecycleOwner, this)
    }

    private fun getProduct() {
        isApiRunning = true
        if (resetPage){
            currentPage = 1
        }
        val map = HashMap<String, String>()
        map["page"] = currentPage.toString()
        map["limit"] = "10"
//        map["is_popular"] = "1"
//        map["is_best_seller"] = "1"
        map["category_id"] = "0"
        viewModel.getProduct(map, requireActivity()).observe(viewLifecycleOwner) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is ProductResponse -> {
                            binding.shimmerLayout.gone()
                            binding.nestedScrollView.visible()
                            binding.shimmerLayout.stopShimmer()
                            if (resetPage) {
                                productList.clear()
                            }
                            value.data.body?.data?.let { productList.addAll(it) }
                            productAdapter?.notifyDataSetChanged()
                            currentPage = (value.data.body?.current_page?:0) + 1
                            totalPageCount =  value.data.body?.total_pages ?: 0
                        }
                    }
                }

                Status.LOADING -> {
                    if (showDialog){
                        LoaderDialog.show(requireActivity())
                    }
                }

                Status.ERROR -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                    binding.shimmerLayout.gone()
                    binding.nestedScrollView.gone()
                    binding.shimmerLayout.stopShimmer()

                }
            }
        }
    }

    private fun setCatAdapter() {
        categoryAdapter = ShopCategoryAdapter(requireContext(), categoryList)
        binding.rvCategory.adapter = categoryAdapter

        categoryAdapter?.categoryClickListener = {pos, model ->
            val bundle = Bundle()
            bundle.putString("id",model.id.toString())
            bundle.putString("name",model.name)
            val fragment = CategoryDetailFragment().apply {
                arguments = bundle
            }
            replaceFragment(fragment)

        }

        productAdapter = ShopAdapter(requireContext(), productList)
        binding.rvProducts.adapter = productAdapter

        productAdapter?.heartListener = { pos, model ->
            val map = HashMap<String, String>()
            map["product_id"] = model.id.toString()
            map["category_id"] = model.product_category_id.toString()
            map["status"] = model.is_wishlist.toString()
            viewModel.addWishList(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is AddWishlistResponse -> {
                              /*  productList[pos].is_wishlist =
                                    (value?.data.body?.status ?: "0").toInt()
                                productAdapter?.notifyItemChanged(pos)*/
                                getCount()
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

        productAdapter?.productClickListener = {pos, model ->
            val bundle = Bundle()
            bundle.putString("id",model.id.toString())
            val fragment = ShopDetailFragment().apply {
                arguments = bundle
            }
            replaceFragment(fragment)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        with(requireActivity() as HomeActivity) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, fragment).addToBackStack(null).commit()
        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                when (value.data) {
                    is ShopBannerResponse -> {
                        getCategory()
                        bannerList.clear()
                        value.data.body?.data?.let { bannerList.addAll(it) }
                        setSliderAdapter()
                    }

                    is ShopCategoryResponse -> {
                        showDialog = false
                        resetPage = true
                        getProduct()
                        categoryList.clear()
                        value.data.body?.data?.let { categoryList.addAll(it) }
                        categoryAdapter?.notifyItemRangeChanged(0, categoryList.size)
                    }
                }
            }

            Status.LOADING -> {
                if (showDialog){
                    LoaderDialog.show(requireActivity())
                }

            }

            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(requireActivity(), binding.root, value.message.toString())

            }
        }
    }

    private fun setSliderAdapter() {
        with(binding) {

            val modelList = bannerList.map {
                ShopBannerModel(
                    image = it.image,
                    id = it.id,
                    type = 0
                )
            } as ArrayList<ShopBannerModel>

            ivTrainerImage.offscreenPageLimit = modelList.size
            ivTrainerImage.adapter = ShopSliderAdapter(requireContext(), modelList)
            TabLayoutMediator(tabLayout, ivTrainerImage) { tab, _ ->
                tab.setIcon(R.drawable.dot_unselected)
            }.attach()

            if (modelList.size > 1){
                tabLayout.visible()
            }else{
                tabLayout.gone()
            }

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    tab.setIcon(R.drawable.dot_selected)
                    val scaleUp = ScaleAnimation(
                        1.0f, 1.0f,  // Start and end X scale
                        1.0f, 1.0f,  // Start and end Y scale
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f
                    ).apply {
                        duration = 200
                        fillAfter = true
                    }
                    tab.view.startAnimation(scaleUp)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    tab.setIcon(R.drawable.dot_unselected)
                    val scaleDown = ScaleAnimation(
                        1.0f, 1.0f,  // Start and end X scale
                        1.0f, 1.0f,  // Start and end Y scale
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f
                    ).apply {
                        duration = 200
                        fillAfter = true
                    }
                    tab.view.startAnimation(scaleDown)
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                }
            })
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }

    override fun onResume() {
        super.onResume()
        getCount()
    }

    private fun getCount(){
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