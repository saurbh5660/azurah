package com.live.azurah.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.live.azurah.activity.HomeActivity
import com.live.azurah.adapter.InterestAdapter
import com.live.azurah.adapter.PromptAdapter
import com.live.azurah.databinding.FragmentAboutBinding
import com.live.azurah.model.CountryModel
import com.live.azurah.model.InterestModel
import com.live.azurah.model.InterestResponse
import com.live.azurah.model.PromptModel
import com.live.azurah.model.countryInfoList
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.util.gone
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment(var from :Int = 0) : Fragment(),InterestAdapter.ClickListener,PromptAdapter.ClickListener {
    private lateinit var binding: FragmentAboutBinding
    private  var list  = ArrayList<InterestResponse.Body>()
    private lateinit var adapter: InterestAdapter

    private val promptList = ArrayList<PromptModel>()
    private lateinit var promptAdapter : PromptAdapter
//    private val loaderDialog by lazy { LoaderDialog(requireContext()) }
    private val viewModel by viewModels<CommonViewModel>()
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        setInterestAdapter()

        binding.tvChoosePrompt.text = "Get to Know Me:"

        sharedViewModel.profile.observe(viewLifecycleOwner, Observer {
            val data = it

            if (from == 1){
                val profileType = data?.profileType ?: -1
                val isFollowByMe = data?.isFollowByMe ?: -1
                Log.d("hgcsghxxgghx",profileType.toString())
                Log.d("hgcsghxxgghx",isFollowByMe.toString())
                if (profileType != -1){
                    if (profileType == 1 && isFollowByMe != 1) {
                        binding.tvNoDataFound.visible()
                        binding.nsAllLayout.gone()
                        binding.tvNoDataFound.text = buildString {
                            append("This account is private.")
                        }
                    }else{
                        binding.nsAllLayout.visible()
                        binding.tvNoDataFound.gone()
                    }
                }else{
                    binding.nsAllLayout.visible()
                    binding.tvNoDataFound.gone()
                }
            }else{
                binding.nsAllLayout.visible()
                binding.tvNoDataFound.gone()

            }

            val pos = countryInfoList.indexOfFirst { it.name == data?.country }
            if (pos != -1){
                binding.tvCountryName.text = countryInfoList[pos].name
                binding.ivFlag.setImageResource(countryInfoList[pos].flag!!)
            }else{
                binding.viewLocation.gone()
                binding.ivFlag.gone()
                binding.tvCountryName.gone()
            }

            Log.d("asfssdgdsg",data?.country.toString())
            if (data?.country.isNullOrEmpty()){
                binding.viewLocation.gone()
                binding.ivFlag.gone()
                binding.tvCountryName.gone()
                binding.tvLocation.gone()
                binding.view1.gone()
            }else{
                binding.viewLocation.visible()
                binding.ivFlag.visible()
                binding.tvCountryName.visible()
                binding.tvLocation.visible()
                binding.view1.visible()
            }

             val interestList = data?.userInterests?.map { InterestResponse.Body(
                image = it?.interest?.image,
                name = it?.interest?.name,
                id = it?.interest?.id
            ) }
            if (interestList != null) {
                list.clear()
                list.addAll(interestList)
            }
            val prompt = data?.userAnswers?.map { PromptModel(
                description = it?.description,
                name = it?.question?.title?: "",
                question_id = it?.questionId.toString()
            ) }
            if (prompt != null) {
                promptList.clear()
                promptList.addAll(prompt)
            }
            setInterestAdapter()

            if(promptList.isEmpty()){
                binding.rvPrompts.gone()
                binding.tvChoosePrompt.gone()
            }else{
                binding.rvPrompts.visible()
                binding.tvChoosePrompt.visible()
            }
        })
    }

    private fun setInterestAdapter() {
        adapter = InterestAdapter(requireContext(),list,this,1)
        binding.rvInterest.adapter = adapter

        promptAdapter = PromptAdapter(requireContext(),promptList,1,this)
        binding.rvPrompts.adapter = promptAdapter
    }

    private fun replaceFragment(fragment: Fragment){
        with(requireActivity() as HomeActivity){
            requireActivity().supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, fragment).addToBackStack(null).commit()
        }
    }

    override fun onClick() {

    }

    override fun onCLick(type: Int, view: View,pos:Int) {
    }
}