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
import com.live.azurah.activity.ShopDetailFragment
import com.live.azurah.adapter.ShopAdapter
import com.live.azurah.databinding.FragmentCategoryDetailBinding
import com.live.azurah.model.AddWishlistResponse
import com.live.azurah.model.ProductResponse
import com.live.azurah.model.ShopBannerResponse
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
class CategoryDetailFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentCategoryDetailBinding
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var productList = ArrayList<ProductResponse.Body.Data>()
    private var id = ""
    private var name = ""
    private var productAdapter: ShopAdapter? = null
    private var showDialog = false
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoryDetailBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        id = arguments?.getString("id") ?: ""
        name = arguments?.getString("name") ?: ""
        binding.tvSongWeek.text = name
        setAdapter()
        initListener()
//        showDialog = true
        resetPage = true
        binding.shimmerLayout.visible()
        binding.clSongWeek.gone()
        binding.shimmerLayout.startShimmer()
        getProduct()
    }

    private fun setAdapter() {
        productAdapter = ShopAdapter(requireContext(), productList)
        binding.rvProducts.adapter = productAdapter

        productAdapter?.heartListener = { pos, model ->
            val map = HashMap<String, String>()
            map["product_id"] = model.id.toString()
            map["category_id"] = model.product_category_id.toString()
            if (model.is_wishlist == 1) {
                map["status"] = "0"
            } else {
                map["status"] = "1"
            }
            viewModel.addWishList(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is AddWishlistResponse -> {
                                productList[pos].is_wishlist =
                                    (value?.data.body?.status ?: "0").toInt()
                                productAdapter?.notifyItemChanged(pos)
                            }
                        }
                    }

                    Status.LOADING -> {
                        LoaderDialog.show(requireActivity())
                    }

                    Status.ERROR -> {
                       LoaderDialog.dismiss()
                        showCustomSnackbar(
                            requireActivity(),
                            binding.root,
                            value.message.toString()
                        )
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

    private fun getProduct() {
        isApiRunning = true
        if (resetPage){
            currentPage = 1
        }
        val map = HashMap<String, String>()
        map["page"] = currentPage.toString()
        map["limit"] = "10"
        map["category_id"] = id
        viewModel.getProduct(map, requireActivity()).observe(viewLifecycleOwner,this)
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
               LoaderDialog.dismiss()
                binding.shimmerLayout.stopShimmer()
                binding.shimmerLayout.gone()
                binding.clSongWeek.visible()
                binding.swipeRefreshLayout.isRefreshing = false
                when (value.data) {
                    is ProductResponse -> {
                        if (resetPage) {
                            productList.clear()
                        }
                        binding.tvSongWeek.text = name+" (${value.data?.body?.total_count})"
                        value.data.body?.data?.let { productList.addAll(it) }
                        productAdapter?.notifyDataSetChanged()

                        if (productList.isEmpty()){
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


    private fun initListener() {
        with(binding){
            rvProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (!recyclerView.canScrollVertically(1)) {
                        if (currentPage <= totalPageCount && !isApiRunning) {
                            resetPage = false
                            showDialog = false
                            getProduct()
                        }
                    }
                }
            })

            swipeRefreshLayout.setOnRefreshListener {
                resetPage = true
                showDialog = false
                getProduct()
                swipeRefreshLayout.isRefreshing = true
            }
            backIcon.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }
    private fun replaceFragment(fragment: Fragment){
        with(requireActivity() as HomeActivity){
            supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, fragment).addToBackStack(null).commit()
        }
    }

}