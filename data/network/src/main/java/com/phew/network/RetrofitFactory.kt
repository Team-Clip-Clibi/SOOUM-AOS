package com.phew.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitFactory {
    private val json = Json { ignoreUnknownKeys = true }

    fun create(): Retrofit {
        val okHttpClient = createOkHttpClient(createLoggingInterceptor())
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private fun createOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
    ) = OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .addInterceptor {
            val newRequest = it.request().newBuilder()
                .build()
            it.proceed(newRequest)
        }.build()

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }
}