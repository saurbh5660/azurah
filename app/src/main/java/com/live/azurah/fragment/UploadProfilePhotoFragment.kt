package com.live.azurah.fragment

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.live.azurah.R
import com.live.azurah.activity.SetUpProfileActivity
import com.live.azurah.databinding.FragmentUploadProfilePhotoBinding
import com.live.azurah.model.CheckUsernameResponse
import com.live.azurah.model.FileUploadResponse
import com.live.azurah.model.SignUpResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.ImagePickerFragment
import com.live.azurah.util.prepareFilePart
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class UploadProfilePhotoFragment : ImagePickerFragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentUploadProfilePhotoBinding
//    private val loaderDialog by lazy { LoaderDialog(requireContext()) }
    private val viewModel by viewModels<CommonViewModel>()
    var image = ""
    private var showDialog = false


    override fun selectedImage(imagePath: String?, code: Int) {
        imagePath?.let {
            image = it
            Glide.with(this).load(imagePath).into(binding.ivProfile)
            binding.tvprofile.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadProfilePhotoBinding.inflate(inflater,container,false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initListener() {
        with(binding){

            btnNext.setOnClickListener {
                if (image.isEmpty()){
                    showCustomSnackbar(requireActivity(),binding.root, "Please select profile image")
                    return@setOnClickListener
                }

                val map = HashMap<String,RequestBody>()
                map["type"] = "image".toRequestBody("text/plain".toMediaTypeOrNull())
                map["folder"] = "users".toRequestBody("text/plain".toMediaTypeOrNull())

                val list = ArrayList<MultipartBody.Part>()
                list.add(prepareFilePart("image", File(image)))
                viewModel.fileUpload(map,list,requireActivity()).observe(viewLifecycleOwner,this@UploadProfilePhotoFragment)
            }
            tvSkip.setOnClickListener {
                showDialog = true
                uploadProfileImage("")
            }
            backIcon.setOnClickListener {
                requireActivity().finish()
            }
            ivProfile.setOnClickListener {
                askStorageManagerPermission(requireActivity(),1,false,this@UploadProfilePhotoFragment,1)
            }
        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                when (value.data) {
                    is FileUploadResponse -> {
                        val jsonString = Gson().toJson(value.data.body)
                        showDialog = false
                        uploadProfileImage(jsonString)
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

    private fun uploadProfileImage(jsonString:String) {
        val map = HashMap<String,String>()
        if (jsonString.isNotEmpty()){
            map["profile_image"] = jsonString
        }
        map["form_step"] = "3"
        viewModel.editProfile(map,requireActivity()).observe(viewLifecycleOwner) { it ->
            when (it.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (it.data) {
                        is SignUpResponse -> {
                            replaceFragment(BirthdayFragment())
                        }
                    }
                }

                Status.LOADING -> {
                    if (showDialog){
                        LoaderDialog.show(requireActivity())
                    }
                }

                Status.ERROR -> {
                   LoaderDialog.dismiss()
                    showCustomSnackbar(
                        requireActivity(),
                        binding.root,
                        it.message.toString()
                    )

                }
            }

        }
    }

    private fun replaceFragment(fragment: Fragment){
        with(requireActivity() as SetUpProfileActivity){
            requireActivity().supportFragmentManager.beginTransaction().replace(binding.frameContainer.id, fragment).commit()
        }
    }

}