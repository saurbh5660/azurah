package com.live.azurah.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.activity.HomeActivity
import com.live.azurah.activity.ShopDetailFragment
import com.live.azurah.adapter.FavouriteAdapter
import com.live.azurah.databinding.FragmentFavouriteBinding
import com.live.azurah.model.AddWishlistResponse
import com.live.azurah.model.WishlistResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class FavouriteFragment : Fragment(), Observer<Resource<Any>> {
 private lateinit var binding: FragmentFavouriteBinding
    private var list = ArrayList<WishlistResponse.Body.Data>()
    private var from = ""
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var favouriteAdapter: FavouriteAdapter? = null
    private var search = ""
    private lateinit var receiver : BroadcastReceiver
    private var showDialog = false
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavouriteBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showDialog = true
//        getWishList()

        receiver = object : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (p1?.action == "Shop"){
                    showDialog = false
                    resetPage = true
                    getWishList()
                }
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, IntentFilter("Shop"))
        from = arguments?.getString("from") ?: ""
        if (from == "1"){
            binding.toolbar.visibility = View.VISIBLE
        }else{
            binding.toolbar.visibility = View.GONE
        }
        setFavouriteAdapter()
        initListener()
    }
    private fun setFavouriteAdapter() {
        favouriteAdapter = FavouriteAdapter(requireContext(),list)
        binding.rvProducts.adapter = favouriteAdapter

        favouriteAdapter?.heartListener = { pos, model ->
            val map = HashMap<String, String>()
            map["product_id"] = model.product?.id.toString()
            map["category_id"] = model.product?.productCategoryId.toString()
            map["status"] = "0"
            viewModel.addWishList(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is AddWishlistResponse -> {
                               list.removeAt(pos)
                                favouriteAdapter?.notifyItemRemoved(pos)
                                if (list.isEmpty()){
                                    binding.tvNoDataFound.visible()
                                }else{
                                    binding.tvNoDataFound.gone()
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

        favouriteAdapter?.productClickListener = {pos, model ->
            val bundle = Bundle()
            bundle.putString("id",model.product?.id.toString())
            val fragment = ShopDetailFragment().apply {
                arguments = bundle
            }
            replaceFragment(fragment)
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
                            getWishList()
                        }
                    }
                }
            })

            swipeRefreshLayout.setOnRefreshListener {
                resetPage = true
                showDialog = false
                getWishList()
                swipeRefreshLayout.isRefreshing = true
            }

            backIcon.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
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
                                resetPage = true
                                binding.shimmerLayout.visible()
                                binding.tvNoDataFound.gone()
                                binding.swipeRefreshLayout.gone()
                                binding.shimmerLayout.startShimmer()
                                getWishList()
                            }
                        }
                    }, delay)
                }

            })
        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
                binding.shimmerLayout.gone()
//               LoaderDialog.dismiss()
                binding.shimmerLayout.stopShimmer()
                binding.swipeRefreshLayout.visible()
                binding.swipeRefreshLayout.isRefreshing = false

                when (value.data) {
                    is WishlistResponse -> {
                        if (resetPage) {
                            list.clear()
                        }
                        value.data.body?.data?.let { list.addAll(it) }
                        favouriteAdapter?.notifyDataSetChanged()
                        if (list.isEmpty()){
                            binding.tvNoDataFound.visible()
                        }else{
                            binding.tvNoDataFound.gone()
                            currentPage = (value.data.body?.currentPage?:0) + 1
                        }
                        totalPageCount = value.data.body?.totalPages ?: 0
                    }
                }
            }

            Status.LOADING -> {
               /* if (showDialog){
                    LoaderDialog.show(this)
                }*/
            }

            Status.ERROR -> {
                isApiRunning = false
               LoaderDialog.dismiss()
                binding.swipeRefreshLayout.isRefreshing = false
                showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                binding.shimmerLayout.gone()
                binding.shimmerLayout.stopShimmer()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        with(requireActivity() as HomeActivity) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, fragment).addToBackStack(null).commit()
        }
    }

    private fun getWishList() {
        isApiRunning = true
        if (resetPage){
            currentPage = 1
        }
        val map = HashMap<String, String>()
        map["page"] = currentPage.toString()
        map["limit"] = "10"
        map["search_string"] = search
        viewModel.getWishList(map, requireActivity()).observe(viewLifecycleOwner, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }

    override fun onResume() {
        super.onResume()
        resetPage = true
        showDialog = false
        getWishList()
        binding.shimmerLayout.visible()
        binding.swipeRefreshLayout.gone()
        binding.shimmerLayout.startShimmer()
    }
}