package com.live.azurah.retrofit

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.live.azurah.R

class LoaderDialog private constructor(context: Context) : Dialog(context) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_loader)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
    }

    companion object {
        @Volatile
        private var instance: LoaderDialog? = null

        fun show(context: Context) {
            if (instance?.isShowing != true) {
                instance = LoaderDialog(context)
                instance?.show()
            }
        }

        fun dismiss() {
            instance?.dismiss()
            instance = null
        }
    }
}
