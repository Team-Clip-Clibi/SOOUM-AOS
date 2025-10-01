package com.phew.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitFactory {

    private const val CONNECTION_TIMEOUT_SEC = 20L // OkHttp Default: 10 sec
    private const val READ_TIMEOUT_SEC = 20L // OkHttp Default: 10 sec
    private const val WRITE_TIMEOUT_SEC = 20L // OkHttp Default: 10 sec

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        explicitNulls = false
    }

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
        }
        .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
        .connectTimeout(CONNECTION_TIMEOUT_SEC, TimeUnit.SECONDS)
        .build()

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }
}