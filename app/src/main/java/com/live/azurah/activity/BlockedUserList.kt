package com.live.azurah.activity

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.live.azurah.R
import com.live.azurah.adapter.BlockAdapter
import com.live.azurah.databinding.ActivityBlockedUserListBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.model.BlockResposne
import com.live.azurah.model.CommonResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlockedUserList : AppCompatActivity() {
    private lateinit var binding: ActivityBlockedUserListBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var blockAdapter : BlockAdapter? = null
    private var blockList = ArrayList<BlockResposne.Body.Data>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedUserListBinding.inflate(layoutInflater)
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

    private fun getBlockList(){
        viewModel.getBlockList(this).observe(this){value->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when(value.data){
                        is BlockResposne -> {
                            val res = value.data.body
                            blockList.clear()
                            blockList.addAll(res?.data ?: ArrayList())
                            blockAdapter?.notifyDataSetChanged()

                            if (blockList.isEmpty()){
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

    private fun initListener() {
        with(binding){
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    fun setAdapter(){
        blockAdapter = BlockAdapter(this,blockList)
        binding.rvBlock.adapter = blockAdapter

        blockAdapter?.onBlockListener ={pos, model ->
            sureDialog(pos,model)
        }
    }

    private fun sureDialog(pos:Int,model: BlockResposne.Body.Data) {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = SureDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmationBinding.tvUsernameTaken.text = buildString {
            append("Are you sure you want to unblock @")
            append(model.blockToUser?.username)
        }
        confirmationBinding.tvMsg.text = buildString {
        append("They won't be notified that you've unblocked them.")
    }
        confirmationBinding.tvNo.text = "Cancel"

        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            val map = HashMap<String,String>()
            map["block_by"] = getPreference("id","")
            map["block_to"] = model.blockToUser?.id.toString()
            map["status"] = "0"
            viewModel.userBlock(map,this).observe(this){value->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when(value.data){
                            is CommonResponse -> {
                                val res = value.data.body
                                showCustomSnackbar(this,binding.root,model.blockToUser?.firstName+" "+model.blockToUser?.lastName+" has been unblocked.")
                                blockList.removeAt(pos)
                                blockAdapter?.notifyDataSetChanged()

                                if (blockList.isEmpty()){
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

    override fun onResume() {
        super.onResume()
        getBlockList()
    }
}