package com.live.azurah.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.MyPostPrayerTestimonyActivity
import com.live.azurah.adapter.MyPostRequestAdapter
import com.live.azurah.databinding.FragmentMyPostPrayerBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.CommunityForumResponse
import com.live.azurah.model.EventDetailResponse
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
class MyPostPrayerFragment : Fragment() {
    private lateinit var binding: FragmentMyPostPrayerBinding
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var prayerRequestAdapter : MyPostRequestAdapter? = null
    private var prayerList = ArrayList<CommunityForumResponse.Body.Data>()
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    private var showDialog = true
    private var search = ""
    private lateinit var sharedViewModel: SharedViewModel
    private var isResume = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyPostPrayerBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRequestAdapter()
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        showDialog = true
        getPrayerList()

        binding.rvPrayer.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1)) {
                    if (currentPage <= totalPageCount && !isApiRunning) {
                        resetPage = false
                        showDialog = false
                        getPrayerList()
                    }
                }
            }
        })

        sharedViewModel.search.observe(viewLifecycleOwner){
            search = it
            if (isResume){
                showDialog = true
                resetPage = true
                getPrayerList()
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            resetPage = true
            showDialog = false
            getPrayerList()
            binding.swipeRefreshLayout.isRefreshing = true
        }
    }

    private fun getPrayerList(){
        isApiRunning = true
        if (resetPage){
            currentPage = 1
        }
        val map = HashMap<String,String>()
        map["page"] = currentPage.toString()
        map["limit"] = "10"
        map["category_id"] = "0"
        map["user_id"] = getPreference("id","")
        map["search_string"] = search

        viewModel.getPrayerList(map,requireActivity()).observe(viewLifecycleOwner){value->
            when (value.status) {
                Status.SUCCESS -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    binding.swipeRefreshLayout.isRefreshing = false
                    when (value.data) {
                        is CommunityForumResponse -> {
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
                }
            }
        }
    }

    private fun setRequestAdapter() {
        prayerRequestAdapter = MyPostRequestAdapter(requireContext(),prayerList,1)
        binding.rvPrayer.adapter = prayerRequestAdapter

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

        prayerRequestAdapter?.deleteListener ={pos, model ->
            sureDialog(pos,model)
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
            with(requireActivity() as MyPostPrayerTestimonyActivity){
                replaceFragment(fragment)
            }
        }

        prayerRequestAdapter?.deleteListener = {pos,model ->
            sureDialog(pos, model)
        }

    }

    private fun sureDialog(pos: Int, model: CommunityForumResponse.Body.Data) {
        val customDialog = Dialog(requireContext())
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = SureDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        confirmationBinding.tvUsernameTaken.text = "Are you sure you want to delete this post?"
        confirmationBinding.tvMsg.visibility = View.GONE
        confirmationBinding.ivDel.visibility = View.VISIBLE
        confirmationBinding.ivDel.setImageResource(R.drawable.del_icon)
//        confirmationBinding.tvMsg.text = "This action cannot be undone."
        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()

            viewModel.deletePrayer(model.id.toString(),requireActivity()).observe(viewLifecycleOwner){value->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is CommonResponse -> {
                                showCustomSnackbar(requireActivity(), binding.root,"Post Deleted Successfully.")
                                prayerList.removeAt(pos)
                                prayerRequestAdapter?.notifyDataSetChanged()
                                if (prayerList.isEmpty()){
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

        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    override fun onResume() {
        super.onResume()
        isResume = true
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