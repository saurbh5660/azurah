package com.live.azurah.activity

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.adapter.BiblePostAdapter
import com.live.azurah.adapter.CategoryAdapter
import com.live.azurah.databinding.ActivityQuestBinding
import com.live.azurah.databinding.FilterMenuBinding
import com.live.azurah.databinding.MenuReportBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.fragment.UserLikesFragment
import com.live.azurah.model.CategoryModel
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.CommunityCategoryResponse
import com.live.azurah.model.CommunityForumResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class QuestActivity : AppCompatActivity(),BiblePostAdapter.ClickListener {
    private lateinit var binding: ActivityQuestBinding
    private val catList = ArrayList<CategoryModel>()
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var categoryAdapter: CategoryAdapter? = null
    private var communityForumAdapter : BiblePostAdapter? = null
    private var communityForumList = ArrayList<CommunityForumResponse.Body.Data>()
    private lateinit var receiver : BroadcastReceiver
    private var categoryId ="0"
    private var currentPage = 1
    private var totalPageCount = 0
    private var filterType = ""
    private var resetPage = false
    private var isApiRunning = false
    private var showDialog = true
    private var search = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestBinding.inflate(layoutInflater)
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
       /* receiver = object : BroadcastReceiver(){
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (p1?.action == "addCommunity"){
                    getCommunityForumList(catList.firstOrNull { it.isSelected }?.id ?: "")
                }
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("addCommunity"))
     */
        setCatAdapter()
        initListener()

    }

    private fun getCategoryList() {
        val map = HashMap<String,String>()
        map["page"] = "1"
        map["limit"] = "50"
        viewModel.getCommunityCategory(map,this).observe(this){value->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is CommunityCategoryResponse -> {
                            catList.clear()
                            showDialog = false
                            resetPage = true
                            getCommunityForumList()
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

    private fun getCommunityForumList(id:String = "0"){
        isApiRunning = true
        if (resetPage){
            currentPage = 1
        }
        val map = HashMap<String,String>()
        map["page"] = currentPage.toString()
        map["limit"] = "10"
        map["category_id"] = id
        map["sort_by"] = filterType
        map["search_string"] = search

        viewModel.getCommunityForumList(map,this).observe(this){value->
            when (value.status) {
                Status.SUCCESS -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    binding.swipeRefreshLayout.isRefreshing = false
                    when (value.data) {
                        is CommunityForumResponse -> {
                            val res = value.data.body
                            binding.shimmerLayout.gone()
                            binding.swipeRefreshLayout.visible()
                            binding.shimmerLayout.stopShimmer()
                            if (resetPage) {
                                communityForumList.clear()
                            }
                            communityForumList.addAll(res?.data ?: ArrayList())
                            communityForumAdapter?.notifyDataSetChanged()

                            if (communityForumList.isEmpty()){
                                binding.tvNoDataFound.visible()
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
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.shimmerLayout.gone()
                    binding.swipeRefreshLayout.visible()
                    binding.shimmerLayout.stopShimmer()
                    showCustomSnackbar(this, binding.root, value.message.toString())
                }
            }
        }
    }

    private fun initListener() {
        with(binding) {

            rvCurisity.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (!recyclerView.canScrollVertically(1)) {
                        if (currentPage <= totalPageCount && !isApiRunning) {
                            resetPage = false
                            showDialog = false
                            getCommunityForumList(categoryId)
                        }
                    }
                }
            })

            swipeRefreshLayout.setOnRefreshListener {
                resetPage = true
                showDialog = false
                getCommunityForumList(categoryId)
                swipeRefreshLayout.isRefreshing = true
            }

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            btnAddPost.setOnClickListener {
                startActivity(Intent(this@QuestActivity,AddBiblePostActivity::class.java))
            }

            ivFilter.setOnClickListener {
                setPopUpWindow(it)
            }

            myPost.setOnClickListener {
                startActivity(Intent(this@QuestActivity,MyQuestPostActivity::class.java))
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
                                showDialog = true
                                getCommunityForumList()
                            }
                        }
                    }, delay)
                }
            })
        }
    }

    private fun setCatAdapter() {
        categoryAdapter = CategoryAdapter(this,catList)
        binding.rvCategory.adapter = categoryAdapter

        categoryAdapter?.categoryListener = {pos, model ->
            categoryId = model.id ?: "0"
            resetPage = true
            showDialog = true
            getCommunityForumList(categoryId)
        }

        communityForumAdapter = BiblePostAdapter(this,this, communityList = communityForumList)
        binding.rvCurisity.adapter = communityForumAdapter

        communityForumAdapter?.categoryListener={pos, model, view,text ->
            setCategoryWindow(view,text)
        }

        communityForumAdapter?.onLikeUnlike ={pos, model ->
            val map = HashMap<String, String>()
            map["community_forum_id"] = model.id.toString()
            map["status"] = model.is_like.toString()
            viewModel.communityLikeUnlike(map,this).observe(this){value->
                when (value.status) {
                    Status.SUCCESS -> {
                    }
                    Status.LOADING -> {
                    }
                    Status.ERROR -> {
                        showCustomSnackbar(this, binding.root, value.message.toString())
                    }
                }
            }
        }
        communityForumAdapter?.likeListener={ _, model ->
                val bundle = Bundle()
                bundle.putString("postId",model.id.toString())
                bundle.putString("from","community")
                val fragment = UserLikesFragment()
                fragment.arguments = bundle
                replaceFragment(fragment)
        }

        communityForumAdapter?.menuListener = {pos, model, view ->
            setPopUpWindow(view,model)
        }

    }

    fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(binding.container.id, fragment).addToBackStack(null).commit()
    }


    private fun setPopUpWindow(view1: View, model: CommunityForumResponse.Body.Data) {
        val view = MenuReportBinding.inflate(layoutInflater)

        val myPopupWindow = PopupWindow(
            view.root,
            500,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        with(view){
            tvHaveDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@QuestActivity, ReportPostActivity::class.java).apply {
                    putExtra("from","community")
                    putExtra("id",model.id.toString())
                    putExtra("reportedTo",model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())

                })
            }

            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@QuestActivity, ReportUserActivity::class.java).apply {
                    putExtra("from","community")
                    putExtra("id",model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())

                })
            }

            ivReportUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@QuestActivity, ReportPostActivity::class.java).apply {
                    putExtra("from", "community")
                    putExtra("id", model.id.toString())
                    putExtra("reportedTo", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())


                })
            }

            ivUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@QuestActivity, ReportUserActivity::class.java).apply {
                    putExtra("from", "community")
                    putExtra("id", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())

                })
            }

            tvBlockUser.setOnClickListener {
                myPopupWindow.dismiss()
                sureDialog(model)
            }
        }

        myPopupWindow.showAsDropDown(view1, 0, 6)
    }


    override fun onCLick(view: View) {
    }

    private fun sureDialog(model: CommunityForumResponse.Body.Data){
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = SureDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmationBinding.tvMsg.text ="Blocking this user will prevent you both from viewing each other’s posts and sending each other messages."
        confirmationBinding.tvUsernameTaken.text = buildString {
            append("Are you sure you want to block ")
            append("@")
            append(model.user?.username)
            append("?")
        }
        confirmationBinding.ivDel.visibility = View.GONE
        confirmationBinding.ivDel.setImageResource(R.drawable.block_user_icon)

        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            val map = HashMap<String,String>()
            map["block_by"] = getPreference("id","")
            map["block_to"] = model.user?.id.toString()
            map["status"] = "1"
            viewModel.userBlock(map,this).observe(this){value->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when(value.data){
                            is CommonResponse -> {
                                val res = value.data.body
                                showCustomSnackbar(this, binding.root, "User Blocked Successfully!")
                                val userid =model.user?.id.toString()
                                communityForumList.removeAll{it.user?.id.toString() == userid}
                                communityForumAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                    Status.LOADING -> {
                        LoaderDialog.show(this)
                    }
                    Status.ERROR -> {
                       LoaderDialog.dismiss()
                        showCustomSnackbar(this, binding.root, value.message.toString())
                    }
                }
            }
        }
        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
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

                getCommunityForumList(categoryId)
                myPopupWindow.dismiss()
            }

            clNewest.setOnClickListener {
                if (filterType == "newest") {
                    filterType = ""
                    ivTrendingr.setImageResource(R.drawable.unselected_radio_prayer)
                    ivNewest.setImageResource(R.drawable.unselected_radio_prayer)
                } else {
                    filterType = "newest"
                    ivTrendingr.setImageResource(R.drawable.unselected_radio_prayer)
                    ivNewest.setImageResource(R.drawable.selected_radio_prayer)
                }
                getCommunityForumList(categoryId)
                myPopupWindow.dismiss()
            }
        }
        myPopupWindow.showAsDropDown(view1, 0, -90)
    }


    override fun onResume() {
        super.onResume()
        showDialog = false
        resetPage = true
        if (catList.isEmpty()){
            getCategoryList()
        }else{
            getCommunityForumList(categoryId)
        }
        binding.shimmerLayout.visible()
        binding.swipeRefreshLayout.gone()
        binding.tvNoDataFound.gone()
        binding.shimmerLayout.startShimmer()
    }

    override fun onDestroy() {
        super.onDestroy()
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private fun setCategoryWindow(view1: View, text: String) {
        val balloon = Balloon.Builder(this)
            .setWidthRatio(0.5f)
            .setHeight(70)
            .setTextSize(15f)
            .setPaddingLeft(4)
            .setPaddingRight(4)
            .setPaddingTop(8)
            .setPaddingBottom(8)
            .setText(text)
            .setArrowSize(12)
            .setArrowOrientation(ArrowOrientation.BOTTOM)
            .setCornerRadius(12f)
            .setMarginLeft(4)
            .setMarginRight(8)
            .setBackgroundColor(ContextCompat.getColor(this,R.color.black_translucent))
            .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
            .setLifecycleOwner(this)
            .build()

        balloon.bodyWindow.isOutsideTouchable = true
        balloon.showAlignBottom(view1)

    }
}