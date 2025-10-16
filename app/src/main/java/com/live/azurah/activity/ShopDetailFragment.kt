package com.live.azurah.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.ScaleAnimation
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.live.azurah.R
import com.live.azurah.adapter.ProductColorAdapter
import com.live.azurah.adapter.ProductImagesAdapter
import com.live.azurah.adapter.ProductSizeAdapter
import com.live.azurah.adapter.ShopSliderAdapter
import com.live.azurah.adapter.ViewPagerAdapter
import com.live.azurah.databinding.ActivityShopDetailBinding
import com.live.azurah.fragment.AllShopCategoryFragment
import com.live.azurah.fragment.CategoryDetailFragment
import com.live.azurah.fragment.FavouriteFragment
import com.live.azurah.fragment.FollowFollowingFragment
import com.live.azurah.fragment.SearchHomeFragment
import com.live.azurah.fragment.SearchProductFragment
import com.live.azurah.fragment.SongFragment
import com.live.azurah.fragment.SuggetionForYouFragment
import com.live.azurah.fragment.UserLikesFragment
import com.live.azurah.model.AddWishlistResponse
import com.live.azurah.model.InterestModel
import com.live.azurah.model.ProductDetailResponse
import com.live.azurah.model.ShopBannerModel
import com.live.azurah.model.ShopBannerResponse
import com.live.azurah.model.ShopCategoryResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.openUrlInBrowser
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShopDetailFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityShopDetailBinding
    private var isFav = 1
    private lateinit var productAdapter: ProductImagesAdapter

    private lateinit var sizeAdapter: ProductSizeAdapter
    private val productSizeList = ArrayList<InterestModel>()

    private lateinit var colorAdapter: ProductColorAdapter
    private val productColorList = ArrayList<InterestModel>()

//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var id = ""
    private var categoryId = ""
    private var data: ProductDetailResponse.Body? = null
    private var bannerList = ArrayList<ProductDetailResponse.Body.ProductImage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityShopDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun getProductDetail() {
        viewModel.getProductDetail(id, requireActivity()).observe(viewLifecycleOwner, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        id = arguments?.getString("id") ?: ""
        getProductDetail()

        productSizeList.add(InterestModel(isSelected = true, name = "S"))
        productSizeList.add(InterestModel(isSelected = false, name = "M"))
        productSizeList.add(InterestModel(isSelected = false, name = "L"))
        productSizeList.add(InterestModel(isSelected = false, name = "XL"))
        productSizeList.add(InterestModel(isSelected = false, name = "2XL"))

        productColorList.add(InterestModel(isSelected = true, icon = Color.parseColor("#b965a0")))
        productColorList.add(InterestModel(isSelected = false, icon = Color.parseColor("#f363d4")))
        productColorList.add(InterestModel(isSelected = false, icon = Color.parseColor("#1ce0cb")))
        productColorList.add(InterestModel(isSelected = false, icon = Color.parseColor("#3d0e87")))
        productColorList.add(InterestModel(isSelected = false, icon = Color.parseColor("#19068b")))

        setProductAdapter()
        initlistener()

        binding.ivTrainerImage.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("dsvDSsds", position.toString())
                binding.rvProductImages.scrollToPosition(position)
                bannerList.forEach {
                    it.isSelected = false
                }
                bannerList[position].isSelected = true
                productAdapter.notifyDataSetChanged()
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)


            }
        })
    }

    private fun setProductAdapter() {
        productAdapter = ProductImagesAdapter(requireContext(), bannerList)
        binding.rvProductImages.adapter = productAdapter

        productAdapter.onClick = { pos ->
            Log.d("dfdfgdfgdf", pos.toString())
            binding.ivTrainerImage.currentItem = pos

        }

        sizeAdapter = ProductSizeAdapter(requireContext(), productSizeList)
        binding.rvSize.adapter = sizeAdapter

        colorAdapter = ProductColorAdapter(requireContext(), productColorList)
        binding.rvColor.adapter = colorAdapter
    }

    private fun initlistener() {
        with(binding) {
            ivFabSelected.setOnClickListener {
                val map = HashMap<String, String>()
                map["product_id"] = id
                map["category_id"] = categoryId
                if (isFav == 1) {
                    isFav = 0
                    map["status"] = "0"
                } else {
                    isFav = 1
                    map["status"] = "1"
                }
                if (isFav == 1) {
                    binding.ivFabSelected.setImageResource(R.drawable.selected_heart)
                    binding.ivFabSelected.imageTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.star_red_color)
                } else {
                    binding.ivFabSelected.setImageResource(R.drawable.unselected_heart)
                    binding.ivFabSelected.imageTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.black)
                }
                viewModel.addWishList(map, requireActivity())
                    .observe(viewLifecycleOwner, Observer {

                    })
            }
            backIcon.setOnClickListener {
                LocalBroadcastManager.getInstance(requireContext())
                    .sendBroadcast(Intent("Shop"))
                requireActivity().supportFragmentManager.popBackStack()
            }
            btnBuyOnWeb.setOnClickListener {
                openUrlInBrowser(requireContext(), data?.websiteUrl ?: "")
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

            ivTrainerImage.adapter = ShopSliderAdapter(requireContext(), modelList)
            TabLayoutMediator(tabLayout, ivTrainerImage) { tab, position ->
                tab.setIcon(R.drawable.dot_unselected)
            }.attach()

            if (modelList.size > 1) {
                tabLayout.visible()
            } else {
                tabLayout.gone()
            }

            // Add listener to animate tab selection
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    // Scale animation to enlarge selected dot
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
                    // Scale animation to shrink unselected dot
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
                    // Optional: handle reselect if needed
                }
            })
        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is ProductDetailResponse -> {
                        binding.nestedScrollView.visible()
                        data = value.data.body
                        val res = value.data.body
                        bannerList.clear()
                        res?.productImages?.let { bannerList.addAll(it) }
                        if (bannerList.isNotEmpty()) {
                            bannerList.firstOrNull()?.isSelected = true
                        }
                        productAdapter.notifyItemRangeChanged(0, bannerList.size)
                        setSliderAdapter()
                        with(binding) {
                            tvItemFullName.text = res?.name ?: ""
                            tvPriceName.text = "£" + res?.price ?: ""
                            tvDes.text = res?.description ?: ""

                            isFav = res?.isWishlist ?: 0
                            categoryId = res?.productCategory?.id.toString()
                            if (isFav == 1) {
                                ivFabSelected.setImageResource(R.drawable.selected_heart)
                                ivFabSelected.imageTintList = ContextCompat.getColorStateList(
                                    requireContext(),
                                    R.color.star_red_color
                                )
                            } else {
                                ivFabSelected.setImageResource(R.drawable.unselected_heart)
                                ivFabSelected.imageTintList = ContextCompat.getColorStateList(
                                    requireContext(),
                                    R.color.black
                                )
                            }

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