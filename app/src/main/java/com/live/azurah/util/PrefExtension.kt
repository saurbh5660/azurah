package com.live.azurah.util

import com.live.azurah.controller.MyApplication

fun savePreference(key: String, value: Any) {
    val preference = MyApplication.instance!!.applicationContext.getSharedPreferences("Azurah", 0)
    val editor = preference.edit()

    when (value) {
        is String -> editor.putString(key, value)
        is Boolean -> editor.putBoolean(key, value)
        is Int -> editor.putInt(key, value)
    }
    editor.apply()
}

fun savePreference1(key: String, value: Any) {
    val preference = MyApplication.instance!!.applicationContext.getSharedPreferences("Azurah1", 0)
    val editor = preference.edit()

    when (value) {
        is String -> editor.putString(key, value)
        is Boolean -> editor.putBoolean(key, value)
        is Int -> editor.putInt(key, value)
    }
    editor.apply()
}

inline fun <reified T> getPreference1(key: String, deafultValue: T): T {
    val preference = MyApplication.instance!!.applicationContext.getSharedPreferences("Azurah1", 0)
    return when (T::class) {
        String::class -> preference.getString(key, deafultValue as String) as T
        Boolean::class -> preference.getBoolean(key, deafultValue as Boolean) as T
        Int::class -> preference.getInt(key, deafultValue as Int) as T
        else -> {
            " " as T
        }
    }

}

fun saveRemember(key: String, value: Any) {
    val preference = MyApplication.instance!!.applicationContext.getSharedPreferences("Remember", 0)
    val editor = preference.edit()

    when (value) {
        is String -> editor.putString(key, value)
        is Boolean -> editor.putBoolean(key, value)
        is Int -> editor.putInt(key, value)
    }
    editor.apply()
}

inline fun <reified T> getPreference(key: String, deafultValue: T): T {
    val preference = MyApplication.instance!!.applicationContext.getSharedPreferences("Azurah", 0)
    return when (T::class) {
        String::class -> preference.getString(key, deafultValue as String) as T
        Boolean::class -> preference.getBoolean(key, deafultValue as Boolean) as T
        Int::class -> preference.getInt(key, deafultValue as Int) as T
        else -> {
            " " as T
        }
    }

}

inline fun <reified T> getRemember(key: String, deafultValue: T): T {
    val preference = MyApplication.instance!!.applicationContext.getSharedPreferences("Remember", 0)
    return when (T::class) {
        String::class -> preference.getString(key, deafultValue as String) as T
        Boolean::class -> preference.getBoolean(key, deafultValue as Boolean) as T
        Int::class -> preference.getInt(key, deafultValue as Int) as T
        else -> {
            " " as T
        }
    }

}

fun clearPreferences() {
    val preference = MyApplication.instance!!.applicationContext.getSharedPreferences("Azurah", 0)
    val editor = preference.edit()
    editor.clear()
    editor.apply()
}
