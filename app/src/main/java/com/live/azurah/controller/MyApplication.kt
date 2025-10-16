package com.live.azurah.controller

import android.app.Application
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.live.drop.controller.AppLifecycleHandler
import com.live.azurah.socket.SocketManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application(), AppLifecycleHandler.AppLifecycleDelegates {
    private lateinit var cacheEvictor: LeastRecentlyUsedCacheEvictor
    private lateinit var exoplayerDatabaseProvider: StandaloneDatabaseProvider
    private val cacheSize: Long = 900 * 1024 * 1024
    companion object{
        var instance: MyApplication? = null
        lateinit var cache: SimpleCache

    }
    var mSocketManager: SocketManager? = null
    private var lifecycleHandler: AppLifecycleHandler? = null
    override fun onCreate() {
        super.onCreate()

        instance = this
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        lifecycleHandler = AppLifecycleHandler(this)
        registerLifecycleHandler(lifecycleHandler!!)
        setUpForPreCaching()
        mSocketManager = getSocketManager()

    }

    private fun registerLifecycleHandler(lifecycleHandler: AppLifecycleHandler) {
        registerActivityLifecycleCallbacks(lifecycleHandler)
        registerComponentCallbacks(lifecycleHandler)
    }
    fun getSocketManager(): SocketManager? {
        mSocketManager = if (mSocketManager == null) {
            SocketManager()
        } else {
            return mSocketManager
        }
        return mSocketManager
    }
    override fun onAppForegrounded() {
    }
    override fun onAppBackgrounded() {
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    fun setUpForPreCaching() {
        cacheEvictor = LeastRecentlyUsedCacheEvictor(cacheSize)
        exoplayerDatabaseProvider = StandaloneDatabaseProvider(this)
        cache = SimpleCache(cacheDir, cacheEvictor, exoplayerDatabaseProvider)
    }

}