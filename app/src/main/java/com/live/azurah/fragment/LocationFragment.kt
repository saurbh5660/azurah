package com.live.azurah.fragment

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.activity.SetUpProfileActivity
import com.live.azurah.adapter.CountryPickerAdapter
import com.live.azurah.databinding.CountryPickerDialogBinding
import com.live.azurah.databinding.FragmentLocationBinding
import com.live.azurah.model.CountryModel
import com.live.azurah.model.SignUpResponse
import com.live.azurah.model.countryInfoList
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentLocationBinding
    private var countryList = ArrayList<CountryModel>()
    private var adapter:CountryPickerAdapter? = null
//    private val loaderDialog by lazy { LoaderDialog(requireContext()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var countryCode = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countryList.clear()
        countryList.addAll(countryInfoList)
        initListener()
    }

    private fun initListener() {
        with(binding){
            btnNext.setOnClickListener {
                locationApi(binding.etCoutries.text.toString().trim(),countryCode)
            }
            tvSkip.setOnClickListener {
                locationApi("","")
            }
            backIcon.setOnClickListener {
                requireActivity().finish()
            }
          /*  countryPicker.changeFlagProvider(
                CPFlagImageProvider(
                    FlagPack1.alpha2ToFlag,
                    FlagPack1.missingFlagPlaceHolder
                )
            )
*/
            clCountrySelection.setOnClickListener {
                showCountryDialog()
              /*  requireActivity().launchCountryPickerDialog(showFullScreen = true){ cpCountry: CPCountry? ->
                    when(val flagProvider = countryPicker.cpViewHelper.cpRowConfig.cpFlagProvider){
                        is CPFlagImageProvider->{
                            tvCountry.text = cpCountry?.name
                            etCoutries.setText(cpCountry?.name)
                            ivFlag.visibility = View.VISIBLE
                            ivFlag.text = cpCountry!!.flagEmoji
                        }
                    }
                }*/
            }
        }
    }

    private fun locationApi(country:String,countryCode:String){
        val map = HashMap<String,String>()
        map["country"] = country
        map["country_code"] = countryCode
        map["form_step"] = "5"

        viewModel.editProfile(map,requireActivity()).observe(viewLifecycleOwner,this)
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is SignUpResponse -> {
                        replaceFragment(CristianJourneyFragment())
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
            requireActivity().supportFragmentManager.beginTransaction().replace(binding.frameContainer.id, fragment).commit()
        }
    }

    private fun showCountryDialog(){
        val customDialog = Dialog(requireContext())
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val countryBinding = CountryPickerDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(countryBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.white)

        countryBinding.etSearch.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()){
                    countryBinding.ivCross.visibility = View.VISIBLE
                    val newList = countryList.filter { it.name.contains(s.toString(), ignoreCase = true) } as ArrayList
                    adapter = CountryPickerAdapter(requireContext(),newList)
                    countryBinding.rvCountry.adapter = adapter
                    if (newList.isNotEmpty()){
                        countryBinding.tvNoResult.visibility = View.GONE
                        countryBinding.rvCountry.visibility = View.VISIBLE
                    }else{
                        countryBinding.tvNoResult.visibility = View.VISIBLE
                        countryBinding.rvCountry.visibility = View.GONE
                    }
                }else{
                    countryBinding.ivCross.visibility = View.GONE
                    countryBinding.tvNoResult.visibility = View.GONE
                    countryBinding.rvCountry.visibility = View.VISIBLE
                    adapter = CountryPickerAdapter(requireContext(),countryList)
                    countryBinding.rvCountry.adapter = adapter
                }
                adapter?.listener = {model, pos ->
                    countryCode = model.countryCode
                    binding.etCoutries.setText(model.name)
                    binding.tvCountry.text = model.name
                    binding.viewLocation.visibility = View.VISIBLE
                    binding.ivFlag.visibility = View.VISIBLE
                    binding.ivFlag.setImageResource(model.flag!!)
                    Log.d("gfdgdfdf","meeeeeeeeee")
                    binding.btnNext.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),
                        R.color.blue
                    )
                    binding.btnNext.isEnabled = true
                    customDialog.dismiss()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        countryBinding.ivCross.setOnClickListener {
            countryBinding.etSearch.setText("")
        }

        adapter = CountryPickerAdapter(requireContext(),countryList)
        countryBinding.rvCountry.adapter = adapter
        adapter?.listener = {model, pos ->
            countryCode = model.countryCode
            binding.etCoutries.setText(model.name)
            binding.tvCountry.text = model.name
            binding.ivFlag.visibility = View.VISIBLE
            binding.viewLocation.visibility = View.VISIBLE
            binding.ivFlag.setImageResource(model.flag!!)
            Log.d("gfdgdfdf","dsdfgfdgdfg")
            binding.btnNext.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),
                R.color.blue
            )
            binding.btnNext.isEnabled = true
            customDialog.dismiss()
        }
        countryBinding.backIcon.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()

    }
}
