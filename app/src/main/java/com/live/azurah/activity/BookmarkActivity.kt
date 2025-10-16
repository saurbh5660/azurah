package com.live.azurah.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.live.azurah.R
import com.live.azurah.adapter.MyPersonalPostAdapter
import com.live.azurah.databinding.ActivityBookmarkBinding
import com.live.azurah.model.PostResponse
import com.live.azurah.model.SavedPostResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityBookmarkBinding
    private var postAdapter: MyPersonalPostAdapter? = null
    private var list = ArrayList<PostResponse.Body.Data>()
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = true
    private var isApiRunning = false
    private var showDialog = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkBinding.inflate(layoutInflater)
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
        setAdapter()
        initListener()
    }

    private fun setAdapter() {
        postAdapter = MyPersonalPostAdapter(this, list)
        binding.rvPosts.adapter = postAdapter

        postAdapter?.onClickListener = { pos, model ->
            Log.d("lisstttttttt",Gson().toJson(list))
            startActivity(Intent(this, MyPostsActivity::class.java).apply {
                putExtra("type","3")
                putExtra("list",list)
                putExtra("scrollPos",pos)
                putExtra("currentPage",currentPage)
                putExtra("totalPage",totalPageCount)
            })
        }
    }

    private fun initListener() {
        with(binding){

            swipeRefreshLayout.setOnRefreshListener {
                resetPage = true
                showDialog = false
                getPosts()
                swipeRefreshLayout.isRefreshing = true
            }

            rvPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(1)) {
                        if (currentPage <= totalPageCount && !isApiRunning) {
                            resetPage = false
                            showDialog = false
                            getPosts()
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

    private fun getPosts() {
        isApiRunning = true
        if (resetPage){
            currentPage = 1
        }
        val map = HashMap<String, String>()
        map["page"] = currentPage.toString()
        map["limit"] = "15"
        viewModel.getPostBookmark(map, this).observe(this, this)
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
               LoaderDialog.dismiss()
                binding.swipeRefreshLayout.isRefreshing = false
                when (value.data) {
                    is SavedPostResponse -> {
                        with(binding) {
                            val res = value.data.body
                          val newMappedList =  res?.data?.filter { it?.post != null }?.map {
                                PostResponse.Body.Data(
                                    id = it?.post?.id,
                                    comment_count = it?.post?.comment_count,
                                    created = it?.post?.created,
                                    created_at = it?.post?.created_at,
                                    description = it?.post?.description,
                                    is_bookmark = it?.post?.is_bookmark,
                                    is_like = it?.post?.is_like,
                                    like_count = it?.post?.like_count,
                                    post_images = it?.post?.post_images?.map {
                                        PostResponse.Body.Data.PostImage(
                                            id = it?.id,
                                            image = it?.image,
                                            image_thumb = it?.image_thumb,
                                            post_id = it?.post_id,
                                            type = it?.type
                                        )
                                    },
                                    user_id = it?.post?.user?.id,
                                    user =  PostResponse.Body.Data.User(
                                        first_name = it?.post?.user?.first_name,
                                        last_name = it?.post?.user?.last_name,
                                        username = it?.post?.user?.username,
                                        id = it?.post?.user?.id,
                                        image = it?.post?.user?.image,
                                        image_thumb = it?.post?.user?.image_thumb,
                                    )
                                )
                            }
                           /* newMappedList?.forEach {
                                it.post_images?.reversed()
                            }*/
                            if (resetPage) {
                                list.clear()
                            }
                            list.addAll(newMappedList ?: ArrayList())
                            postAdapter?.notifyDataSetChanged()
                            if (list.isNotEmpty()){
                                currentPage = (res?.current_page?:0) + 1
                                totalPageCount = res?.total_pages ?: 0
                                binding.tvNoDataFound.gone()
                            }else{
                                binding.tvNoDataFound.visible()
                            }
                        }
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
                binding.swipeRefreshLayout.isRefreshing = false
               LoaderDialog.dismiss()
                showCustomSnackbar(this, binding.root, value.message.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showDialog = true
        resetPage = true
        getPosts()
    }
}