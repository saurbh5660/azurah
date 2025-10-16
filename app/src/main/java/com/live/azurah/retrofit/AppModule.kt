package com.live.azurah.retrofit

import android.content.Context
import android.media.AudioManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providerExecutor(): Executor {
        return Executors.newSingleThreadExecutor()
    }

    @Provides
    @Singleton
    fun providerInterceptor(@ApplicationContext context: Context): AppInterceptor {
        return AppInterceptor(context)
    }

    @Provides
    @Singleton
    internal fun provideOkHttpClient(interceptor: AppInterceptor): OkHttpClient {

        val okHttpClient = OkHttpClient.Builder()
        with(okHttpClient) {
            connectTimeout(50000, TimeUnit.MILLISECONDS)
            readTimeout(50000, TimeUnit.MILLISECONDS)
            writeTimeout(50000, TimeUnit.MILLISECONDS)
            val logInterceptor = HttpLoggingInterceptor()
//            logInterceptor.level = (HttpLoggingInterceptor.Level.BASIC)
            logInterceptor.level = (HttpLoggingInterceptor.Level.BODY)
            addInterceptor(interceptor)
            addInterceptor(logInterceptor)
            return build()
        }
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): ApiServiceInterface {
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        return retrofit.create(ApiServiceInterface::class.java)
    }

    @Singleton
    @Provides
    fun provideAudioManager(context: Context): AudioManager {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

}