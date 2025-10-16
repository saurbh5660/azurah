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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.adapter.CategoryAdapter
import com.live.azurah.adapter.ExploreGroupAdapter
import com.live.azurah.databinding.ActivityGroupBinding
import com.live.azurah.model.CategoryModel
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.CommunityCategoryResponse
import com.live.azurah.model.ExploreGroupResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityGroupBinding
    private val catList = ArrayList<CategoryModel>()
    private val exploreGroupList = ArrayList<ExploreGroupResponse.Body.Data>()
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var categoryAdapter: CategoryAdapter? = null
    private var exploreGroupAdapter: ExploreGroupAdapter? = null
    private var selectedCategory = 0
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    private var showDialog = true
    private var categoryId = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupBinding.inflate(layoutInflater)
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

        initListener()
        setAdapter()
        setCatAdapter()
        getGroupCategoryList()
        resetPage = true
        showDialog = true
        getGroupList("0")
    }

    private fun getGroupCategoryList() {
        val map = HashMap<String,String>()
        map["page"] = "1"
        map["limit"] = "50"
        viewModel.getTestimonyCategoryList(map,this).observe(this,this)
    }

    private fun initListener() {
        with(binding){

            rvExplore.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (!recyclerView.canScrollVertically(1)) {
                        if (currentPage <= totalPageCount && !isApiRunning) {
                            resetPage = false
                            showDialog = false
                            getGroupList(categoryId)
                        }
                    }
                }
            })

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            ivCross.setOnClickListener {
                etSearch.setText("")
            }

            btnGroupPost.setOnClickListener {
                startActivity(Intent(this@GroupActivity, RequestGroupChat::class.java).apply {
                    putExtra("id",categoryId)
                })
            }

//            tvViewAll.setOnClickListener {
//                startActivity(Intent(this@GroupActivity,JoinedGroupActivity::class.java))
//            }
//
//
//            btnViewGroup.setOnClickListener {
//                startActivity(Intent(this@GroupActivity,GuidelineParticipationActivity::class.java))
//            }
            etSearch.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotBlank()){
                        ivCross.visibility = View.VISIBLE
                    }else{
                        ivCross.visibility = View.GONE
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                }

            })
        }
    }

    private fun setAdapter() {
        exploreGroupAdapter = ExploreGroupAdapter(this,exploreGroupList)
        binding.rvExplore.adapter = exploreGroupAdapter
    }

    private fun setCatAdapter() {
        categoryAdapter = CategoryAdapter(this,catList)
        binding.rvCategory.adapter = categoryAdapter

        categoryAdapter?.categoryListener = {pos, model ->
            categoryId = model.id.toString()
            resetPage = true
            showDialog = true
            getGroupList(categoryId)
        }
    }
    private fun getGroupList(id:String = "0"){
        isApiRunning = true
        if (resetPage){
            currentPage = 1
        }
        val map = HashMap<String,String>()
        map["page"] = currentPage.toString()
        map["limit"] = "10"
        map["category_id"] = id
        viewModel.getGroupList(map,this).observe(this){value->
            when (value.status) {
                Status.SUCCESS -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is ExploreGroupResponse -> {
                            val res = value.data.body
                            if (resetPage) {
                                exploreGroupList.clear()
                            }
                            exploreGroupList.addAll(res?.data ?: ArrayList())
                            exploreGroupAdapter?.notifyDataSetChanged()

                            if (exploreGroupList.isEmpty()){
                                binding.tvNoDataFound.visible()
                                binding.tvNoDataFound.text = buildString {
                                    append("No group chats found.")
                                }
                            }else{
                                binding.tvNoDataFound.gone()
                                currentPage = (res?.current_page?:0) + 1
                            }
                            totalPageCount = res?.total_pages ?: 0

                        }
                    }
                }

                Status.LOADING -> {
                    if (showDialog){
                        LoaderDialog.show(this)
                    }
                }

                Status.ERROR -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    showCustomSnackbar(this, binding.root, value.message.toString())
                }
            }
        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CommonResponse -> {
                        showCustomSnackbar(this, binding.root, "Post added Successfully.")
//                        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("addPrayRequest"))
//                        finish()
                        /*  startActivity(Intent(this, HomeActivity::class.java).apply {
                              addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                          })*/
                        finish()
                    }
                    is CommunityCategoryResponse -> {
                        catList.clear()
                        value.data.body?.data?.let {
                            val list =  it.map { CategoryModel(name = it?.name.toString(), id = it?.id.toString()) }
                            catList.addAll(list)
                            catList.add(0, CategoryModel(name = "All", id = "0"))
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