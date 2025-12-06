package com.live.azurah.activity

import android.app.Dialog
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.live.azurah.R
import com.live.azurah.adapter.ChatAdapter
import com.live.azurah.controller.MyApplication
import com.live.azurah.databinding.ActivityChatBinding
import com.live.azurah.databinding.ChatMenuBinding
import com.live.azurah.databinding.LogoutDialogBinding
import com.live.azurah.databinding.MenuReportDeleteDialogBinding
import com.live.azurah.model.ChatResponse
import com.live.azurah.model.FileUploadResponse
import com.live.azurah.model.FullImageModel
import com.live.azurah.model.MuteResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.socket.SocketManager
import com.live.azurah.util.ImagePickerActivity
import com.live.azurah.util.ShowImagesDialogFragment
import com.live.azurah.util.chatDate
import com.live.azurah.util.clearPreferences
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.makeCommunityGuidelinesClickable
import com.live.azurah.util.prepareVideoPart
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.showCustomToast
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.regex.Pattern
import kotlin.collections.set

@AndroidEntryPoint
class ChatActivity : ImagePickerActivity(), SocketManager.Observer {
    private lateinit var binding: ActivityChatBinding
    private lateinit var socketManager: SocketManager
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private var hideJob: Job? = null
    private val viewModel by viewModels<CommonViewModel>()
    private var user2id = ""
    private val list = ArrayList<ChatResponse.Body.Data>()
    private lateinit var adapter: ChatAdapter
    private var groupId = ""
    private var name = ""
    private var username = ""
    private var image = ""
    private var isLoadingOldMessages = false
    private var offset = 1
    private val pageSize = 20
    private var totalPages = 0
    private var muteStatus = ""
    private var constantId = ""
    private var isNotification = ""
    private var isFirstLoad = true
    private var isFollowByMe = 0
    private var isFollowByOther = 0
    private var messageRequest = 0
    private var isSenderBlockByAdmin = ""
    private var isReceiverBlockByAdmin = ""
    private var receiverProfileType = 0
    private var senderProfileType = 0
    private var requestedSenderId = 0


    private val typingHandler = android.os.Handler()
    private val typingTimeout = 3000L // 3 seconds

    private val stopTypingRunnable = Runnable {
        typing("0")
    }
//    private lateinit var bottomSheetBehavior : BottomSheetBehavior<View>
    private var isVideo = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
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
//        bottomSheetBehavior = BottomSheetBehavior.from(binding.inPost.clPostSheet)
//        initBottomSheetBehavior()
        socketManager = MyApplication.instance!!.getSocketManager()!!
        if (!socketManager.isConnected() || socketManager.getmSocket() == null) {
            socketManager.init()
        }
        groupId = intent.getStringExtra("groupId") ?: ""
        user2id = intent.getStringExtra("uid2") ?: ""
        name = intent.getStringExtra("name") ?: ""
        username = intent.getStringExtra("username") ?: ""
        image = intent.getStringExtra("image") ?: ""
        constantId = intent.getStringExtra("constant_id") ?: ""
        isNotification = intent.getStringExtra("isNotification") ?: ""
        binding.tvStartUsername.text = buildString {
            append("Start a conversation with @")
            append(username)
        }
        binding.tvAcceptUsername.text = buildString {
            append("Do you want to accept the message request from @")
            append(username)
            append("?")
        }
        val message = "You can send up to three messages to @${username} until they accept your request. Please follow our community guidelines to keep conversations respectful."
        makeCommunityGuidelinesClickable(this,binding.tvStartDescription, message) {
            startActivity(Intent(this,ContentActivity::class.java).apply {
                putExtra("type", 2)
            })
        }
        if (groupId.isEmpty()) {
            getSingleMessage()

        } else {
            binding.tvMessage.visibility = View.GONE
            getGroupMessage()
        }
        binding.cvTime.gone()
        binding.cvTime.translationY = -binding.cvTime.height.toFloat()
        binding.cvTime.alpha = 0f
        initListener()
        setChatAdapter()
        getMuteStatus()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isNotification == "1") {
                        startActivity(Intent(this@ChatActivity,HomeActivity::class.java))
                        finishAffinity()
                    } else {
                        finish()
                    }
                }
            }
        )
    }

    private fun getMuteStatus() {
        val map = HashMap<String, String>()
        map["sender_id"] = getPreference("id", "")
        if (groupId.isEmpty()) {
            map["receiver_id"] = user2id
            map["group_id"] = "0"
        } else {
            map["receiver_id"] = "0"
            map["group_id"] = groupId
        }

        viewModel.getMuteStatus(map, this).observe(this, Observer { value ->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is MuteResponse -> {
                            with(binding) {
                                val res = value.data.body
                                muteStatus = res?.isMutedByMe ?: ""
                                isFollowByMe = res?.isFollowByMe ?: 0
                                isFollowByOther = res?.isFollowByOther ?: 0
                                messageRequest = res?.messageRequest ?: 0
                                requestedSenderId = res?.messageRequestSenderId ?: 0
                                isReceiverBlockByAdmin = res?.isReceiverBlockByAdmin ?: ""
                                isSenderBlockByAdmin = res?.isSenderBlockByAdmin ?: ""
                                senderProfileType = res?.senderProfileType ?: 0
                                receiverProfileType = res?.receiverProfileType ?: 0

                                if (isSenderBlockByAdmin == "1"){
                                    showCustomToast(this@ChatActivity,"Your account has been blocked by the admin.")
                                    clearPreferences()
                                    startActivity(Intent(this@ChatActivity, LoginActivity::class.java))
                                    finishAffinity()
                                }
                                if (res?.isBlockedByMe == "1" || res?.isBlockedByOther == "1") {
                                    llSendMessage.gone()
                                    ivSendMessage.gone()
                                } else {
                                    llSendMessage.visible()
                                    ivSendMessage.visible()
                                }
                                setMessageLayout()

                            }
                        }
                    }
                }

                Status.LOADING -> {
                   LoaderDialog.dismiss()
                }

                Status.ERROR -> {
                   LoaderDialog.dismiss()
                }
            }
        })
    }

    private fun setMessageLayout() {
        if (receiverProfileType == 1){
            if (isFollowByOther != 1 && isFollowByOther != 0) {
                if (messageRequest == 1) {
                    binding.llRequestLayout.gone()
                } else {
                    if (messageRequest == 0) {
                      /*  binding.llRequestLayout.visible()
                        binding.llSendRequest.gone()
                        binding.llRequested.visible()*/
                        if (requestedSenderId.toString() == getPreference("id","")){
                            binding.llRequestLayout.visible()
                            binding.llSendRequest.visible()
                            binding.llRequested.gone()
                        }else{
                            binding.llRequestLayout.visible()
                            binding.llSendRequest.gone()
                            binding.llRequested.visible()
                        }
                    } else {
                        binding.llRequestLayout.visible()
                        binding.llSendRequest.visible()
                        binding.llRequested.gone()
                    }

                }
            }else{
                Log.d("dcdc", messageRequest.toString())
                if (messageRequest == 0) {
                    binding.llRequestLayout.visible()
                    binding.llSendRequest.gone()
                    binding.llRequested.visible()
                } else {
                    binding.llRequestLayout.gone()
                }
            }
//            else if (isFollowByMe != 1 && isFollowByMe != 0) {

           /* } else {
                Log.d("dcdssssac", messageRequest.toString())
                binding.llRequestLayout.gone()
            } */
        }else{
            if (messageRequest == 0) {
                binding.llRequestLayout.visible()
                binding.llSendRequest.gone()
                binding.llRequested.visible()
            } else {
                binding.llRequestLayout.gone()
            }
        }

    }

    private fun setChatAdapter() {
        adapter = ChatAdapter(this, list)
        binding.rvChat.adapter = adapter

        adapter.menuListener = { msgId, pos, view, userId, userName, type ->
            Log.d("ffsfsdfsdf", userName)

            if (receiverProfileType==1){
                if (isFollowByOther != 1) {
                    if (messageRequest == 1) {
                        setPopUpWindowReportDelete(view, msgId, type, userId, userName, pos)

                    }
                }else{
                    setPopUpWindowReportDelete(view, msgId, type, userId, userName, pos)
                }
            }else{
                setPopUpWindowReportDelete(view, msgId, type, userId, userName, pos)
            }

        }

        adapter.onPostImages = {pos,model->
            val imageList = ArrayList<FullImageModel>()
            imageList.add(FullImageModel(image = model?.message, type = 1) )

            val fullImageDialog = ShowImagesDialogFragment.newInstance(imageList, pos)
            fullImageDialog.show(supportFragmentManager, "FullImageDialog")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initListener() {
        with(binding) {
            ivProfile.loadImage(ApiConstants.IMAGE_BASE_URL + image, R.drawable.profile_icon)
            name.text = this@ChatActivity.username

            rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()

                    // Cancel any pending hide operations when scrolling
                    hideJob?.cancel()

                    // Only show date if we have a valid position with createdAt data
                    val shouldShowDate = firstVisiblePosition != 0 &&
                            firstVisiblePosition < list.size &&
                            !list[firstVisiblePosition].createdAt.isNullOrEmpty()

                    if (shouldShowDate) {
                        showDateWithAnimation(chatDate(list[firstVisiblePosition].createdAt ?: ""))
                    } else {
                        hideDateWithAnimation()
                    }

                    if (isFirstLoad) {
                        isFirstLoad = false
                        return
                    }

                    if (layoutManager.findFirstVisibleItemPosition() == 0 &&
                        !isLoadingOldMessages &&
                        offset <= totalPages) {
                        if (groupId.isEmpty()) {
                            loadSingleOlderMessages()
                        } else {
                            loadGroupOlderMessages()
                        }
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            // Only start hide timer if we're actually showing a date
                            if (binding.cvTime.visibility == View.VISIBLE) {
                                startHideTimer()
                            }
                        }
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            hideJob?.cancel()
                        }
                        RecyclerView.SCROLL_STATE_SETTLING -> {
                            hideJob?.cancel()
                        }
                    }
                }
            })
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            ivMore.setOnClickListener {
                setPopUpWindow(it)
            }

           /* inPost.clCamera.setOnClickListener {
                if (isVideo){
                    askStorageManagerPermission(this@ChatActivity,10,true,2)
                }else{
                    askStorageManagerPermission(this@ChatActivity,98,false,2)
                }
            }

            inPost.clGallery.setOnClickListener {
                if (isVideo){
                    askStorageManagerPermission(this@ChatActivity,11,true,2)
                }else{
                    askStorageManagerPermission(this@ChatActivity,99,false,2)
                }
            }*/

            ivPickImage.setOnClickListener {
                askStorageManagerPermission(this@ChatActivity,1,false,4)
//                imageVideoPickerDialog()
            }

            tvYes.setOnClickListener {
                val jsonObject = JSONObject()
                jsonObject.put("sender_id", getPreference("id", ""))
                jsonObject.put("message_request", "1")
                if (groupId.isNotEmpty()) {
                    jsonObject.put("group_id", groupId)
                    jsonObject.put("receiver_id", "0")
                } else {
                    jsonObject.put("group_id", "0")
                    jsonObject.put("receiver_id", user2id)
                }
                Log.d("dsdgdsgd", jsonObject.toString())
                socketManager.messageRequest(jsonObject)
                LoaderDialog.show(this@ChatActivity)
            }

            tvNo.setOnClickListener {
                val jsonObject = JSONObject()
                jsonObject.put("sender_id", getPreference("id", ""))
                jsonObject.put("message_request", "2")
                if (groupId.isNotEmpty()) {
                    jsonObject.put("group_id", groupId)
                    jsonObject.put("receiver_id", "0")
                } else {
                    jsonObject.put("group_id", "0")
                    jsonObject.put("receiver_id", user2id)
                }
                Log.d("dsdgdsgd", jsonObject.toString())
                socketManager.messageRequest(jsonObject)
                LoaderDialog.show(this@ChatActivity)
            }

            ivProfile.setOnClickListener {
                if (groupId.isEmpty()) {
                    startActivity(
                        Intent(
                            this@ChatActivity,
                            OtherUserProfileActivity::class.java
                        ).apply {
                            putExtra("user_id", user2id)
                        })

                } else {
                    startActivity(Intent(this@ChatActivity, MembersActivity::class.java).apply {
                        putExtra("id", groupId)
                    })
                }
            }

            ivSendMessage.setOnClickListener {
                if (isReceiverBlockByAdmin == "1"){
                    showCustomToast(this@ChatActivity,"Messaging with this user is currently restricted.")
                    return@setOnClickListener
                }
                if (receiverProfileType == 1){
                    if (isFollowByOther != 1 && isFollowByOther != 0) {
                        if (messageRequest == 1) {
                            sendMessage(binding.etMessage.text.toString(),"0")
                            binding.etMessage.setText("")
                        } else {
                            if (list.size < 3) {
                                if (messageRequest != 0){
                                    sendMessage(binding.etMessage.text.toString(),"0")
                                    binding.etMessage.setText("")
                                    val jsonObject = JSONObject()
                                    jsonObject.put("sender_id", getPreference("id", ""))
                                    jsonObject.put("message_request", "0")
                                    if (groupId.isNotEmpty()) {
                                        jsonObject.put("group_id", groupId)
                                        jsonObject.put("receiver_id", "0")
                                    } else {
                                        jsonObject.put("group_id", "0")
                                        jsonObject.put("receiver_id", user2id)
                                    }
                                    Log.d("dsdgdsgd", jsonObject.toString())
                                    socketManager.messageRequest(jsonObject)
                                }else{
                                    sendMessage(binding.etMessage.text.toString(),"0")
                                    binding.etMessage.setText("")
                                }
                            } else {
                                showCustomSnackbar(
                                    this@ChatActivity,
                                    it,
                                    "You can't send message until they accept your request."
                                )
                            }
                        }
                    }else{
                        sendMessage(binding.etMessage.text.toString(),"0")
                        binding.etMessage.setText("")
                    }

                } else {
                    sendMessage(binding.etMessage.text.toString(),"0")
                    binding.etMessage.setText("")
                }
            }

            name.setOnClickListener {
                if (groupId.isEmpty()) {
                    startActivity(
                        Intent(
                            this@ChatActivity,
                            OtherUserProfileActivity::class.java
                        ).apply {
                            putExtra("user_id", user2id)
                        })
                } else {
                    startActivity(Intent(this@ChatActivity, MembersActivity::class.java).apply {
                        putExtra("id", groupId)
                    })
                }
            }

            etMessage.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().isNotEmpty()) {

                        val text = s.toString()
                        val urlPattern = "(https?://[^\\s]+)"
                        val pattern = Pattern.compile(urlPattern)
                        val matcher = pattern.matcher(text)

                        if (matcher.find()) {
                            val url = matcher.group(1) ?: ""
                            if (url.startsWith("https://app.azrius.co.uk/common_api/deepLinking/referral?referral_code=")){
                                binding.etMessage.removeTextChangedListener(this)
                                binding.etMessage.setText(url)
                                binding.etMessage.setSelection(url.length)
                                binding.etMessage.addTextChangedListener(this)
                            }
                        }

                        binding.ivSendMessage.backgroundTintList = getColorStateList(R.color.blue)
                        binding.ivSendMessage.isEnabled = true
                        typing("1")

                        typingHandler.removeCallbacks(stopTypingRunnable)
                        typingHandler.postDelayed(stopTypingRunnable, typingTimeout)
                    } else {
                        binding.ivSendMessage.backgroundTintList =
                            getColorStateList(R.color.button_grey)
                        binding.ivSendMessage.isEnabled = false

                        typingHandler.removeCallbacks(stopTypingRunnable)
                        typing("0")
                    }
                }
            })
        }
    }

    private fun showDateWithAnimation(date: String) {
        // Don't do anything if we're already showing the same date
        if (binding.cvTime.visibility == View.VISIBLE && binding.tvDate.text == date) {
            return
        }

        binding.tvDate.text = date

        if (binding.cvTime.visibility != View.VISIBLE) {
            // Reset any previous animation and ensure view is properly hidden initially
            binding.cvTime.clearAnimation()
            binding.cvTime.animate().cancel()

            // Set initial position above the screen
            binding.cvTime.translationY = -binding.cvTime.height.toFloat()
            binding.cvTime.alpha = 0f
            binding.cvTime.visible()

            // Slide in from top with fade effect
            binding.cvTime.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun hideDateWithAnimation() {
        if (binding.cvTime.visibility == View.VISIBLE) {
            // Cancel any ongoing animation
            binding.cvTime.clearAnimation()
            binding.cvTime.animate().cancel()

            binding.cvTime.animate()
                .translationY(-binding.cvTime.height.toFloat())
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    binding.cvTime.gone()
                    binding.cvTime.translationY = 0f
                    binding.cvTime.alpha = 1f // Reset alpha for next time
                }
                .start()
        }
    }

    private fun startHideTimer() {
        hideJob?.cancel()
        hideJob = lifecycleScope.launch {
            delay(2000) // Increased to 2 seconds for better UX
            hideDateWithAnimation()
        }
    }


    private fun getSingleMessage() {
        isLoadingOldMessages = true
        val jsonObject = JSONObject()
        jsonObject.put("sender_id", getPreference("id", ""))
        jsonObject.put("receiver_id", user2id)
        jsonObject.put("page", offset)
        jsonObject.put("limit", pageSize)

        socketManager.getMySingleChat(jsonObject)
        Log.d("jsodddfdf", jsonObject.toString())
        LoaderDialog.show(this)
    }

    private fun loadSingleOlderMessages() {
        if (isLoadingOldMessages) return
        isLoadingOldMessages = true

        val jsonObject = JSONObject().apply {
            put("sender_id", getPreference("id", ""))
            put("receiver_id", user2id)
            put("page", offset)
            put("limit", pageSize)
        }

        Log.d("sdfsddsg", "dsfdsfd")
        socketManager.getMySingleChat(jsonObject)
    }

    private fun getGroupMessage() {
        isLoadingOldMessages = true
        val jsonObject = JSONObject()
        jsonObject.put("sender_id", getPreference("id", ""))
        jsonObject.put("group_id", groupId)
        jsonObject.put("page", offset)
        jsonObject.put("limit", pageSize)
        Log.d("sdgsgfgfgf", jsonObject.toString())

        socketManager.getMyGroupChat(jsonObject)
        LoaderDialog.show(this)
    }

    private fun loadGroupOlderMessages() {
        if (isLoadingOldMessages) return
        isLoadingOldMessages = true

        val jsonObject = JSONObject().apply {
            put("sender_id", getPreference("id", ""))
            put("group_id", groupId)
            put("page", offset)
            put("limit", pageSize)
        }

        Log.d("sdgsgfgfgf", jsonObject.toString())
        socketManager.getMyGroupChat(jsonObject) // Call the socket function to fetch older messages
    }

    private fun readMessage() {
        if (list.isNotEmpty()) {
            val myId = getPreference("id", "")
            val lastReadMessageId = if (groupId.isNotEmpty() && groupId != "0") {
                list.lastOrNull()?.id?.toString() ?: "0"
            } else {
                list.lastOrNull { it.senderId.toString() != myId }?.id?.toString() ?: "0"
            }

            val jsonObject = JSONObject().apply {
                put("sender_id", myId.toInt())
                if (groupId.isEmpty()) {
                    put("receiver_id", user2id.toInt())
                    put("group_id", "0".toInt())
                } else {
                    put("receiver_id", "0".toInt())
                    put("group_id", groupId.toInt())
                }
                put("last_read_message_id", lastReadMessageId.toInt())
            }
            Log.d("safaf", jsonObject.toString())
            socketManager.readMessages(jsonObject)
        }
    }

    private fun sendMessage(message: String,msgType: String) {
        val jsonObject = JSONObject()
        jsonObject.put("sender_id", getPreference("id", ""))
        if (groupId.isNotEmpty()) {
            jsonObject.put("group_id", groupId)
            jsonObject.put("receiver_id", "0")
        } else {
            jsonObject.put("group_id", "0")
            jsonObject.put("receiver_id", user2id)
        }
        jsonObject.put("message", message)
        jsonObject.put("msg_type", msgType)
        Log.d("dsdgdsgd", jsonObject.toString())
        socketManager.sendMessage(jsonObject)
        LoaderDialog.show(this)
    }


    private fun muteUnmute(status: String) {
        val jsonObject = JSONObject()
        jsonObject.put("sender_id", getPreference("id", ""))
        if (groupId.isNotEmpty()) {
            jsonObject.put("group_id", groupId)
            jsonObject.put("receiver_id", "0")
        } else {
            jsonObject.put("group_id", "0")
            jsonObject.put("receiver_id", user2id)
        }
        jsonObject.put("status", status)
        socketManager.muteUnmuteChat(jsonObject)
    }

    private fun typing(status: String) {
        val jsonObject = JSONObject()
        jsonObject.put("sender_id", getPreference("id", ""))
        if (groupId.isNotEmpty()) {
            jsonObject.put("group_id", groupId)
            jsonObject.put("receiver_id", "0")
        } else {
            jsonObject.put("group_id", "0")
            jsonObject.put("receiver_id", user2id)
        }
        jsonObject.put("is_typing", status)
        jsonObject.put("constant_id", constantId)
        Log.d("fdsgdg", jsonObject.toString())
        socketManager.typing(jsonObject)
    }

    private fun deleteMessage(id: String) {
        val jsonObject = JSONObject()
        jsonObject.put("sender_id", getPreference("id", ""))
        if (groupId.isNotEmpty()) {
            jsonObject.put("group_id", groupId)
            jsonObject.put("receiver_id", "0")
        } else {
            jsonObject.put("group_id", "0")
            jsonObject.put("receiver_id", user2id)
        }
        jsonObject.put("delete_type", "1")
        jsonObject.put("message_id", id)
        Log.d("fdsgdg", jsonObject.toString())
        socketManager.deleteMessage(jsonObject)
    }


    private fun leaveGroup() {
        val jsonObject = JSONObject()
        jsonObject.put("user_id", getPreference("id", ""))
        jsonObject.put("group_id", groupId)

        socketManager.leaveGroupChat(jsonObject)
        LoaderDialog.show(this)
    }

    override fun onError(event: String, vararg args: Any) {
    }

    override fun onResponse(event: String, vararg args: Any) {
        when (event) {
            SocketManager.SEND_MESSAGE -> {
                lifecycleScope.launch(Dispatchers.Main) {
                   LoaderDialog.dismiss()
                    try {
                        val msgObject = args[0] as JSONObject
                        val msgObjectBody = msgObject.getJSONObject("body")
                        Log.d("fdsgdsgd", msgObject.toString())
                        val msg = Gson().fromJson(
                            msgObjectBody.toString(),
                            ChatResponse.Body.Data::class.java
                        )
                        var recId = ""
                        recId = if (msg.group != null) {
                            msg.group.id.toString()
                        } else {
                            if (msg.senderId.toString() == getPreference("id", "")) {
                                msg.receiverId.toString()
                            } else {
                                msg.senderId.toString()
                            }
                        }

                        if (msg.group != null) {
                            if (recId == groupId) {
                                list.add(msg)
                                adapter.notifyDataSetChanged()
                                binding.rvChat.scrollToPosition(list.size - 1)
                            }
//                            if (msg.senderId.toString() != getPreference("id", "")) {
                            readMessage()
//                            }

                        } else {
                            if (recId == user2id) {
                                list.add(msg)
                                adapter.notifyDataSetChanged()
                                binding.rvChat.scrollToPosition(list.size - 1)

                                Log.d("Sfsdfsff",msg.senderId.toString())
                                Log.d("Sfsdfsff",getPreference("id", ""))

                                if (msg.senderId.toString() != getPreference("id", "")) {
                                    Log.d("Sfsdfsff","hdsfbsfbsfsf")
                                    readMessage()
                                }
                            }
                        }

                    } catch (e: Exception) {
                        showCustomSnackbar(this@ChatActivity, binding.root, args[0].toString())
                    }
                }
            }

            SocketManager.GET_SINGLE_CHAT -> {
                lifecycleScope.launch(Dispatchers.Main) {
                   LoaderDialog.dismiss()
                    try {
                        val jsonArray = args[0] as JSONObject
                        Log.d("fdsgdsgd", jsonArray.toString())
                        val inbox = Gson().fromJson(jsonArray.toString(), ChatResponse::class.java)
                        val inboxList = inbox.body?.data
//                        list.clear()
                        totalPages = inbox.body?.totalPages ?: 0
                        if (!inboxList.isNullOrEmpty()) {
                            val reverseList = inboxList.reversed()
                            list.addAll(0, reverseList) // Add at the top
                            adapter.notifyItemRangeInserted(0, reverseList.size)
                            binding.rvChat.scrollToPosition(reverseList.size - 1)

                        }
                        offset += 1
                        readMessage()
                    } catch (e: Exception) {
                        Log.d("fdsgdsgd", args[0].toString())
                        // showCustomSnackbar(this@ChatActivity,binding.root,args[0].toString())
                    } finally {
                        isLoadingOldMessages = false
                    }
                }
            }

            SocketManager.GET_GROUP_CHAT -> {
                lifecycleScope.launch(Dispatchers.Main) {
                   LoaderDialog.dismiss()
                    try {
                        val jsonArray = args[0] as JSONObject
                        Log.d("fdsgdsgd", jsonArray.toString())
                        val inbox = Gson().fromJson(jsonArray.toString(), ChatResponse::class.java)
                        val inboxList = inbox.body?.data
                        totalPages = inbox.body?.totalPages ?: 0
                        if (!inboxList.isNullOrEmpty()) {
                            val reverseList = inboxList.reversed()
                            list.addAll(0, reverseList) // Add at the top
                            adapter.notifyItemRangeInserted(0, reverseList.size)
                            binding.rvChat.scrollToPosition(reverseList.size - 1)
                        }
                        offset += 1
                        readMessage()
                    } catch (e: Exception) {
                        Log.d("fdsgdsgd", args[0].toString())
                        //    showCustomSnackbar(this@ChatActivity,binding.root,args[0].toString())
                    } finally {
                        isLoadingOldMessages = false
                    }
                }
            }

            SocketManager.READ_UNREAD_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        val jsonObject = args[0] as JSONObject
                        Log.d("fdsgdsgd", jsonObject.toString())
                        val body = jsonObject.optJSONObject("body")
                        val lastMessageId = body?.optInt("last_read_message_id") ?: 0
                        val groupId = body?.optString("group_id") ?: ""
                        val senderId = body?.optString("sender_id") ?: ""
                        val receiverId = body?.optString("receiver_id") ?: ""
                        if (groupId != "0" && groupId.isNotEmpty()) {
                            if (groupId == this@ChatActivity.groupId) {
                                Log.d("dsmvdsfvfdgdf", "sdfdgdfdhdf")
                                list.forEach { message ->
                                    if ((message.id ?: 0) <= lastMessageId) {
                                        val isAlreadyRead =
                                            message.message_read_status?.any { it.user?.id.toString() == senderId }
                                        if (isAlreadyRead != true) {
                                            message.message_read_status?.add(
                                                ChatResponse.Body.Data.MessageReadStatus(
                                                    user_id = senderId,
                                                    user = ChatResponse.Body.Data.MessageReadStatus.User(
                                                        id = senderId
                                                    )
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                Log.d("dsmvdsfvfdgdf", "not same grouppppp")
                            }
                        }
                        else {
                            Log.d("jhvchjvsa",senderId)
                            Log.d("jhvchjvsa", getPreference("id",""))
                            Log.d("jhvchjvsa",this@ChatActivity.user2id)
                            if (senderId == this@ChatActivity.user2id) {
                                Log.d("dsmvdsfvfdgdf", "same user")
                                list.forEach {
                                    if ((it.id ?: 0) <= lastMessageId) {
                                        it.isRead = "1"
                                    }
                                }
                            } else {
                                Log.d("dsmvdsfvfdgdf", "not same user")
                            }
                        }
                        if (list.isNotEmpty()) {
                            adapter.notifyItemRangeChanged(0, list.size)
                        }
                    } catch (e: Exception) {
                        Log.d("fdsgdsgd", args[0].toString())
                        //    showCustomSnackbar(this@ChatActivity,binding.root,args[0].toString())
                    }
                }
            }

            SocketManager.LEAVE_GROUP_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main) {
                   LoaderDialog.dismiss()
                    try {
                        val jsonObject = args[0] as JSONObject
                        Log.d("fdsgdsgd", jsonObject.toString())
                        onBackPressedDispatcher.onBackPressed()
                    } catch (e: Exception) {
                        Log.d("fdsgdsgd", args[0].toString())
                    }
                }
            }


            SocketManager.MUTE_UNMUTE_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main) {
                   LoaderDialog.dismiss()
                    try {
                        val jsonObject = args[0] as JSONObject
                        var message = jsonObject.optString("message") ?: ""
                        showCustomSnackbar(this@ChatActivity, binding.root, message)
                        Log.d("fdsgdsgd", jsonObject.toString())
                    } catch (e: Exception) {
                        Log.d("fdsgdsgd", args[0].toString())
                    }
                }
            }

            SocketManager.TYPING_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        val jsonObject = args[0] as JSONObject
                        val jsonObj = jsonObject.getJSONObject("body")
                        Log.d("dsggdfgdfg", jsonObject.toString())
                        val receiverId = jsonObj.optString("sender_id") ?: ""
                        val group = jsonObj.optString("group_id") ?: ""
                        val isTyping = jsonObj.optString("is_typing") == "1"
                        if (groupId.isNotEmpty()) {
                            if (groupId == group) {
                                binding.tvMessage.text = if (isTyping) "$name is typing..." else ""
                                binding.tvMessage.visibility =
                                    if (isTyping) View.VISIBLE else View.GONE
                            }
                        } else {
                            Log.d("SDfsdfsdfsdfds", receiverId)
                            Log.d("SDfsdfsdfsdfds", user2id)
                            Log.d("SDfsdfsdfsdfds", isTyping.toString())
                            if (receiverId == user2id) {
                                binding.tvMessage.text = if (isTyping) "$name is typing..." else ""
                                binding.tvMessage.visibility =
                                    if (isTyping) View.VISIBLE else View.GONE
                            }
                        }

                    } catch (e: Exception) {
                        Log.d("TypingEvent", args[0].toString())
                    }
                }
            }

            SocketManager.DELETE_MESSAGE_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main) {
                   LoaderDialog.dismiss()
                    try {
                        val jsonObject = args[0] as JSONObject
                        Log.d("fdsgdsgd", jsonObject.toString())

                        val body = jsonObject.optJSONObject("body")
                        val messageId = body?.optString("message_id")

                        if (!messageId.isNullOrEmpty()) {
                            val iterator = list.iterator()
                            var removedPosition = -1

                            var index = 0
                            while (iterator.hasNext()) {
                                val message = iterator.next()
                                if (message.id.toString() == messageId) {
                                    iterator.remove()
                                    removedPosition = index
                                    break
                                }
                                index++
                            }

                            if (removedPosition >= 0) {
                                adapter.notifyItemRemoved(removedPosition)
                                Log.d("fdsgdsgd", "Removed message with id: $messageId at position: $removedPosition")
                            }
                        }

                    } catch (e: Exception) {
                        Log.d("fdsgdsgd", args[0].toString())
                    }

                }
            }

            SocketManager.MESSAGE_REQUEST_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main) {
                   LoaderDialog.dismiss()
                    try {
                        val jsonObject = args[0] as JSONObject
                        Log.d("fdsgdsgd", jsonObject.toString())
                        getMuteStatus()
                    } catch (e: Exception) {
                        Log.d("fdsgdsgd", args[0].toString())
                    }
                }
            }
        }
    }

    private fun setPopUpWindow(view1: View) {
        val menuBinding = ChatMenuBinding.inflate(layoutInflater)
        val myPopupWindow = PopupWindow(
            menuBinding.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        val activeColor = ContextCompat.getColor(this, R.color.blue)
        val inactiveColor = ContextCompat.getColor(this, R.color.button_grey)
        val trackColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ), intArrayOf(activeColor, inactiveColor)
        )

        if (groupId.isEmpty()) {
            menuBinding.clLeave.gone()
            menuBinding.divider15.gone()
            menuBinding.clGroupInfo.gone()
            menuBinding.divider12.gone()
        }
        menuBinding.switchMute.trackTintList = trackColorStateList

        menuBinding.switchMute.isChecked = muteStatus == "1"

        menuBinding.switchMute.setOnClickListener {
            if (menuBinding.switchMute.isChecked) {
                muteUnmute("1")
            } else {
                muteUnmute("0")
            }
        }

        menuBinding.clLeave.setOnClickListener {
            myPopupWindow.dismiss()
            leaveDialog()
        }
        menuBinding.clGroupInfo.setOnClickListener {
            myPopupWindow.dismiss()
            startActivity(
                Intent(
                    this@ChatActivity,
                    GuidelineParticipationActivity::class.java
                ).apply {
                    putExtra("id", groupId)
                })

        }

        myPopupWindow.showAsDropDown(view1, 0, -10)
    }

    private fun leaveDialog() {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = LogoutDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmationBinding.tvUsernameTaken.text = "Are you sure you want to leave this group chat?"
        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }

        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            leaveGroup()
        }
        customDialog.show()

    }

    override fun onResume() {
        super.onResume()
        socketManager.unRegister(this)
        socketManager.onRegister(this)

        socketManager.activateReadUnreadListener()
        socketManager.activateTypingListener()
        socketManager.deleteMessageListener()
        socketManager.activateMessageRequestListener()
        socketManager.activateSendMessageListener()
        ApiConstants.isNotification = true
    }


    override fun onDestroy() {
        super.onDestroy()
        socketManager.unRegister(this)
        ApiConstants.isNotification = false
    }

    private fun setPopUpWindowReportDelete(
        view1: View, id: String, type: Int, userId: String, userNa: String, pos: Int
    ) {
        val view = MenuReportDeleteDialogBinding.inflate(layoutInflater)

        val myPopupWindow = PopupWindow(
            view.root,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        with(view) {
            if (type == 1) {
                clReportUser.gone()
                view2.gone()
                clDeleteUser.visible()
                val params = clDeleteUser.layoutParams as LinearLayout.LayoutParams
                params.topMargin = 0
                clDeleteUser.layoutParams = params
                tvDelComm.text = "Delete Message"
            } else {
                clReportUser.visible()
                view2.gone()
                clDeleteUser.gone()
            }
            clDeleteUser.setOnClickListener {
                myPopupWindow.dismiss()
                deleteMessage(id)
                list.removeAt(pos)
                adapter.notifyItemRemoved(pos)
            }

            clReportUser.setOnClickListener {
                myPopupWindow.dismiss()
                startActivity(Intent(this@ChatActivity, ReportUserActivity::class.java).apply {
                    putExtra("from", "prayer")
                    putExtra("id", userId)
                    putExtra("username", username)
                })
            }
        }

        myPopupWindow.showAsDropDown(view1, 0, -80)
    }


  /*  private fun initBottomSheetBehavior() {
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d("kjbdgjfg",slideOffset.toString())
            }
        })
    }*/

   /* private fun imageVideoPickerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.image_video_picker)
        val window = dialog.window
        window!!.setGravity(Gravity.BOTTOM)
        window.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val camera = dialog.findViewById<TextView>(R.id.select_camera)
        val cancel = dialog.findViewById<TextView>(R.id.cancel)
        val gallery = dialog.findViewById<TextView>(R.id.select_photo_library)
        cancel.setOnClickListener { dialog.dismiss() }

        camera.setOnClickListener {
            dialog.dismiss()
            isVideo = false
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        gallery.setOnClickListener {
            dialog.dismiss()
            isVideo = true
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        }
        dialog.show()
    }*/

    override fun selectedImage(imagePath: String?, code: Int) {
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        if (!imagePath.isNullOrEmpty()){
            val map = HashMap<String, RequestBody>()
            if (code == 10 || code == 11){
                map["type"] = "video".toRequestBody()
                map["folder"] = "chat".toRequestBody()
            }else{
                map["type"] = "image".toRequestBody()
                map["folder"] = "chat".toRequestBody()
            }
            var filePart : MultipartBody.Part?= null

            filePart = prepareVideoPart("image", File(imagePath))

            LoaderDialog.show(this)
            viewModel.postFileUpload(map, filePart, this).observe(this){value->
                when (value.status) {
                    Status.SUCCESS -> {
                        LoaderDialog.dismiss()
                        when (value.data) {
                            is FileUploadResponse -> {
                                val file = value.data.body?.firstOrNull()?.image ?: ""
                                sendMessage(file,"1")
                            }
                        }
                    }
                    Status.ERROR -> {
                        LoaderDialog.dismiss()
                        showCustomSnackbar(this, binding.root, value.message.toString())

                    }
                    else -> Unit
                }
            }

        }


    }

}