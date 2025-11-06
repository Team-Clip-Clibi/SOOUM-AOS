package com.phew.network.di

import com.phew.domain.token.TokenManger
import com.phew.network.AuthInterceptor
import com.phew.network.BuildConfig
import com.phew.network.TokenAuthenticator
import com.phew.network.retrofit.AppVersionHttp
import com.phew.network.retrofit.CardDetailsInquiryHttp
import com.phew.network.retrofit.FeedHttp
import com.phew.network.retrofit.MembersHttp
import com.phew.network.retrofit.NotifyHttp
import com.phew.network.retrofit.ReportHttp
import com.phew.network.retrofit.SignUpHttp
import com.phew.network.retrofit.SplashHttp
import com.phew.network.retrofit.TokenRefreshHttp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level =
            HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManger: TokenManger): AuthInterceptor =
        AuthInterceptor(tokenManger)

    @Provides
    @Singleton
    fun provideTokenAuthenticator(tokenManger: TokenManger): TokenAuthenticator =
        TokenAuthenticator(tokenManger)

    @Provides
    @Singleton
    @Named("AuthClient")
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .readTimeout(20L, TimeUnit.SECONDS)
        .writeTimeout(20L, TimeUnit.SECONDS)
        .connectTimeout(20L, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("RefreshClient")
    fun provideRefreshOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .readTimeout(20L, TimeUnit.SECONDS)
        .writeTimeout(20L, TimeUnit.SECONDS)
        .connectTimeout(20L, TimeUnit.SECONDS)
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        @Named("AuthClient") okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Singleton
    @Provides
    fun provideTokenRefreshApi(
        @Named("RefreshClient") okHttpClient: OkHttpClient,
        json: Json
    ): TokenRefreshHttp = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(TokenRefreshHttp::class.java)

    @Singleton
    @Provides
    fun provideHttp(retrofit: Retrofit): NotifyHttp = retrofit.create(NotifyHttp::class.java)

    @Singleton
    @Provides
    fun provideFeedHttp(retrofit: Retrofit): FeedHttp = retrofit.create(FeedHttp::class.java)

    @Singleton
    @Provides
    fun provideSplashHttp(retrofit: Retrofit): SplashHttp = retrofit.create(SplashHttp::class.java)

    @Singleton
    @Provides
    fun provideSignUpHttp(retrofit: Retrofit): SignUpHttp = retrofit.create(SignUpHttp::class.java)

    @Singleton
    @Provides
    fun provideReportsHttp(retrofit: Retrofit): ReportHttp = retrofit.create(ReportHttp::class.java)

    @Singleton
    @Provides
    fun provideCardDetailsHttp(retrofit: Retrofit): CardDetailsInquiryHttp =
        retrofit.create(CardDetailsInquiryHttp::class.java)

    @Singleton
    @Provides
    fun provideMembersHttp(retrofit: Retrofit): MembersHttp =
        retrofit.create(MembersHttp::class.java)

    @Singleton
    @Provides
    fun provideAppVersionHttp(retrofit: Retrofit): AppVersionHttp =
        retrofit.create(AppVersionHttp::class.java)
}