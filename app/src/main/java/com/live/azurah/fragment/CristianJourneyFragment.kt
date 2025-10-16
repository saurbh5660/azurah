package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.activity.SetUpProfileActivity
import com.live.azurah.databinding.FragmentCristianJourneyBinding
import com.live.azurah.databinding.FragmentUploadProfilePhotoBinding
import com.live.azurah.model.SignUpResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CristianJourneyFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentCristianJourneyBinding
//    private val loaderDialog by lazy { LoaderDialog(requireContext()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var journeyName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCristianJourneyBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    private fun initListener() {
        with(binding){
            rbInterested.setOnClickListener {
                journeyName = "Interested/New Christian"
                rbInterested.setImageResource(R.drawable.selected_radio_icon)
                rbNext.setImageResource(R.drawable.unselected_radio_icon)
                rbDeeper.setImageResource(R.drawable.unselected_radio_icon)

                clInterested.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),R.color.blue)
                clNextStep.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),R.color.divider_grey)
                clDelvingDeeper.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),R.color.divider_grey)

                binding.btnNext.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),
                    R.color.blue
                )
                binding.btnNext.isEnabled = true
            }

            rbNext.setOnClickListener {
                journeyName = "Taking the Next Steps"
                rbInterested.setImageResource(R.drawable.unselected_radio_icon)
                rbNext.setImageResource(R.drawable.selected_radio_icon)
                rbDeeper.setImageResource(R.drawable.unselected_radio_icon)
                clInterested.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),R.color.divider_grey)
                clNextStep.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),R.color.blue)
                clDelvingDeeper.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),R.color.divider_grey)

                binding.btnNext.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),
                    R.color.blue
                )
                binding.btnNext.isEnabled = true
            }

            rbDeeper.setOnClickListener {
                journeyName = "Delving Deeper"
                rbInterested.setImageResource(R.drawable.unselected_radio_icon)
                rbNext.setImageResource(R.drawable.unselected_radio_icon)
                rbDeeper.setImageResource(R.drawable.selected_radio_icon)
                clInterested.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),R.color.divider_grey)
                clNextStep.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),R.color.divider_grey)
                clDelvingDeeper.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),R.color.blue)

                binding.btnNext.backgroundTintList = ActivityCompat.getColorStateList(requireContext(),
                    R.color.blue
                )
                binding.btnNext.isEnabled = true

            }

            clInterested.setOnClickListener {
                rbInterested.performClick()
            }

            clNextStep.setOnClickListener {
                rbNext.performClick()
            }

            clDelvingDeeper.setOnClickListener {
                rbDeeper.performClick()
            }

            btnNext.setOnClickListener {
                journeyApi()
            }
            backIcon.setOnClickListener {
                requireActivity().finish()
            }
        }
    }

    private fun journeyApi(){
        if (journeyName.isEmpty()){
            showCustomSnackbar(requireActivity(),binding.root, "Please select christian journey")
            return
        }
        val map = HashMap<String,String>()
        map["christian_journey"] = journeyName
        map["form_step"] = "6"

        viewModel.editProfile(map,requireActivity()).observe(viewLifecycleOwner,this)
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is SignUpResponse -> {
                        val res = value.data.body
                        savePreference("displayNamePreference",res?.displayNamePreference ?: 1)
                        replaceFragment(InterestFragment())
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

}