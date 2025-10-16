package com.live.azurah.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.live.azurah.R
import com.live.azurah.activity.LoginActivity
import com.live.azurah.activity.SettingActivity
import com.live.azurah.adapter.FollowUnfollowAdapter
import com.live.azurah.adapter.FollowUnfollowListAdapter
import com.live.azurah.databinding.DialogResetPasswordBinding
import com.live.azurah.databinding.DialogShareBinding
import com.live.azurah.databinding.FragmentFollowFollowingBinding
import com.live.azurah.model.InterestModel


class FollowFollowingActivity : AppCompatActivity(),FollowUnfollowAdapter.ClickListener {
    private lateinit var binding: FragmentFollowFollowingBinding
    private var list  = ArrayList<InterestModel>()
    private var followList  = ArrayList<InterestModel>()
    private var unFollowList  = ArrayList<InterestModel>()
    private var type = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentFollowFollowingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.white)

         type = intent.getStringExtra("type") ?: "0"

        if (type == "0"){
            list.add(InterestModel(true,"1.3K Followers"))
            list.add(InterestModel(false,"435 Following"))


        }else{
            list.add(InterestModel(false,"1.3K Followers"))
            list.add(InterestModel(true,"435 Following"))

        }

        followList.add(InterestModel(false))
        followList.add(InterestModel(false))
        followList.add(InterestModel(false))
        followList.add(InterestModel(false))
        followList.add(InterestModel(false))
        followList.add(InterestModel(false))
        followList.add(InterestModel(false))
        followList.add(InterestModel(false))
        followList.add(InterestModel(false))

        unFollowList.add(InterestModel(true))
        unFollowList.add(InterestModel(true))
        unFollowList.add(InterestModel(true))
        unFollowList.add(InterestModel(true))
        unFollowList.add(InterestModel(true))
        unFollowList.add(InterestModel(true))
        unFollowList.add(InterestModel(true))
        unFollowList.add(InterestModel(true))
        unFollowList.add(InterestModel(true))

        setAdapter()

        initListener()

    }

    private fun initListener() {
        with(binding){
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
          /*  ivSetting.setOnClickListener {
                startActivity(Intent(this@FollowFollowingActivity,SettingActivity::class.java))
            }
            ivShare.setOnClickListener {
                showShareDialog()
            }*/

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

    fun setAdapter(){
        val adapter = FollowUnfollowAdapter(this,list,this)
        binding.rvFollowType.adapter = adapter

//        binding.rvFollow.scrollToPosition(type.toInt())

       /* if (type == "0"){
            val adapter1 = FollowUnfollowListAdapter(this,followList)
            binding.rvFollow.adapter = adapter1
        }else{
            val adapter1 = FollowUnfollowListAdapter(this,unFollowList)
            binding.rvFollow.adapter = adapter1
        }*/

    }

    private fun showShareDialog(){
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val resetBinding = DialogShareBinding.inflate(layoutInflater)
        customDialog.setContentView(resetBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        resetBinding.btnCopy.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    override fun onClick(position: Int) {
//        binding.rvFollow.scrollToPosition(position)
       /* if (position == 0){
            val adapter1 = FollowUnfollowListAdapter(this,followList)
            binding.rvFollow.adapter = adapter1
        }else{
            val adapter1 = FollowUnfollowListAdapter(this,unFollowList)
            binding.rvFollow.adapter = adapter1
        }*/
    }


}