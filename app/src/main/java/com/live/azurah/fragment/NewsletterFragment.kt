package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.activity.SetUpProfileActivity
import com.live.azurah.databinding.FragmentNewsletterBinding
import com.live.azurah.model.SignUpResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewsletterFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding : FragmentNewsletterBinding
//    private val loaderDialog by lazy { LoaderDialog(requireContext()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var isNewsletter = "0"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsletterBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListener()

    }

    private fun initListener() {
        with(binding){
            tvYes.setOnClickListener {
                isNewsletter = "1"
               tvYes.background = ContextCompat.getDrawable(requireContext(),R.drawable.full_round_corner_background)
               tvYes.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.light_green1)
               tvYes.setTextColor(ContextCompat.getColorStateList(requireContext(),R.color.white))

                tvNo.background = ContextCompat.getDrawable(requireContext(),R.drawable.full_round_stroke_background)
                tvNo.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.button_grey)
                tvNo.setTextColor(ContextCompat.getColorStateList(requireContext(),R.color.blue))

                btnContinue.isEnabled = true
                btnContinue.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.blue)

            }

            tvNo.setOnClickListener {
                isNewsletter = "0"
                tvYes.background = ContextCompat.getDrawable(requireContext(),R.drawable.full_round_stroke_background)
                tvYes.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.button_grey)
                tvYes.setTextColor(ContextCompat.getColorStateList(requireContext(),R.color.blue))

                tvNo.background = ContextCompat.getDrawable(requireContext(),R.drawable.full_round_corner_background)
                tvNo.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.light_red1)
                tvNo.setTextColor(ContextCompat.getColorStateList(requireContext(),R.color.white))

                btnContinue.isEnabled = true
                btnContinue.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.blue)
            }

            btnContinue.setOnClickListener {
                val map = HashMap<String,String>()
                map["dob"] = isNewsletter
                map["form_step"] = "4"

                viewModel.editProfile(map,requireActivity()).observe(viewLifecycleOwner,this@NewsletterFragment)
            }
            backIcon.setOnClickListener {
                requireActivity().finish()
            }
        }
    }


    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is SignUpResponse -> {
                        replaceFragment(LocationFragment())
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

}