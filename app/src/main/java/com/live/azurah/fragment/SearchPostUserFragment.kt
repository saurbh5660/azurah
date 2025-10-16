package com.live.azurah.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.activity.MyPostsActivity
import com.live.azurah.adapter.BlockAdapter
import com.live.azurah.adapter.MyPersonalPostAdapter
import com.live.azurah.adapter.SearchPostAdapter
import com.live.azurah.databinding.FragmentSearchPostUserBinding
import com.live.azurah.model.BlockResposne
import com.live.azurah.model.PostResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchPostUserFragment : Fragment() {
    private lateinit var binding: FragmentSearchPostUserBinding
    private  var tabPosition = 0
    private var postAdapter: SearchPostAdapter? = null
    private var list = ArrayList<PostResponse.Body.Data>()
    private lateinit var sharedViewModel: SharedViewModel
//    private val loaderDialog by lazy { LoaderDialog(requireContext()) }
    private val viewModel by viewModels<CommonViewModel>()

    private var blockAdapter : BlockAdapter? = null
    private var blockList = ArrayList<BlockResposne.Body.Data>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchPostUserBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabPosition = arguments?.getInt("tab_position") ?: 0
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        initListener()
        setAdapter()

        sharedViewModel.getSearchPostData.observe(viewLifecycleOwner){
          if (tabPosition == 0){
              list.clear()
              list.addAll(it)
              Log.d("sfdfsd",list.size.toString())
              postAdapter?.notifyDataSetChanged()
              if (list.isEmpty()){
                  binding.tvNoDataFound.visible()
                  binding.tvNoDataFound.text = buildString {
                      append("No posts found.")
                  }
              }else{
                  binding.tvNoDataFound.gone()
              }
          }

        }

        sharedViewModel.getSearchUserData.observe(viewLifecycleOwner){
            if (tabPosition == 1){
                blockList.clear()
                blockList.addAll(it)
                blockAdapter?.notifyDataSetChanged()
                Log.d("sfdfsdfdsfd",blockList.size.toString())


                if (blockList.isEmpty()){
                    binding.tvNoDataFound.visible()
                    binding.tvNoDataFound.text = buildString {
                        append("No users found.")
                    }
                }else{
                    binding.tvNoDataFound.gone()
                }
            }


        }
    }

    private fun setAdapter() {
        if (tabPosition == 0){
            binding.rvPostsUsers.layoutManager = GridLayoutManager(requireContext(), 2)
            postAdapter = SearchPostAdapter(requireContext(),list)
            binding.rvPostsUsers.adapter = postAdapter

            postAdapter?.onClickListener = { pos, model ->
                startActivity(Intent(requireActivity(), MyPostsActivity::class.java).apply {
                    putExtra("type","2")
                    putExtra("list",list)
                    putExtra("scrollPos",pos)
                    putExtra("currentPage",sharedViewModel.searchPair.value?.second ?: 0)
                    putExtra("totalPage",sharedViewModel.searchPair.value?.third ?: 0)
                    putExtra("search",sharedViewModel.searchPair.value?.first ?: 0)
                })
            }

        }else{
            binding.rvPostsUsers.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            blockAdapter = BlockAdapter(requireContext(),blockList,"search")
            binding.rvPostsUsers.adapter = blockAdapter

            blockAdapter?.followUnfollowListener = { _, model, _ ->
                val map = HashMap<String, String>()
                map["follow_by"] = getPreference("id", "")
                map["follow_to"] = model.blockToUser?.id.toString()
                map["status"] = model.blockToUser?.isFollowByMe.toString()
                viewModel.followUnfollow(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                    when (value.status) {
                        Status.SUCCESS -> {
                        }

                        Status.LOADING -> {
                        }

                        Status.ERROR -> {
                            showCustomSnackbar(
                                requireActivity(),
                                binding.root,
                                value.message.toString()
                            )
                        }
                    }
                }
            }

        }
    }

    private fun initListener(){
        with(binding){
            rvPostsUsers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(1)) {
                        sharedViewModel.setRecyclerScroll()
                    }
                }
            })
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(tabPosition: Int): SearchPostUserFragment {
            val fragment = SearchPostUserFragment()
            val args = Bundle()
            args.putInt("tab_position", tabPosition)
            fragment.arguments = args
            return fragment
        }
    }



}