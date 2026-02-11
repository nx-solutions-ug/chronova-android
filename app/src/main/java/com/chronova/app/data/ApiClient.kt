package com.chronova.app.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val DEFAULT_BASE_URL = "https://app.chronova.dev/"
    private var currentBaseUrl = DEFAULT_BASE_URL
    private var currentRetrofit: Retrofit? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(baseUrl))
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun ensureTrailingSlash(url: String): String {
        return if (url.endsWith("/")) url else "$url/"
    }

    fun updateBaseUrl(baseUrl: String) {
        val normalizedUrl = ensureTrailingSlash(baseUrl)
        if (normalizedUrl != currentBaseUrl) {
            currentBaseUrl = normalizedUrl
            currentRetrofit = null // Force recreation
        }
    }

    val apiService: ChronovaApiService
        get() {
            if (currentRetrofit == null) {
                currentRetrofit = createRetrofit(currentBaseUrl)
            }
            return currentRetrofit!!.create(ChronovaApiService::class.java)
        }
}
