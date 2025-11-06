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
import com.live.azurah.databinding.FragmentGroupChatBinding
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
class GroupChatFragment : Fragment() , SocketManager.Observer{
    private lateinit var binding: FragmentGroupChatBinding
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var socketManager: SocketManager
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private lateinit var adapter: InboxAdapter
    private var search = ""
    private val list = ArrayList<InboxResponse.Body.Data>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  FragmentGroupChatBinding.inflate(inflater,container,false)
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
        sharedViewModel.search.observe(viewLifecycleOwner){
            search = it
            searchData()
        }
    }

    private fun searchData(){
        if (search.isNotEmpty()){
             val newList = list.filter { it.group?.name.toString().contains(search,true) } as ArrayList
             val adapter = InboxAdapter(requireContext(),newList,1)
             binding.rvInbox.adapter = adapter
            if (newList.isNotEmpty()){
                binding.tvNoDataFound.gone()
            }else{
                binding.tvNoDataFound.visible()
                binding.tvNoDataFound.text = buildString { append("No message request yet!") }

               /* binding.tvNoDataFound.text = buildString { append("Group chats you’ve \n" +
                        "joined will appear here.") }*/

            }
        }else{
            setAdapter()
        }
    }
    private fun getInbox() {
        val jsonObject = JSONObject()
        jsonObject.put("sender_id", getPreference("id", ""))
//        jsonObject.put("chat_type", "2")
        jsonObject.put("chat_type", "3")
        jsonObject.put("page", "1")
        jsonObject.put("limit", "1000")
        socketManager.getInbox(jsonObject)
    }

    private fun setAdapter() {
        adapter = InboxAdapter(requireContext(),list,1)
        binding.rvInbox.adapter = adapter
       /* if (list.isNotEmpty()){
            binding.tvNoDataFound.gone()
        }else{
            binding.tvNoDataFound.visible()
            binding.tvNoDataFound.text = buildString { append("No message requests yet!") }
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
                        Log.d("dfddf",jsonArray.toString())
                        val inbox =
                            Gson().fromJson(jsonArray.toString(), InboxResponse::class.java)
                        val inboxList = inbox.body?.data
                        list.clear()
                        if (!inboxList.isNullOrEmpty()) {
                            list.addAll(inboxList)
                        }
                        adapter.notifyDataSetChanged()
                        if (list.isEmpty()){
                            binding.tvNoDataFound.visible()
                            binding.tvNoDataFound.text = buildString { append("No message requests yet!") }
                           /* binding.tvNoDataFound.text = buildString { append("Group chats you’ve \n" +
                                    "joined will appear here.") }*/
                        }else{
                            binding.tvNoDataFound.gone()
                        }

                        if (search.isNotEmpty()){
                            searchData()
                        }

                        /*with(requireActivity() as MessageActivity){
                            val unreadCount = list.map { it.unreadCount }.sumOf { it ?: 0 }
                            if (unreadCount !=0) {
                                binding.ivGroupCount.text = unreadCount.toString()
                                binding.ivGroupCount.visible()
                            }else{
                                binding.ivGroupCount.gone()
                            }
                        }*/

                    } catch (e: Exception) {
                        Log.d("sdfsdsddsfd",args[0].toString())
                        showCustomSnackbar(requireActivity(),binding.root, args[0].toString())
                    }
                }
            }
            SocketManager.SEND_MESSAGE -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    try {
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
   /*     LoaderDialog.show(requireActivity())*/
        binding.progressBar.visible()
        socketManager.activateSendMessageListener()
        getInbox()

    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.unRegister(this)
    }
}