package com.live.azurah.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import com.live.azurah.R
import com.live.azurah.adapter.CategoryAdapter
import com.live.azurah.adapter.ViewPagerAdapter
import com.live.azurah.databinding.ActivityRequestBinding
import com.live.azurah.databinding.FilterMenuBinding
import com.live.azurah.fragment.PrayerRequestFragment
import com.live.azurah.fragment.TestimoniesFragment
import com.live.azurah.model.CategoryModel
import com.live.azurah.model.CommunityCategoryResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class RequestActivity : AppCompatActivity(),Observer<Resource<Any>> {
    private lateinit var binding: ActivityRequestBinding
    private val list = arrayOf("Prayer Request","Testimonies")
    private lateinit var fragmentList: MutableList<Fragment>
    private lateinit var viewpagerAdapter: ViewPagerAdapter
    private val catList = ArrayList<CategoryModel>()
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var categoryAdapter: CategoryAdapter? = null
    private var type = "0"
    private var filterType = ""
    private var search = ""
    private var categoryId = "0"
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.white)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
              val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.updatePadding(
                left = systemBars.left,
                bottom = systemBars.bottom,
                right = systemBars.right,
                top = systemBars.top
            )
            insets
        }
        type = intent.getStringExtra("type") ?: ""
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        binding.viewPager.isUserInputEnabled = false

        setCatAdapter()
        initData()
        initFragment()
        TabLayoutMediator(binding.tbLayout, binding.viewPager) { tab, position ->
            tab.text = list[position]
        }.attach()

        initListener()

        when(type){
            "0"-> binding.viewPager.currentItem = 0
            else-> binding.viewPager.currentItem = 1
        }

    }

    private fun getPrayerCategoryList() {
        val map = HashMap<String,String>()
        map["page"] = "1"
        map["limit"] = "50"
        viewModel.getPrayerCategoryList(map,this).observe(this,this)
    }

    private fun getTestimonyCategoryList() {
        val map = HashMap<String,String>()
        map["page"] = "1"
        map["limit"] = "50"
        viewModel.getTestimonyCategoryList(map,this).observe(this,this)
    }

    private fun initListener() {
        with(binding){
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            ivFilter.setOnClickListener {
                setPopUpWindow(it)
            }
            btnAddPost.setOnClickListener {
                startActivity(Intent(this@RequestActivity,AddPrayerRequestTestimonies::class.java))
            }

            myPost.setOnClickListener {
                startActivity(Intent(this@RequestActivity,MyPostPrayerTestimonyActivity::class.java))
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
                                sharedViewModel.setSearchChat(search)
                            }
                        }
                    }, delay)
                }

            })

            binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback(){
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {

                }

                override fun onPageSelected(position: Int) {
                    when(position){
                        0->{
                            sharedViewModel.setCategoryId(Pair("",filterType))
                            getPrayerCategoryList()
                        }
                        1->{
                            sharedViewModel.setCategoryId(Pair("",filterType))
                            getTestimonyCategoryList()
                        }
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {

                }

            })
        }
    }

    private fun initData() {
        fragmentList = ArrayList()
        fragmentList.add(PrayerRequestFragment())
        fragmentList.add(TestimoniesFragment())
    }
    private fun initFragment() {
        viewpagerAdapter = ViewPagerAdapter(fragmentList, this)
        binding.viewPager.adapter = viewpagerAdapter
    }

    private fun setCatAdapter() {
        categoryAdapter = CategoryAdapter(this,catList)
        binding.rvCategory.adapter = categoryAdapter

        categoryAdapter?.categoryListener = {pos, model ->
            categoryId = model.id.toString()
            sharedViewModel.setCategoryId(Pair(categoryId,filterType))
        }
    }

    fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, fragment).addToBackStack(null).commit()
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CommunityCategoryResponse -> {
                        catList.clear()
                        value.data.body?.data?.let {
                            val list =  it.map { CategoryModel(name = it?.name.toString(), id = it?.id.toString()) }
                            catList.add(CategoryModel(name = "All", id = "0"))
                            catList.addAll(list)
                            catList.firstOrNull()?.isSelected = true
                        }
                        categoryAdapter?.notifyDataSetChanged()
                    }
                }
            }

            Status.LOADING -> {
               LoaderDialog.dismiss()
            }

            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(this, binding.root, value.message.toString())
            }
        }
    }

    private fun setPopUpWindow(view1: View) {
        val menuBinding = FilterMenuBinding.inflate(layoutInflater)
        val myPopupWindow = PopupWindow(
            menuBinding.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )


        with(menuBinding){
            if (filterType == "trending"){
                ivTrendingr.setImageResource(R.drawable.selected_radio_prayer)
                ivNewest.setImageResource(R.drawable.unselected_radio_prayer)
            }else if (filterType == "newest"){
                ivTrendingr.setImageResource(R.drawable.unselected_radio_prayer)
                ivNewest.setImageResource(R.drawable.selected_radio_prayer)
            }
            clTrending.setOnClickListener {
                if (filterType == "trending"){
                    filterType = ""
                    ivTrendingr.setImageResource(R.drawable.unselected_radio_prayer)
                    ivNewest.setImageResource(R.drawable.unselected_radio_prayer)
                }else{
                    filterType = "trending"
                    ivTrendingr.setImageResource(R.drawable.selected_radio_prayer)
                    ivNewest.setImageResource(R.drawable.unselected_radio_prayer)
                }

                sharedViewModel.setCategoryId(Pair(categoryId,filterType))
                    myPopupWindow.dismiss()
            }

            clNewest.setOnClickListener {
                if (filterType == "newest"){
                    filterType = ""
                    ivTrendingr.setImageResource(R.drawable.unselected_radio_prayer)
                    ivNewest.setImageResource(R.drawable.unselected_radio_prayer)
                }else{
                    filterType = "newest"
                    ivTrendingr.setImageResource(R.drawable.unselected_radio_prayer)
                    ivNewest.setImageResource(R.drawable.selected_radio_prayer)
                }

                sharedViewModel.setCategoryId(Pair(categoryId,filterType))
                    myPopupWindow.dismiss()
            }
        }
        myPopupWindow.showAsDropDown(view1, 0, -90)
    }



}