package com.live.azurah.activity

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.viewModels
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
import com.live.azurah.databinding.ActivityMyQuestPostBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.fragment.UserLikesFragment
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.CommunityForumResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
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
class MyQuestPostActivity : AppCompatActivity(),BiblePostAdapter.ClickListener  {
    private lateinit var binding: ActivityMyQuestPostBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var communityForumAdapter : BiblePostAdapter? = null
    private var communityForumList = ArrayList<CommunityForumResponse.Body.Data>()
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    private var showDialog = true
    private var search = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyQuestPostBinding.inflate(layoutInflater)
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
        setQuestAdapter()
        resetPage = true
        showDialog = true
        getCommunityForumList()

        binding.backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivCross.setOnClickListener {
            binding.etSearch.setText("")
        }
        binding.etSearch.addTextChangedListener(object: TextWatcher {
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
                    binding.ivCross.visibility = View.VISIBLE
                }else{
                    binding.ivCross.visibility = View.GONE
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

        binding.rvCurisity.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1)) {
                    if (currentPage <= totalPageCount && !isApiRunning) {
                        resetPage = false
                        showDialog = false
                        getCommunityForumList()
                    }
                }
            }
        })

        binding.swipeRefreshLayout.setOnRefreshListener {
            resetPage = true
            showDialog = false
            getCommunityForumList()
            binding.swipeRefreshLayout.isRefreshing = true
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
        map["user_id"] = getPreference("id","")
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
                            if (resetPage) {
                                communityForumList.clear()
                            }
                            communityForumList.addAll(res?.data ?: ArrayList())
                            communityForumAdapter?.notifyDataSetChanged()

                            if (communityForumList.isEmpty()){
                                binding.tvNoDataFound.visible()
                                binding.rvCurisity.gone()
                            }else{
                                binding.tvNoDataFound.gone()
                                binding.rvCurisity.visible()
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
                    showCustomSnackbar(this, binding.root, value.message.toString())
                }
            }
        }
    }

    private fun setQuestAdapter() {
        communityForumAdapter = BiblePostAdapter(this,this, type = 1, communityList = communityForumList)
        binding.rvCurisity.adapter = communityForumAdapter

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

        communityForumAdapter?.deleteListener ={pos, model ->
            sureDialog(pos,model)
        }
    }

    fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(binding.container.id, fragment).addToBackStack(null).commit()
    }

    override fun onCLick(view: View) {
    }

    private fun sureDialog(pos: Int, model: CommunityForumResponse.Body.Data) {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = SureDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        confirmationBinding.tvUsernameTaken.text = "Are you sure you want to delete this post?"
        confirmationBinding.tvMsg.text = "This action cannot be undone."
        confirmationBinding.tvMsg.visibility = View.GONE
        confirmationBinding.ivDel.visibility = View.VISIBLE
        confirmationBinding.ivDel.setImageResource(R.drawable.del_icon)
        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            viewModel.deleteCommunity(model.id.toString(),this).observe(this){value->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is CommonResponse -> {
                                showCustomSnackbar(this, binding.root,"Post Deleted Successfully.")
                                communityForumList.removeAt(pos)
                                communityForumAdapter?.notifyDataSetChanged()
                                if (communityForumList.isEmpty()){
                                    binding.tvNoDataFound.visible()
                                }else{
                                    binding.tvNoDataFound.gone()
                                }
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

}


