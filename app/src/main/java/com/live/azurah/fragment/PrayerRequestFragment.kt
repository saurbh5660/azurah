package com.live.azurah.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.ReportPostActivity
import com.live.azurah.activity.ReportUserActivity
import com.live.azurah.activity.RequestActivity
import com.live.azurah.adapter.PrayerRequestAdapter
import com.live.azurah.databinding.FragmentPrayerRequestFrgamentBinding
import com.live.azurah.databinding.MenuReportBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.CommunityForumResponse
import com.live.azurah.model.InterestModel
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrayerRequestFragment : Fragment() {
    private lateinit var binding: FragmentPrayerRequestFrgamentBinding
    private val commentList = ArrayList<InterestModel>()
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var prayerRequestAdapter : PrayerRequestAdapter? = null
    private var prayerList = ArrayList<CommunityForumResponse.Body.Data>()
    private lateinit var sharedViewModel: SharedViewModel
    private var categoryId = "0"
    private var search = ""
    private var filterType =""
    private var isResume = false
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    private var showDialog = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrayerRequestFrgamentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        sharedViewModel.getCategoryId.observe(viewLifecycleOwner){
            Log.d("sdgsfsffdhfd",isResume.toString())
            if (isResume){
                if (it.first.isNotEmpty()){
                    categoryId = it.first
                    filterType = it.second
                    showDialog = true
                    resetPage = true
                    getPrayerList(it.first)
                }
            }
        }

        sharedViewModel.search.observe(viewLifecycleOwner){
            search = it
            if (isResume){
                showDialog = true
                resetPage = true
                getPrayerList()
            }
        }


        binding.rvCurisity.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1)) {
                    if (currentPage <= totalPageCount && !isApiRunning) {
                        resetPage = false
                        showDialog = false
                        getPrayerList(categoryId)
                    }
                }
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            resetPage = true
            showDialog = false
            getPrayerList(categoryId)
            binding.swipeRefreshLayout.isRefreshing = true
        }
        setAdapter()
    }

    private fun getPrayerList(id:String = "0"){
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
        viewModel.getPrayerList(map,requireActivity()).observe(viewLifecycleOwner){value->
            when (value.status) {
                Status.SUCCESS -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    binding.swipeRefreshLayout.isRefreshing = false
                    when (value.data) {
                        is CommunityForumResponse -> {
                            binding.shimmerLayout.gone()
                            binding.swipeRefreshLayout.visible()
                            binding.shimmerLayout.stopShimmer()
                            val res = value.data.body
                            if (resetPage) {
                                prayerList.clear()
                            }
                            prayerList.addAll(res?.data ?: ArrayList())
                            prayerRequestAdapter?.notifyDataSetChanged()

                            if (prayerList.isEmpty()){
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
                        LoaderDialog.show(requireActivity())
                    }
                }

                Status.ERROR -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    binding.swipeRefreshLayout.isRefreshing = false
                    showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                    binding.shimmerLayout.gone()
                    binding.swipeRefreshLayout.visible()
                    binding.shimmerLayout.stopShimmer()
                }
            }
        }
    }

    private fun setAdapter() {
        prayerRequestAdapter = PrayerRequestAdapter(requireContext(),prayerList,1)
        binding.rvCurisity.adapter = prayerRequestAdapter

        prayerRequestAdapter?.categoryListener={pos, model, view,text ->
            setCategoryWindow(view,text)
        }

        prayerRequestAdapter?.onLikeUnlike ={pos, model ->
            val map = HashMap<String, String>()
            map["prayer_id"] = model.id.toString()
            map["status"] = model.is_like.toString()
            viewModel.prayerLikeUnlike(map,requireActivity()).observe(viewLifecycleOwner){value->
                when (value.status) {
                    Status.SUCCESS -> {
                    }
                    Status.LOADING -> {
                    }
                    Status.ERROR -> {
                        showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                    }
                }
            }
        }

        prayerRequestAdapter?.praiseListener ={pos, model ->
            val map = HashMap<String, String>()
            map["prayer_id"] = model.id.toString()
            map["status"] = model.is_praise.toString()
            viewModel.prayerPriseUnpraise(map,requireActivity()).observe(viewLifecycleOwner){value->
                when (value.status) {
                    Status.SUCCESS -> {
                    }
                    Status.LOADING -> {
                    }
                    Status.ERROR -> {
                        showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                    }
                }
            }
        }

        prayerRequestAdapter?.likeListener={ _, model ->
            val bundle = Bundle()
            bundle.putString("postId",model.id.toString())
            bundle.putString("from","prayer")
            val fragment = UserLikesFragment()
            fragment.arguments = bundle
            with(requireActivity() as RequestActivity){
                replaceFragment(fragment)
            }
        }

        prayerRequestAdapter?.praiseClickListener={ _, model ->
            val bundle = Bundle()
            bundle.putString("postId",model.id.toString())
            bundle.putString("from","prayer_praise")
            val fragment = UserLikesFragment()
            fragment.arguments = bundle
            with(requireActivity() as RequestActivity){
                replaceFragment(fragment)
            }
        }

        prayerRequestAdapter?.menuListener= { _, model ,view->
            setPopUpWindow(view,model)
        }
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
                startActivity(Intent(requireContext(), ReportPostActivity::class.java).apply {
                    putExtra("from","prayer")
                    putExtra("id",model.id.toString())
                    putExtra("reportedTo",model.user?.id.toString())

                })
            }

            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(requireContext(), ReportUserActivity::class.java).apply {
                    putExtra("from","prayer")
                    putExtra("id",model.user?.id.toString())
                    putExtra("username",model.user?.username.toString())

                })
            }

            ivReportUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(requireContext(), ReportPostActivity::class.java).apply {
                    putExtra("from", "prayer")
                    putExtra("id", model.id.toString())
                    putExtra("reportedTo", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())


                })
            }

            ivUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(requireContext(), ReportUserActivity::class.java).apply {
                    putExtra("from", "prayer")
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

    private fun sureDialog(model: CommunityForumResponse.Body.Data) {
        val customDialog = Dialog(requireContext())
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
            viewModel.userBlock(map,requireActivity()).observe(viewLifecycleOwner){value->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when(value.data){
                            is CommonResponse -> {
                                val res = value.data.body
                                showCustomSnackbar(requireActivity(), binding.root, "User Blocked Successfully!")
                                val userid =model.user?.id.toString()
                                prayerList.removeAll{it.user?.id.toString() == userid}
                                prayerRequestAdapter?.notifyDataSetChanged()
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
        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    override fun onResume() {
        super.onResume()
        isResume = true
        showDialog = false
        resetPage = true
        val id = sharedViewModel.getCategoryId.value
        filterType = id?.second ?: ""
        if (id?.first.isNullOrEmpty()){
            getPrayerList("0")
        }else{
            getPrayerList(categoryId)
        }
        binding.shimmerLayout.visible()
        binding.swipeRefreshLayout.gone()
        binding.tvNoDataFound.gone()
        binding.shimmerLayout.startShimmer()
    }

    override fun onPause() {
        super.onPause()
        isResume = false
    }

    private fun setCategoryWindow(view1: View, text: String) {
        val balloon = Balloon.Builder(requireContext())
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
            .setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.black_translucent))
            .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
            .setLifecycleOwner(this)
            .build()

        balloon.bodyWindow.isOutsideTouchable = true
        balloon.showAlignBottom(view1)

    }

}