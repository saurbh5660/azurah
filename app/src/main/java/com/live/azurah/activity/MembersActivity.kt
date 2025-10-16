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
import androidx.lifecycle.lifecycleScope
import com.live.azurah.R
import com.live.azurah.adapter.MembersAdapter
import com.live.azurah.databinding.ActivityMembersBinding
import com.live.azurah.databinding.MenuReportMemberBinding
import com.live.azurah.model.ViewGroupResponse
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
class MembersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMembersBinding
    private var groupMembers = ArrayList<ViewGroupResponse.Body.GroupMembers>()
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var id = ""
    private lateinit var followUnfollowListAdapter: MembersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMembersBinding.inflate(layoutInflater)
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
        id = intent.getStringExtra("id") ?: ""


        setAdapter(groupMembers)
        initListener()
        getGroupView()
    }

    private fun initListener() {
        with(binding) {
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            ivCross.setOnClickListener {
                etSearch.setText("")
            }


            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotBlank()) {
                        ivCross.visibility = View.VISIBLE
                    } else {
                        ivCross.visibility = View.GONE
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().isNotBlank()) {
                        val newList = groupMembers.filter { it.user?.username.toString().contains(s.toString()) } as ArrayList
                        setAdapter(newList)
                    }else{
                        setAdapter(groupMembers)
                    }

                }

            })

            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotBlank()) {
                        ivCross.visibility = View.VISIBLE
                    } else {
                        ivCross.visibility = View.GONE
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })
        }
    }

    fun setAdapter(list:ArrayList<ViewGroupResponse.Body.GroupMembers>) {
        followUnfollowListAdapter = MembersAdapter(this, list)
        binding.rvFollow.adapter = followUnfollowListAdapter

        if (list.isEmpty()){
            binding.tvNoDataFound.visible()
            binding.rvFollow.gone()
        }else{
            binding.tvNoDataFound.gone()
            binding.rvFollow.visible()

        }

        followUnfollowListAdapter.listener = { pos, view, model ->
            setPopUpWindow(view, model)
        }

        followUnfollowListAdapter.followUnfollowListener = { _, model, _ ->
            val map = HashMap<String, String>()
            map["follow_by"] = getPreference("id", "")
            map["follow_to"] = model.user?.id.toString()
            map["status"] = model.isFollowByMe.toString()
            viewModel.followUnfollow(map, this).observe(this) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                    }

                    Status.LOADING -> {
                    }

                    Status.ERROR -> {
                        showCustomSnackbar(
                            this,
                            binding.root,
                            value.message.toString()
                        )
                    }
                }
            }
        }
    }

    private fun setPopUpWindow(view1: View, model: ViewGroupResponse.Body.GroupMembers) {
        val view = MenuReportMemberBinding.inflate(layoutInflater)

        val myPopupWindow = PopupWindow(
            view.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        with(view) {
            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@MembersActivity, ReportUserActivity::class.java).apply {
                    putExtra("id", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())
                })
            }
            ivUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@MembersActivity, ReportUserActivity::class.java).apply {
                    putExtra("id", model.user?.id.toString())
                    putExtra("username", model.user?.username.toString())
                })
            }

        }

        myPopupWindow.showAsDropDown(view1, 0, -80)
    }

    private fun getGroupView() {
        viewModel.getGroupView(id, this).observe(this) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is ViewGroupResponse -> {
                            val res = value.data.body
                            groupMembers.clear()
                            groupMembers.addAll(res?.groupMembers ?: ArrayList())
                            followUnfollowListAdapter.notifyDataSetChanged()

                            binding.tvMembers.text = buildString {
                                append("Members: ")
                                append(groupMembers.size)
                                append("/")
                                append(res?.memberLimit ?: 0)
                            }
                            if (groupMembers.isEmpty()){
                                binding.tvNoDataFound.visible()
                                binding.rvFollow.gone()
                            }else{
                                binding.tvNoDataFound.gone()
                                binding.rvFollow.visible()

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

}