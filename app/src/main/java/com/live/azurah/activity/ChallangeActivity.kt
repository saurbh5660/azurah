package com.live.azurah.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
import com.google.android.material.tabs.TabLayoutMediator
import com.live.azurah.R
import com.live.azurah.adapter.CategoryAdapter
import com.live.azurah.adapter.ViewPagerAdapter
import com.live.azurah.databinding.ActivityChallangeBinding
import com.live.azurah.fragment.FreeQuestFragment
import com.live.azurah.fragment.PaidQuestFragment
import com.live.azurah.model.CategoryModel
import com.live.azurah.model.CommunityCategoryResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class ChallangeActivity : AppCompatActivity(), Observer<Resource<Any>> {
     lateinit var binding: ActivityChallangeBinding
    private val list = arrayOf("Free Access","Premium Access")
    private lateinit var fragmentList: MutableList<Fragment>
    private lateinit var viewpagerAdapter: ViewPagerAdapter
    private val catList = ArrayList<CategoryModel>()
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var categoryAdapter: CategoryAdapter? = null
    private lateinit var sharedViewModel: SharedViewModel
    private var type = "0"
    private var search = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallangeBinding.inflate(layoutInflater)
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
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        binding.viewPager.isUserInputEnabled = false
        setCatAdapter()
        initListener()
        initData()
        initFragment()

        TabLayoutMediator(binding.tbLayout, binding.viewPager) { tab, position ->
            tab.text = list[position]
        }.attach()

        getBibleCategoryList()
    }

    private fun getBibleCategoryList() {
        val map = HashMap<String,String>()
        map["page"] = "1"
        map["limit"] = "50"
        viewModel.bibleQuestCategoryList(map,this).observe(this,this)
    }

    private fun initListener() {
        with(binding){
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            ivCross.setOnClickListener {
                etSearch.setText("")
            }

            progressBar.setOnClickListener {
                startActivity(Intent(this@ChallangeActivity,CompletedQuestActivity::class.java))
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
        }
    }

/*
    private fun addCatList() {
        catList.add(CategoryModel(R.color.white,R.color.blue,"All",true))
        catList.add(CategoryModel(R.color.red_color,R.color.light_red,"Intercession"))
        catList.add(CategoryModel(R.color.green,R.color.cream,"Faith"))
        catList.add(CategoryModel(R.color.red_color,R.color.light_red,"Affirmative prayerenting"))
        catList.add(CategoryModel(R.color.green,R.color.cream,"Faith"))
    }
*/

    private fun setCatAdapter() {
        categoryAdapter = CategoryAdapter(this,catList)
        binding.rvCategory.adapter = categoryAdapter

        categoryAdapter?.categoryListener = {pos, model ->
            sharedViewModel.setCategoryId(Pair(model.id.toString(),""))
        }
    }

    private fun initData() {
        fragmentList = ArrayList()
        fragmentList.add(FreeQuestFragment())
        fragmentList.add(PaidQuestFragment())
    }
    private fun initFragment() {
        viewpagerAdapter = ViewPagerAdapter(fragmentList, this)
        binding.viewPager.adapter = viewpagerAdapter
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

}