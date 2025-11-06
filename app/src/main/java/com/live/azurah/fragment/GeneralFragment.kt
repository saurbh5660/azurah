package com.live.azurah.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.live.azurah.activity.MessageActivity
import com.live.azurah.adapter.InboxAdapter
import com.live.azurah.controller.MyApplication
import com.live.azurah.databinding.FragmentGeneralBinding
import com.live.azurah.model.ChatResponse
import com.live.azurah.model.InboxResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.socket.SocketManager
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@AndroidEntryPoint
class GeneralFragment : Fragment(), SocketManager.Observer {
    private lateinit var binding: FragmentGeneralBinding
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var socketManager: SocketManager
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var search = ""
    private var showDialog = false
    private lateinit var adapter: InboxAdapter
    private val list = ArrayList<InboxResponse.Body.Data>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGeneralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        setAdapter()
        socketManager = MyApplication.instance!!.getSocketManager()!!
        if (!socketManager.isConnected() || socketManager.getmSocket() == null) {
            socketManager.init()
        }

        sharedViewModel.search.observe(viewLifecycleOwner) {
            search = it
            searchData()
        }
    }

    private fun searchData() {
        if (search.isNotEmpty()) {

            val newList = list.filter {
                val name = if (getPreference(
                        "id",
                        ""
                    ) == it.message?.firstOrNull()?.messageSender?.id.toString()
                ) {
                    it.message?.firstOrNull()?.messageReceiver?.username ?: ""
                } else {
                    it.message?.firstOrNull()?.messageSender?.username ?: ""
                }
                name.contains(search, true)
            } as ArrayList
            val adapter = InboxAdapter(requireContext(), newList, 0)
            binding.rvInbox.adapter = adapter
            if (newList.isNotEmpty()) {
                binding.tvNoDataFound.gone()
                binding.tvNoDataFound.text = buildString { append("No messages yet. \n" +
                        "Start a conversation!") }
            } else {
                binding.tvNoDataFound.visible()
            }
        } else {
            setAdapter()
        }
    }

    private fun getInbox() {
        val jsonObject = JSONObject()
        jsonObject.put("sender_id", getPreference("id", ""))
        jsonObject.put("chat_type", "1")
        jsonObject.put("page", "1")
        jsonObject.put("limit", "1000")
        socketManager.getInbox(jsonObject)
    }


    private fun setAdapter() {
        adapter = InboxAdapter(requireContext(), list, 0)
        binding.rvInbox.adapter = adapter
       /* if (list.isNotEmpty()) {
            binding.tvNoDataFound.gone()
        } else {
            binding.tvNoDataFound.visible()
            binding.tvNoDataFound.text = buildString { append("No messages yet. \n" +
                    "Start the conversation!") }
        }*/
    }

    override fun onError(event: String, vararg args: Any) {
    }

    override fun onResponse(event: String, vararg args: Any) {
        when (event) {
            SocketManager.CHAT_LIST_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main) {
                  binding.progressBar.gone()
                    try {
                        val jsonArray = args[0] as JSONObject
                        Log.d("sdfsdsddsfd", args[0].toString())

                        val inbox =
                            Gson().fromJson(jsonArray.toString(), InboxResponse::class.java)
                        val inboxList = inbox.body?.data
                        list.clear()
                        if (!inboxList.isNullOrEmpty()) {
                            list.addAll(inboxList)
                        }
                        adapter.notifyDataSetChanged()

                        if (list.isEmpty()) {
                            binding.tvNoDataFound.visible()
                            binding.tvNoDataFound.text = buildString { append("No messages yet. \n" +
                                    "Start the conversation!") }
                        } else {
                            binding.tvNoDataFound.gone()
                        }
                        if (search.isNotEmpty()) {
                            searchData()
                        }

                      /*  with(requireActivity() as MessageActivity) {
                            val unreadCount = list.map { it.unreadCount }.sumOf { it ?: 0 }
                            if (unreadCount != 0) {
                                binding.ivGeneralCount.text = unreadCount.toString()
                                binding.ivGeneralCount.visible()
                            } else {
                                binding.ivGeneralCount.gone()
                            }
                        }*/

                    } catch (e: Exception) {
                        Log.d("sdfsdsddsfd", args[0].toString())
                        showCustomSnackbar(requireActivity(), binding.root, args[0].toString())
                    }
                }
            }
            SocketManager.SEND_MESSAGE -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        showDialog = false
                        getInbox()
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        socketManager.unRegister(this)
        socketManager.onRegister(this)
        binding.progressBar.visible()
        socketManager.activateSendMessageListener()
        getInbox()
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.unRegister(this)
    }
}