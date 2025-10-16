package com.live.azurah.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.live.azurah.R
import com.live.azurah.activity.ChallangeActivity
import com.live.azurah.activity.ChallangeDetailActivity
import com.live.azurah.activity.HomeActivity
import com.live.azurah.activity.ShopDetailFragment
import com.live.azurah.adapter.AdviceAdapter
import com.live.azurah.databinding.FragmentAdviceBinding
import com.live.azurah.model.AddWishlistResponse
import com.live.azurah.model.BibleQuestViewModel
import com.live.azurah.model.ProductDetailResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.loadImage
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdviceFragment : Fragment() {
    private lateinit var binding: FragmentAdviceBinding
    private lateinit var adapter: AdviceAdapter
    private lateinit var sharedViewModel: SharedViewModel
    private var productId = ""
    private var isFav = 0
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var list = ArrayList<BibleQuestViewModel.Body.BibleQuestAdvice>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdviceBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedViewModel.bibleQuestDetail.observe(viewLifecycleOwner){
            setData(it)
        }
    }

    private fun setAdapter() {
        adapter = AdviceAdapter(requireContext(),list)
        binding.rvAdvice.adapter = adapter

        adapter.heartListener = { pos, model ->
            val map = HashMap<String, String>()
            map["product_id"] = model.product?.id.toString()
            map["category_id"] = model.product?.productCategoryId.toString()
            map["status"] = model.isWishlist.toString()

            viewModel.addWishList(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is AddWishlistResponse -> {
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

        adapter.productClickListener = {pos, model ->
            val bundle = Bundle()
            bundle.putString("id",model.product?.id.toString())
            val fragment = ShopDetailFragment().apply {
                arguments = bundle
            }
            replaceFragment(fragment)

        }
    }

    private fun replaceFragment(fragment: Fragment){
        with(requireActivity() as ChallangeDetailActivity){
            supportFragmentManager.beginTransaction().replace(binding.container.id, fragment).addToBackStack(null).commit()
        }
    }

    override fun onResume() {
        super.onResume()
        setData(sharedViewModel.bibleQuestDetail.value)
    }

    private fun setData(data: BibleQuestViewModel.Body?){
        data?.let {
            list.clear()
            list.addAll(data.bibleQuestAdvices ?: ArrayList())
            setAdapter()


          /*  binding.ivHeart.setOnClickListener {
                val map = HashMap<String, String>()
                map["product_id"] = data.bibleQuestAdvices?.firstOrNull()?.product?.id.toString()
                map["category_id"] = data.bibleQuestAdvices?.firstOrNull()?.product?.productCategory?.id.toString()
                if (isFav == 1) {
                    map["status"] = "0"
                } else {
                    map["status"] = "1"
                }
                viewModel.addWishList(map, requireActivity()).observe(viewLifecycleOwner,this)
            }*/
        }
    }

   /* override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is AddWishlistResponse -> {
                        val res = value.data.body
                        isFav = (res?.status ?: 0).toString().toInt()
                        if (isFav == 1) {
                            binding.ivHeart.setImageResource(R.drawable.selected_heart)
                            binding.ivHeart.imageTintList = ContextCompat.getColorStateList(requireContext(),R.color.star_red_color)
                        }else{
                            binding.ivHeart.setImageResource(R.drawable.unselected_heart)
                            binding.ivHeart.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.black)
                        }
                    }
                }
            }

            Status.LOADING -> {
                LoaderDialog.show(this)
            }

            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(requireActivity(), binding.root, value.message.toString())

            }
        }
    }*/


}