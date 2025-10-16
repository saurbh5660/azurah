package com.live.azurah.util

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.live.azurah.R
import com.live.azurah.databinding.FullImageLayoutBinding
import com.live.azurah.model.FullImageModel

class ShowImagesDialogFragment : DialogFragment() {

    private var imageList = mutableListOf<FullImageModel>()
    private var initialPosition = 0
    private var originalStatusBarColor = 0
    private var originalNavBarColor = 0
    private var originalSystemUiFlags = 0

    companion object {
        fun newInstance(images: MutableList<FullImageModel>, position: Int): ShowImagesDialogFragment {
            val fragment = ShowImagesDialogFragment()
            fragment.imageList = images
            fragment.initialPosition = position
            return fragment
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        val binding = FullImageLayoutBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            setGravity(Gravity.CENTER)

            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.BLACK
            navigationBarColor = Color.BLACK
            decorView.systemUiVisibility = 0
        }

        var pos = initialPosition

        binding.viewPager.adapter = FullImageViewPagerAdapter(requireActivity(), imageList)
        binding.viewPager.setCurrentItem(pos, false)

        if (imageList.size > 1){
            binding.clCount.visible()
        }else{
            binding.clCount.gone()
        }

        binding.tvCount.text = "${pos + 1}/${imageList.size}"

        binding.ivBackChat.setOnClickListener {
            dismiss()
        }

        binding.ivLeft.setOnClickListener {
            binding.viewPager.setCurrentItem(pos - 1, false)
        }

        binding.ivRight.setOnClickListener {
            binding.viewPager.setCurrentItem(pos + 1, false)
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                pos = position
                binding.tvCount.text = "${position + 1}/${imageList.size}"

                binding.ivLeft.apply {
                    isEnabled = position != 0
                    alpha = if (isEnabled) 1f else 0.5f
                }

                binding.ivRight.apply {
                    isEnabled = position != imageList.lastIndex
                    alpha = if (isEnabled) 1f else 0.5f
                }
            }
        })

        return dialog
    }
}
