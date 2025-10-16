package com.live.azurah.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.gson.Gson
import com.live.azurah.R
import com.live.azurah.controller.MyApplication
import com.live.azurah.databinding.ActivityGuildlineParticipationBinding
import com.live.azurah.model.ViewGroupResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.socket.SocketManager
import com.live.azurah.util.convertDate
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.setClickableText
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@AndroidEntryPoint
class GuidelineParticipationActivity : AppCompatActivity(),SocketManager.Observer {
    private lateinit var binding: ActivityGuildlineParticipationBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var id = ""
    private var isJoined = 0
    private var groupDetail = ViewGroupResponse.Body()
    private lateinit var socketManager: SocketManager

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuildlineParticipationBinding.inflate(layoutInflater)
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
        initListener()
        getGroupView()
        socketManager = MyApplication.instance!!.getSocketManager()!!
        if (!socketManager.isConnected() || socketManager.getmSocket() == null) {
            socketManager.init()
        }
    }

    private fun initListener() {
        with(binding) {
            setClickableText(tvGuideCommunity,"Follow community guidelines.","community guidelines",this@GuidelineParticipationActivity)
            btnJoin.setOnClickListener {
                if (isJoined != 1){
                    joinGroup()
                }
            }
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            cbTerms.setOnClickListener {
                if (cbTerms.isChecked) {
                    btnJoin.backgroundTintList = getColorStateList(R.color.blue)
                    btnJoin.isEnabled = true
                } else {
                    btnJoin.backgroundTintList = getColorStateList(R.color.button_grey)
                    btnJoin.isEnabled = false
                }
            }
        }
    }

    private fun getGroupView() {
        viewModel.getGroupView(id, this).observe(this) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is ViewGroupResponse -> {
                            val res = value.data.body
                            groupDetail = res ?: ViewGroupResponse.Body()
                            with(binding) {
                                nestedScrollView.visible()
                                tvLoc.text = buildString {
                                    append("Created On: ")
                                    append(convertDate(res?.createdAt ?: "", "EEE dd MMM yyyy"))
                                }
                                tvDate.text = buildString {
                                    append("Total Members: ")
                                    append(res?.groupMemberCount)
                                }
                                ivGroupImage.loadImage(
                                    ApiConstants.IMAGE_BASE_URL + res?.image
                                )
                                tvGroupName.text = res?.name ?: ""
                                tvDes.text = res?.description ?: ""
                                isJoined = res?.isJoined ?: 0
                                if (res?.isJoined == 1){
                                    clTerms.gone()
                                    btnJoin.text = "Joined"
                                }
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

    private fun joinGroup() {
        val jsonObject = JSONObject()
        jsonObject.put("user_id", getPreference("id", ""))
        jsonObject.put("group_id", id)
        socketManager.joinGroupChat(jsonObject)
        LoaderDialog.show(this)
    }

    override fun onError(event: String, vararg args: Any) {
    }

    @OptIn(UnstableApi::class)
    override fun onResponse(event: String, vararg args: Any) {
        when (event) {
            SocketManager.JOIN_GROUP_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main) {
                   LoaderDialog.dismiss()
                    val jsonArray= args[0] as JSONObject
                    Log.d("yaadsfd",jsonArray.toString())
                    try {
                        isJoined = 1
                        binding.clTerms.gone()
                        binding.btnJoin.text = "Joined"
                        binding.btnJoin.backgroundTintList = getColorStateList(R.color.button_grey)
                        binding.btnJoin.isEnabled = false
                        startActivity(Intent(this@GuidelineParticipationActivity, ChatActivity::class.java).apply {
                            putExtra("groupId",groupDetail.id.toString())
                            putExtra("name",groupDetail.name?: "")
                            putExtra("image",groupDetail.image?: "")
                        })

                    }catch (e: Exception){
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        socketManager.unRegister(this)
        socketManager.onRegister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.unRegister(this)

    }


}