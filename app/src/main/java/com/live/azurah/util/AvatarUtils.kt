package com.live.azurah.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.live.azurah.retrofit.ApiConstants

object AvatarUtils {

    // Curated vibrant color palette matching the reference design
    private val avatarColorPalette = intArrayOf(
        Color.parseColor("#FF2D55"), // Vibrant Red / Pink (like Loki in screenshot)
        Color.parseColor("#9B51E0"), // Vibrant Purple (like Janel in screenshot)
        Color.parseColor("#27AE60"), // Vibrant Green (like Naveen in screenshot)
        Color.parseColor("#2563EB"), // Royal Blue
        Color.parseColor("#F97316"), // Vibrant Orange
        Color.parseColor("#EC4899"), // Vibrant Pink
        Color.parseColor("#06B6D4"), // Cyan
        Color.parseColor("#8B5CF6"), // Violet
        Color.parseColor("#F59E0B"), // Amber / Gold
        Color.parseColor("#10B981")  // Emerald Green
    )

    fun getAvatarColor(name: String): Int {
        val letter = name.trim().firstOrNull { it.isLetter() }?.uppercaseChar() ?: 'A'
        return when (letter) {
            'L', 'R', 'A', 'E', 'M' -> avatarColorPalette[0] // Red / Pink
            'J', 'P', 'B', 'F', 'T' -> avatarColorPalette[1] // Purple
            'N', 'G', 'C', 'K', 'V' -> avatarColorPalette[2] // Green
            'D', 'H', 'Q', 'W' -> avatarColorPalette[3]      // Blue
            'S', 'I', 'O', 'Y', 'Z' -> avatarColorPalette[4] // Orange
            else -> avatarColorPalette[kotlin.math.abs(letter.code) % avatarColorPalette.size]
        }
    }

    fun getFirstLetter(name: String?): String {
        return name?.trim()?.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString()
            ?: "A"
    }

    fun setupAvatar(
        ivProfile: ImageView,
        tvInitials: TextView,
        imageUrl: String?,
        displayName: String?
    ) {
        val name = displayName ?: ""
        val initial = getFirstLetter(name)
        val color = getAvatarColor(name)

        tvInitials.text = initial
        tvInitials.backgroundTintList = ColorStateList.valueOf(color)

        val cleanUrl = imageUrl?.trim()
        if (cleanUrl.isNullOrEmpty() || cleanUrl == "null" || cleanUrl.endsWith("/null")) {
            ivProfile.visibility = View.GONE
            tvInitials.visibility = View.VISIBLE
            return
        }

        val fullUrl = when {
            cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://") -> cleanUrl
            cleanUrl.startsWith("/") -> ApiConstants.IMAGE_BASE_URL + cleanUrl
            else -> ApiConstants.IMAGE_BASE_URL + "/" + cleanUrl
        }

        tvInitials.visibility = View.VISIBLE
        ivProfile.visibility = View.VISIBLE

        Glide.with(ivProfile.context)
            .load(fullUrl)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    ivProfile.visibility = View.GONE
                    tvInitials.visibility = View.VISIBLE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    ivProfile.visibility = View.VISIBLE
                    tvInitials.visibility = View.GONE
                    return false
                }
            })
            .into(ivProfile)
    }
}
