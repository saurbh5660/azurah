package com.live.azurah.socket

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.getPreference
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.Transport
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.*

class SocketManager {
    companion object {
        const val CONNECT_USER = "connectUser"
        const val CREATE_GROUP = "createGroup"
        const val JOIN_GROUP = "joinGroup"
        const val CHAT_LIST = "chatList"
        const val SEND_MESSAGE = "sendMessage"
        const val GET_SINGLE_CHAT = "getSingleChat"
        const val GET_GROUP_CHAT = "getGroupChat"
        const val READ_UNREAD = "readUnread"
        const val MUTE_UNMUTE_CHAT = "muteUnmuteChat"
        const val LEAVE_GROUP = "leaveGroup"
        const val TYPING = "typing"
        const val DELETE_MESSAGE = "deleteMessage"
        const val MESSAGE_REQUEST = "messageRequestAction"

        const val CONNECT_LISTENER = "connectListener"
        const val CREATE_GROUP_LISTENER = "createGroupListener"
        const val JOIN_GROUP_LISTENER = "joinGroupListener"
        const val CHAT_LIST_LISTENER = "chatListListener"
        const val SEND_MESSAGE_LISTENER = "sendMessageListener"
        const val SEND_MESSAGE_GROUP_LISTENER = "sendGroupMessageListener"
        const val GET_SINGLE_CHAT_LISTENER = "getSingleChatListener"
        const val GET_GROUP_CHAT_LISTENER = "getGroupChatListener"
        const val READ_UNREAD_LISTENER = "readUnreadListener"
        const val MUTE_UNMUTE_LISTENER = "muteUnmuteChatListener"
        const val LEAVE_GROUP_LISTENER = "leaveGroupListener"
        const val TYPING_LISTENER = "typingListener"
        const val DELETE_MESSAGE_LISTENER = "deleteMessageListener"
        const val MESSAGE_REQUEST_LISTENER = "messageRequestActionListener"


    }
    var t = 0
    private var mSocket: Socket? = null
    private var context: Context? = null
    private var observerList: MutableList<Observer>? = null

     fun isConnected(): Boolean {
        return mSocket != null && mSocket!!.connected()
    }

    fun getmSocket(): Socket? {
        return mSocket
    }

    private fun getSocket(): Socket? {
        run {
            try {
                mSocket = IO.socket(ApiConstants.SOCKET_BASE_URL)
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            }
        }
        return mSocket
    }

    fun init() {
        initializeSocket()
    }


    private val onConnectResponce = Emitter.Listener { args ->
        Log.d("Socket","Connected --- "+ args.toString())
        for (observerlist in observerList!!) {
        }
    }

    private val onConnect = Emitter.Listener {
        if (isConnected()) {
            try {
                val jsonObject = JSONObject()

                val userId = getPreference("id", "")
                if (userId.isNotEmpty()) {
                    if (userId.toInt() != 0) {
                        jsonObject.put("user_id", userId.toInt())
                        mSocket!!.off(CONNECT_LISTENER)
                        mSocket!!.on(CONNECT_LISTENER, onConnectResponce)
                        Handler(Looper.getMainLooper()).post {
                            mSocket!!.emit(CONNECT_USER, jsonObject)
                        }
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            initializeSocket()
        }
    }

     fun disconnect() {
        if (mSocket != null) {
            mSocket!!.off(Socket.EVENT_CONNECT, onConnect)
            mSocket!!.off(Socket.EVENT_DISCONNECT, onDisconnect)
            mSocket!!.off(Socket.EVENT_CONNECT_ERROR, onConnectError)

            mSocket!!.off(SEND_MESSAGE_LISTENER, onBodyListener)
            mSocket!!.off(SEND_MESSAGE_GROUP_LISTENER, onBodyListener)
            mSocket!!.off(GET_SINGLE_CHAT_LISTENER, onMySingleChatListener)
            mSocket!!.off(GET_GROUP_CHAT_LISTENER, onMyGroupChatListener)
            mSocket!!.off(JOIN_GROUP_LISTENER, onJoinGroupListener)
            mSocket!!.off(CHAT_LIST_LISTENER, onInboxListener)
            mSocket!!.off(READ_UNREAD_LISTENER, onReadListener)
            mSocket!!.off(DELETE_MESSAGE_LISTENER,onDeleteMessageListener)
            mSocket!!.off()
            mSocket!!.disconnect()
        }
    }

    fun onRegister(observer: Observer) {
        if (observerList != null && !observerList!!.contains(observer)) {
            observerList!!.clear()
            observerList!!.add(observer)
        } else {
            observerList = ArrayList()
            observerList!!.clear()
            observerList!!.add(observer)
        }
    }

    fun unRegister(observer: Observer) {
        observerList?.let { list ->
            for (i in 0 until list.size - 1) {
                val model = list[i]
                if (model == observer) {
                    observerList?.remove(model)
                }
            }
        }
    }

    private val onDisconnect = Emitter.Listener { args ->
        mSocket!!.connect()
        for (observer in observerList!!) {
            observer.onError("errorSocket", args)
        }
    }

    private val onConnectError = Emitter.Listener { args ->
        mSocket!!.connect()
        Log.d("Socket","errorr---"+args.toString())
        for (observer in observerList!!) {
            observer.onError("errorSocket", args)
        }
    }

    fun readMessages(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(READ_UNREAD_LISTENER)
            mSocket!!.on(READ_UNREAD_LISTENER, onReadListener)
            mSocket!!.emit(READ_UNREAD, jsonObject)

        } else {
            mSocket!!.off(READ_UNREAD_LISTENER)
            mSocket!!.on(READ_UNREAD_LISTENER, onReadListener)
            mSocket!!.emit(READ_UNREAD, jsonObject)

        }
    }

    fun sendMessage(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(SEND_MESSAGE_LISTENER)
            mSocket!!.off(SEND_MESSAGE_GROUP_LISTENER)
            mSocket!!.on(SEND_MESSAGE_LISTENER, onBodyListener)
            mSocket!!.on(SEND_MESSAGE_GROUP_LISTENER, onBodyListener)
            mSocket!!.emit(SEND_MESSAGE, jsonObject)

        } else {
            mSocket!!.off(SEND_MESSAGE_LISTENER)
            mSocket!!.off(SEND_MESSAGE_GROUP_LISTENER)
            mSocket!!.on(SEND_MESSAGE_LISTENER, onBodyListener)
            mSocket!!.on(SEND_MESSAGE_GROUP_LISTENER, onBodyListener)
            mSocket!!.emit(SEND_MESSAGE, jsonObject)

        }
    }

    fun getInbox(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(CHAT_LIST_LISTENER)
            mSocket!!.on(CHAT_LIST_LISTENER, onInboxListener)
            mSocket!!.emit(CHAT_LIST, jsonObject)

        } else {
            mSocket!!.off(CHAT_LIST_LISTENER)
            mSocket!!.on(CHAT_LIST_LISTENER, onInboxListener)
            mSocket!!.emit(CHAT_LIST, jsonObject)

        }
    }

    fun getMySingleChat(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(GET_SINGLE_CHAT_LISTENER)
            mSocket!!.on(GET_SINGLE_CHAT_LISTENER, onMySingleChatListener)
            mSocket!!.emit(GET_SINGLE_CHAT, jsonObject)

        } else {
            mSocket!!.off(GET_SINGLE_CHAT_LISTENER)
            mSocket!!.on(GET_SINGLE_CHAT_LISTENER, onMySingleChatListener)
            mSocket!!.emit(GET_SINGLE_CHAT, jsonObject)

        }
    }

    fun getMyGroupChat(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(GET_GROUP_CHAT_LISTENER)
            mSocket!!.on(GET_GROUP_CHAT_LISTENER, onMyGroupChatListener)
            mSocket!!.emit(GET_GROUP_CHAT, jsonObject)

        } else {
            mSocket!!.off(GET_GROUP_CHAT_LISTENER)
            mSocket!!.on(GET_GROUP_CHAT_LISTENER, onMyGroupChatListener)
            mSocket!!.emit(GET_GROUP_CHAT, jsonObject)

        }
    }

    fun joinGroupChat(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(JOIN_GROUP_LISTENER)
            mSocket!!.on(JOIN_GROUP_LISTENER, onJoinGroupListener)
            mSocket!!.emit(JOIN_GROUP, jsonObject)

        } else {
            mSocket!!.off(JOIN_GROUP_LISTENER)
            mSocket!!.on(JOIN_GROUP_LISTENER, onJoinGroupListener)
            mSocket!!.emit(JOIN_GROUP, jsonObject)
        }
    }

    fun muteUnmuteChat(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(MUTE_UNMUTE_LISTENER)
            mSocket!!.on(MUTE_UNMUTE_LISTENER, onMuteUnmuteListener)
            mSocket!!.emit(MUTE_UNMUTE_CHAT, jsonObject)

        } else {
            mSocket!!.off(MUTE_UNMUTE_LISTENER)
            mSocket!!.on(MUTE_UNMUTE_LISTENER, onMuteUnmuteListener)
            mSocket!!.emit(MUTE_UNMUTE_CHAT, jsonObject)

        }
    }

    fun requestGroupChat(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(CREATE_GROUP_LISTENER)
            mSocket!!.on(CREATE_GROUP_LISTENER, onRequestGroupListener)
            mSocket!!.emit(CREATE_GROUP, jsonObject)

        } else {
            mSocket!!.off(CREATE_GROUP_LISTENER)
            mSocket!!.on(CREATE_GROUP_LISTENER, onRequestGroupListener)
            mSocket!!.emit(CREATE_GROUP, jsonObject)

        }
    }

    fun leaveGroupChat(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(LEAVE_GROUP_LISTENER)
            mSocket!!.on(LEAVE_GROUP_LISTENER, onLeaveGroupListener)
            mSocket!!.emit(LEAVE_GROUP, jsonObject)

        } else {
            mSocket!!.off(LEAVE_GROUP_LISTENER)
            mSocket!!.on(LEAVE_GROUP_LISTENER, onLeaveGroupListener)
            mSocket!!.emit(LEAVE_GROUP, jsonObject)

        }
    }

    fun typing(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(TYPING_LISTENER)
            mSocket!!.on(TYPING_LISTENER, onTypingListener)
            mSocket!!.emit(TYPING, jsonObject)

        } else {
            mSocket!!.off(TYPING_LISTENER)
            mSocket!!.on(TYPING_LISTENER, onTypingListener)
            mSocket!!.emit(TYPING, jsonObject)

        }
    }

    fun deleteMessage(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(DELETE_MESSAGE_LISTENER)
            mSocket!!.on(DELETE_MESSAGE_LISTENER, onDeleteMessageListener)
            mSocket!!.emit(DELETE_MESSAGE, jsonObject)

        } else {
            mSocket!!.off(DELETE_MESSAGE_LISTENER)
            mSocket!!.on(DELETE_MESSAGE_LISTENER, onDeleteMessageListener)
            mSocket!!.emit(DELETE_MESSAGE, jsonObject)

        }
    }

    fun messageRequest(jsonObject: JSONObject?) {
        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            mSocket!!.off(MESSAGE_REQUEST_LISTENER)
            mSocket!!.on(MESSAGE_REQUEST_LISTENER, onMessageRequestListener)
            mSocket!!.emit(MESSAGE_REQUEST, jsonObject)

        } else {
            mSocket!!.off(MESSAGE_REQUEST_LISTENER)
            mSocket!!.on(MESSAGE_REQUEST_LISTENER, onMessageRequestListener)
            mSocket!!.emit(MESSAGE_REQUEST, jsonObject)

        }
    }


    private val onReadListener = Emitter.Listener { args ->
        Log.d("fdshgldffdh",args.toString())
        for (observer in observerList!!) {
            observer.onResponse(READ_UNREAD_LISTENER, args[0])
        }
    }


    private val onDeleteMessageListener = Emitter.Listener { args ->
        Log.d("fdshgldffdh",args.toString())
        for (observer in observerList!!) {
            observer.onResponse(DELETE_MESSAGE_LISTENER, args[0])
        }
    }


    private val onMessageRequestListener = Emitter.Listener { args ->
        Log.d("fdshgldffdh",args.toString())
        for (observer in observerList!!) {
            observer.onResponse(MESSAGE_REQUEST_LISTENER, args[0])
        }
    }

    private val onBodyListener = Emitter.Listener { args ->
        Log.d("fdshgldffdh",args.toString())
        for (observer in observerList!!) {
            observer.onResponse(SEND_MESSAGE, args[0])
        }
    }


    private val onMySingleChatListener = Emitter.Listener { args ->
        Log.d("gerghgerger",args.toString())
        for (observer in observerList!!) {
            observer.onResponse(GET_SINGLE_CHAT, args[0])
        }
    }

    private val onMyGroupChatListener = Emitter.Listener { args ->
        Log.d("gerghgerger",args.toString())
        for (observer in observerList!!) {
            observer.onResponse(GET_GROUP_CHAT, args[0])
        }
    }

    private val onInboxListener = Emitter.Listener { args ->
        for (observer in observerList!!) {
            observer.onResponse(CHAT_LIST_LISTENER, args[0])
        }
    }

    private val onJoinGroupListener = Emitter.Listener { args ->
        Log.d("gerghgerger",args.toString())
        for (observer in observerList!!) {
            observer.onResponse(JOIN_GROUP_LISTENER, args[0])
        }
    }

    private val onMuteUnmuteListener = Emitter.Listener { args ->
        Log.d("gerghgerger",args.toString())
        for (observer in observerList!!) {
            observer.onResponse(MUTE_UNMUTE_LISTENER,args[0])
        }
    }

    private val onLeaveGroupListener = Emitter.Listener { args ->
        Log.d("gerghgerger",args.toString())
        for (observer in observerList!!) {
            observer.onResponse(LEAVE_GROUP_LISTENER, args[0])
        }
    }

    private val onRequestGroupListener = Emitter.Listener { args ->
        Log.d("gerghgerger",args.toString())
        for (observer in observerList!!) {
            observer.onResponse(CREATE_GROUP_LISTENER, args[0])
        }
    }

    private val onTypingListener = Emitter.Listener { args ->
        Log.d("gerghgerger",args.toString())
        for (observer in observerList!!) {
            observer.onResponse(TYPING_LISTENER, args[0])
        }
    }


    fun deleteMessageListener(){
        mSocket!!.off(DELETE_MESSAGE_LISTENER)
        mSocket!!.on(DELETE_MESSAGE_LISTENER, onDeleteMessageListener)
    }

    fun activateReadUnreadListener() {
        mSocket!!.off(READ_UNREAD_LISTENER)
        mSocket!!.on(READ_UNREAD_LISTENER, onReadListener)
    }

    fun activateTypingListener() {
        mSocket!!.off(TYPING_LISTENER)
        mSocket!!.on(TYPING_LISTENER, onTypingListener)
    }

    fun activateMessageRequestListener() {
        mSocket!!.off(MESSAGE_REQUEST_LISTENER)
        mSocket!!.on(MESSAGE_REQUEST_LISTENER, onMessageRequestListener)
    }

    fun activateSendMessageListener() {
        mSocket!!.off(SEND_MESSAGE_LISTENER)
        mSocket!!.on(SEND_MESSAGE_LISTENER, onBodyListener)
    }


    private fun initializeSocket() {
        if (mSocket == null) {
            mSocket = getSocket()
        }
        if (observerList == null || observerList!!.size == 0) {
            observerList = ArrayList()
        }

        mSocket!!.io().on(Manager.EVENT_TRANSPORT) { args ->
            val transport: Transport = args[0] as Transport
            transport.on(Transport.EVENT_REQUEST_HEADERS) { args ->
                println("Caught EVENT_REQUEST_HEADERS after EVENT_TRANSPORT, adding headers")
                val mHeaders = args[0] as MutableMap<String, List<String>>
                mHeaders["secret_key"] = listOf(ApiConstants.SECRET_KEY)
                mHeaders["publish_key"] = listOf(ApiConstants.PUBLISH_KEY)
            }
        }

        disconnect()
        mSocket!!.connect()
        mSocket!!.on(Socket.EVENT_CONNECT, onConnect)
        mSocket!!.on(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket!!.on(Socket.EVENT_CONNECT_ERROR, onConnectError)

        mSocket!!.on(SEND_MESSAGE_LISTENER, onBodyListener)
        mSocket!!.on(SEND_MESSAGE_GROUP_LISTENER, onBodyListener)
        mSocket!!.on(GET_SINGLE_CHAT_LISTENER, onMySingleChatListener)
        mSocket!!.on(GET_GROUP_CHAT_LISTENER, onMyGroupChatListener)
        mSocket!!.on(JOIN_GROUP_LISTENER, onJoinGroupListener)
        mSocket!!.on(CHAT_LIST_LISTENER, onInboxListener)
        mSocket!!.on(READ_UNREAD_LISTENER, onReadListener)
        mSocket!!.on(DELETE_MESSAGE_LISTENER, onDeleteMessageListener)

    }

    interface Observer {
        fun onError(event: String, vararg args: Any)
        fun onResponse(event: String, vararg args: Any)
    }

}