package com.phew.network.di

import com.phew.core_common.IsDebug
import com.phew.domain.interceptor.GlobalEventBus
import com.phew.domain.interceptor.InterceptorManger
import com.phew.network.AuthInterceptor
import com.phew.network.BuildConfig
import com.phew.network.TeapotInterceptor
import com.phew.network.TokenAuthenticator
import com.phew.network.retrofit.AppVersionHttp
import com.phew.network.retrofit.BlockHttp
import com.phew.network.retrofit.CardDetailsInquiryHttp
import com.phew.network.retrofit.FeedHttp
import com.phew.network.retrofit.MembersHttp
import com.phew.network.retrofit.NotifyHttp
import com.phew.network.retrofit.ProfileHttp
import com.phew.network.retrofit.ReportHttp
import com.phew.network.retrofit.SignUpHttp
import com.phew.network.retrofit.SplashHttp
import com.phew.network.retrofit.TagHttp
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
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(interceptorManger: InterceptorManger): AuthInterceptor =
        AuthInterceptor(interceptorManger)

    @Provides
    @Singleton
    fun provideTokenAuthenticator(interceptorManger: InterceptorManger): TokenAuthenticator =
        TokenAuthenticator(interceptorManger)

    @Provides
    @Singleton
    fun provideGlobalEventBus(): GlobalEventBus = GlobalEventBus()

    @Provides
    @Singleton
    @Named("AuthClient")
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        teapotInterceptor: TeapotInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(teapotInterceptor)
        .authenticator(tokenAuthenticator)
        .readTimeout(20L, TimeUnit.SECONDS)
        .writeTimeout(20L, TimeUnit.SECONDS)
        .connectTimeout(20L, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("RefreshClient")
    fun provideRefreshOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        @IsDebug isDebug: Boolean,
    ): OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (isDebug) {
                addInterceptor(loggingInterceptor)
            }
        }
        .readTimeout(20L, TimeUnit.SECONDS)
        .writeTimeout(20L, TimeUnit.SECONDS)
        .connectTimeout(20L, TimeUnit.SECONDS)
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        @Named("AuthClient") okHttpClient: OkHttpClient,
        @IsDebug isDebug: Boolean,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(if (isDebug) BuildConfig.BASE_URL_DEBUG else BuildConfig.BASE_URL_PROD)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Singleton
    @Provides
    fun provideTokenRefreshApi(
        @Named("RefreshClient") okHttpClient: OkHttpClient,
        @IsDebug isDebug: Boolean,
        json: Json,
    ): TokenRefreshHttp = Retrofit.Builder()
        .baseUrl(if (isDebug) BuildConfig.BASE_URL_DEBUG else BuildConfig.BASE_URL_PROD)
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
    fun provideProfileHttp(retrofit: Retrofit): ProfileHttp =
        retrofit.create(ProfileHttp::class.java)

    @Singleton
    @Provides
    fun provideMembersHttp(retrofit: Retrofit): MembersHttp =
        retrofit.create(MembersHttp::class.java)

    @Singleton
    @Provides
    fun provideAppVersionHttp(retrofit: Retrofit): AppVersionHttp =
        retrofit.create(AppVersionHttp::class.java)

    @Singleton
    @Provides
    fun provideBlockHttp(retrofit: Retrofit): BlockHttp =
        retrofit.create(BlockHttp::class.java)

    @Singleton
    @Provides
    fun provideTagHttp(retrofit: Retrofit): TagHttp =
        retrofit.create(TagHttp::class.java)
}