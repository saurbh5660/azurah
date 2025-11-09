package com.live.azurah.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.activity.HomeActivity
import com.live.azurah.activity.ShopDetailFragment
import com.live.azurah.adapter.SearchShopAdapter
import com.live.azurah.databinding.FragmentSearchProductBinding
import com.live.azurah.model.AddWishlistResponse
import com.live.azurah.model.CountResponse
import com.live.azurah.model.ProductResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask


@AndroidEntryPoint
class SearchProductFragment : Fragment() {
  private lateinit var binding: FragmentSearchProductBinding
    private lateinit var sharedViewModel: SharedViewModel

  private var productList = ArrayList<ProductResponse.Body.Data>()
  private var likeProductList = ArrayList<ProductResponse.Body.Data>()
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var productAdapter: SearchShopAdapter? = null
    private var likeProductAdapter: SearchShopAdapter? = null
    private var search = ""
    private lateinit var receiver : BroadcastReceiver
    private var showDialog = false
    private var currentPage = 1
    private var totalPageCount = 0
    private var apiType = 0
    private var resetPage = false
    private var isApiRunning = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchProductBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCatAdapter()
        setLikesCatAdapter()
        initListener()
        showDialog = true
        resetPage = true
        binding.tvSongWeek.visible()
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        getLikeProduct()

        receiver = object : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (p1?.action == "Shop"){
                    showDialog = false
                    resetPage = true
                    getProduct()
                }
            }
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, IntentFilter("Shop"))

    }

    private fun initListener() {
        with(binding){

            rvProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (!recyclerView.canScrollVertically(1)) {
                        if (currentPage <= totalPageCount && !isApiRunning && apiType == 1) {
                            resetPage = false
                            showDialog = false
                            getProduct()
                        }
                    }
                }
            })

            swipeRefreshLayout.setOnRefreshListener {
                if (apiType == 1){
                    resetPage = true
                    showDialog = false
                    getProduct()
                    swipeRefreshLayout.isRefreshing = true
                }else{
                    swipeRefreshLayout.isRefreshing = false

                }

            }
            backIcon.setOnClickListener {
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent("Shop"))
                requireActivity().supportFragmentManager.popBackStack()
            }
            ivFab.setOnClickListener {
                startActivity(Intent(requireContext(),FavouriteFragment::class.java))
            }

            ivCross.setOnClickListener {
                etSearch.setText("")
            }
            etSearch.addTextChangedListener(object: TextWatcher {
                var delay : Long = 1000
                var timer = Timer()
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    timer.cancel()
                    timer.purge()
                    if (s.toString().isNotBlank()){
                        ivCross.visibility = View.VISIBLE
                    }else{
                        ivCross.visibility = View.GONE
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            lifecycleScope.launch(Dispatchers.Main){
                                search = s.toString().trim()
                                showDialog = true
                                resetPage = true
                                binding.tvSongWeek.gone()
                                getProduct()
                            }
                        }
                    }, delay)
                }
            })
        }
    }

    private fun getProduct() {
        apiType = 1
        isApiRunning = true
        if (resetPage){
            currentPage = 1
        }
        val map = HashMap<String, String>()
        map["page"] = currentPage.toString()
        map["limit"] = "10"
        map["category_id"] = "0"
        map["search_string"] = search
        viewModel.getProduct(map, requireActivity()).observe(viewLifecycleOwner) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    binding.swipeRefreshLayout.isRefreshing = false
                    when (value.data) {
                        is ProductResponse -> {
                            if (resetPage) {
                                productList.clear()
                            }
                            value.data.body?.data?.let { productList.addAll(it) }
                            productAdapter?.notifyDataSetChanged()

                            if (productList.isEmpty()){
                                binding.tvNoDataFound.visible()
                                binding.rvLikesProducts.gone()
                                binding.rvProducts.gone()
                            }else{
                                binding.tvNoDataFound.gone()
                                binding.rvLikesProducts.gone()
                                binding.rvProducts.visible()
                                currentPage = (value.data.body?.current_page?:0) + 1
                            }
                            totalPageCount = value.data.body?.total_pages ?: 0

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
                    binding.swipeRefreshLayout.isRefreshing = false
                    showCustomSnackbar(requireActivity(), binding.root, value.message.toString())

                }
            }
        }
    }

    private fun getLikeProduct() {
        apiType = 0
        isApiRunning = true
        currentPage = 1
        val map = HashMap<String, String>()
        map["page"] = "1"
        map["limit"] = "5"
        viewModel.productMayLike(map, requireActivity()).observe(viewLifecycleOwner) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is ProductResponse -> {
                            likeProductList.clear()
                            value.data.body?.data?.let { likeProductList.addAll(it) }
                            likeProductAdapter?.notifyDataSetChanged()

                            if (likeProductList.isEmpty()){
                                binding.tvNoDataFound.visible()
                                binding.rvLikesProducts.gone()
                                binding.rvProducts.gone()
                            }else{
                                binding.tvNoDataFound.gone()
                                binding.rvLikesProducts.visible()
                                binding.rvProducts.gone()
//                                currentPage = (value.data.body?.current_page?:0) + 1
                            }
//                            totalPageCount = value.data.body?.total_pages ?: 0

                        }
                    }
                }

                Status.LOADING -> {
                    LoaderDialog.show(requireActivity())
                }

                Status.ERROR -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    showCustomSnackbar(requireActivity(), binding.root, value.message.toString())

                }
            }
        }
    }

    private fun setCatAdapter() {
        productAdapter = SearchShopAdapter(requireContext(),productList)
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
                                    if (!value?.data?.body?.status.isNullOrEmpty()){
                                        (value.data?.body?.status ?: "0").toInt()
                                    }else{
                                        0
                                    }

                                productAdapter?.notifyItemChanged(pos)

                               val counts = sharedViewModel.count.value
                               var favCount = counts?.favouriteProductsCount ?: 0
                                if (productList[pos].is_wishlist == 1) {
                                    favCount = favCount +1
                                } else {
                                    favCount = favCount - 1
                                }
                                counts?.favouriteProductsCount = favCount
                                sharedViewModel.setCount(counts)
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

        productAdapter?.productClickListener = {pos, model ->
            val bundle = Bundle()
            bundle.putString("id",model.id.toString())
            val fragment = ShopDetailFragment().apply {
                arguments = bundle
            }
            replaceFragment(fragment)
        }
    }

    private fun setLikesCatAdapter() {
        likeProductAdapter = SearchShopAdapter(requireContext(),likeProductList)
        binding.rvLikesProducts.adapter = likeProductAdapter

        likeProductAdapter?.heartListener = { pos, model ->
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

                                likeProductList[pos].is_wishlist =
                                    if (!value?.data?.body?.status.isNullOrEmpty()){
                                        (value.data?.body?.status ?: "0").toInt()
                                    }else{
                                        0
                                    }

                                likeProductAdapter?.notifyItemChanged(pos)

                                val counts = sharedViewModel.count.value
                                var favCount = counts?.favouriteProductsCount ?: 0
                                if (likeProductList[pos].is_wishlist == 1) {
                                    favCount = favCount +1
                                } else {
                                    favCount = favCount - 1
                                }
                                counts?.favouriteProductsCount = favCount
                                sharedViewModel.setCount(counts)
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

        likeProductAdapter?.productClickListener = {pos, model ->
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

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }
}