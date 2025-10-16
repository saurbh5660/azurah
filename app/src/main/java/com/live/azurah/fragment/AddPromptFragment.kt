package com.live.azurah.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.live.azurah.activity.HomeActivity
import com.live.azurah.adapter.PopUpPromptAdapter
import com.live.azurah.adapter.PromptAdapter
import com.live.azurah.databinding.FragmentAddPromptBinding
import com.live.azurah.databinding.PopupPromptBinding
import com.live.azurah.model.InterestResponse
import com.live.azurah.model.PromptModel
import com.live.azurah.model.QuestionResponse
import com.live.azurah.model.SignUpResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddPromptFragment : Fragment(),PromptAdapter.ClickListener,PopUpPromptAdapter.ClickListener,
    Observer<Resource<Any>> {
    private lateinit var binding: FragmentAddPromptBinding
    private val promptList = ArrayList<PromptModel>()
    private val questionList = ArrayList<QuestionResponse.Body>()
    private lateinit var adapter : PromptAdapter
    private var myPopupWindow: PopupWindow? = null
    private var position = 0
//    private val loaderDialog by lazy { LoaderDialog(requireContext()) }
    private val viewModel by viewModels<CommonViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPromptBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        promptList.clear()
        promptList.add(PromptModel())
        initListener()
        setAdapter()
        getQuestion()
    }

    private fun setAdapter() {
         adapter = PromptAdapter(requireContext(), promptList, 0, this)
        binding.rvPrompts.adapter = adapter
    }

    private fun initListener() {
        with(binding){
            tvAddPrompt.setOnClickListener {
                promptList.add(PromptModel())
                adapter.notifyDataSetChanged()

                if (promptList.size > 2){
                    tvAddPrompt.visibility = View.GONE
                }else{
                    tvAddPrompt.visibility = View.VISIBLE

                }
            }
            backIcon.setOnClickListener {
                requireActivity().finish()
            }
            tvSkip.setOnClickListener {
                val map = HashMap<String,String>()
                map["is_profile_completed"] = "1"
                map["form_step"] = "8"
                viewModel.editProfile(map,requireActivity()).observe(viewLifecycleOwner,this@AddPromptFragment)
            }

            btnFinish.setOnClickListener {
                val question = promptList.any { it.question_id.isNullOrEmpty()}
                val answer = promptList.any { it.description.isNullOrEmpty()}
                if (question){
                    showCustomSnackbar(requireActivity(),binding.root, "Please select question.")
                    return@setOnClickListener
                }
                if (answer){
                    showCustomSnackbar(requireActivity(),binding.root, "Please enter description.")
                    return@setOnClickListener
                }
                val data = Gson().toJson(promptList)
                Log.d("dataaaaaaaaaaa",data)
                val map = HashMap<String,String>()
                map["answer_ids"] = data
                map["is_profile_completed"] = "1"
                map["form_step"] = "8"

                viewModel.editProfile(map,requireActivity()).observe(viewLifecycleOwner,this@AddPromptFragment)
            }
        }
    }

    private fun getQuestion(){
        viewModel.getQuestion(requireActivity()).observe(requireActivity(),this)
    }

    override fun onCLick(type: Int,view: View,pos:Int) {
        position = pos
        if (type == 0){
            if (promptList.size > 2){
                binding.tvAddPrompt.visibility = View.GONE
            }else{
                binding.tvAddPrompt.visibility = View.VISIBLE

            }
        }else{
            setPopUpWindow(view,pos)
        }

    }

    private fun setPopUpWindow(view1: View,pos: Int) {
        val inflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popUpPromptBinding = PopupPromptBinding.inflate(inflater)

         myPopupWindow = PopupWindow(
            popUpPromptBinding.root,
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        val adapter = PopUpPromptAdapter(requireContext(),questionList,this,pos)
        popUpPromptBinding.rvPrompts.adapter = adapter
        myPopupWindow?.showAsDropDown(view1, 0, 0)

    }

    override fun onPopClick(pos: Int,qusPos:Int) {
        promptList[qusPos].name = questionList[pos].title
        promptList[qusPos].question_id = questionList[pos].id.toString()
        adapter.notifyItemChanged(qusPos)
        myPopupWindow?.dismiss()
    }

    override fun onResume() {
        super.onResume()
       /* requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        )*/
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is QuestionResponse -> {
                        questionList.clear()
                        value.data.body?.let { questionList.addAll(it) }
                    }
                    is SignUpResponse -> {
                        val res = value.data.body
                        showCustomSnackbar(requireActivity(),binding.root, "Profile Completed Successfully.")
                        lifecycleScope.launch {
                            delay(700)
                            savePreference("firstName",res?.first_name ?: "")
                            savePreference("lastName",res?.last_name ?: "")
                            savePreference("email",res?.email ?: "")
                            savePreference("isLogin",true)
                            savePreference("id",res?.id.toString())
                            ApiConstants.isMute = true
                            startActivity(Intent(requireContext(),HomeActivity::class.java).apply {
                                putExtra("from","1")
                            })
                            requireActivity().finishAffinity()
                        }

                    }
                }
            }
            Status.LOADING -> {
                LoaderDialog.show(requireActivity())
            }

            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(requireActivity(),binding.root, value.message.toString())

            }
        }
    }
}