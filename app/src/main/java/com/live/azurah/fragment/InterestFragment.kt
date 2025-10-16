package com.live.azurah.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.live.azurah.R
import com.live.azurah.activity.SetUpProfileActivity
import com.live.azurah.adapter.InterestAdapter
import com.live.azurah.databinding.FragmentInterestBinding
import com.live.azurah.model.InterestModel
import com.live.azurah.model.InterestResponse
import com.live.azurah.model.SignUpResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InterestFragment : Fragment(),InterestAdapter.ClickListener, Observer<Resource<Any>> {
    private lateinit var binding: FragmentInterestBinding
    private lateinit var adapter: InterestAdapter
    private  var list  = ArrayList<InterestResponse.Body>()
//    private val loaderDialog by lazy { LoaderDialog(requireContext()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var from = ""
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInterestBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        from = arguments?.getString("from") ?:""

        if (from == "edit"){
            binding.btnNext.text = "Save"
        }else{
            binding.btnNext.text  = "Next"
        }
        getInterest()

        setInterestAdapter()
        initListener()
    }

    private fun getInterest(){
        viewModel.getInterest(requireActivity()).observe(requireActivity(),this)
    }

    private fun setInterestAdapter() {
        adapter = InterestAdapter(requireContext(),list,this)
        binding.rvInterest.adapter = adapter
    }

    private fun initListener() {
        with(binding){
            val selectedList = list.filter { it.isSelected }
            tvPrompt.text = selectedList.size.toString()+"/6"
            btnNext.setOnClickListener {
                val selectedInterest = list.filter { it.isSelected }
                if (from == "edit"){
                    sharedViewModel.setInterestData(selectedInterest as ArrayList)
                    requireActivity().supportFragmentManager.popBackStack()

                }else{
                    val ids = selectedInterest.map { it.id.toString() }
                    val map = HashMap<String, String>()
                    map["interest_ids"] = ids.joinToString(prefix = "[", separator = ",", postfix = "]")
                    map["form_step"] = "7"
                    viewModel.editProfile(map,requireActivity()).observe(requireActivity(),this@InterestFragment)
                }
            }
            backIcon.setOnClickListener {
                if (from == "edit"){
                  requireActivity().supportFragmentManager.popBackStack()
                }else{
                    requireActivity().finish()
                }
            }
        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is InterestResponse -> {
                        list.clear()
                        value.data.body?.let { list.addAll(it) }

                        if (from == "edit"){
                            val oldInterestList = sharedViewModel.interest.value

                            list.forEach { model->
                                oldInterestList?.forEach {
                                    if (it.id == model.id && it.isSelected){
                                        model.isSelected = true
                                    }
                                }
                            }
                        }

                        adapter.notifyDataSetChanged()


                    }
                    is SignUpResponse -> {
                        replaceFragment(AddPromptFragment())
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

    private fun replaceFragment(fragment: Fragment){
        with(requireActivity() as SetUpProfileActivity){
            requireActivity().supportFragmentManager.beginTransaction().replace(binding.frameContainer.id, fragment).addToBackStack(null).commit()
        }
    }

    override fun onClick() {
        val selectedList = list.filter { it.isSelected }
       binding.tvPrompt.text = selectedList.size.toString()+"/6"

        if (list.any { it.isSelected }){
            binding.btnNext.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.blue)
            binding.btnNext.isEnabled= true
        }else{
            binding.btnNext.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.button_grey)
            binding.btnNext.isEnabled= false
        }
    }

}