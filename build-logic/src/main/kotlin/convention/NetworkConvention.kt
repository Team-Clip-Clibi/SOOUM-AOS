package convention

import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import java.util.Properties

class NetworkConvention : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")
        pluginManager.apply("sooum.android.lint.convention")
        extensions.getByType<LibraryExtension>().apply {
            namespace = "com.phew.network"
            compileSdk = 36
            defaultConfig {
                minSdk = 31
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                consumerProguardFiles("consumer-rules.pro")
                val properties = Properties()
                val localPropsFile = rootProject.file("local.properties")
                if (localPropsFile.exists()) {
                    localPropsFile.inputStream().use { properties.load(it) }
                }
                val baseUrl: String = properties.getProperty("base_url", "")
                val apiUrl: String = properties.getProperty("api_url", "")
                val apiUrlType: String = properties.getProperty("api_url_type", "")
                val apiUrlQuery: String = properties.getProperty("api_url_version", "")
                val rsaKey: String = properties.getProperty("key_url", "")
                val loginUrl: String = properties.getProperty("api_url_login", "")
                val checkSignUp: String = properties.getProperty("api_url_check_sign_up", "")
                val updateFcm: String = properties.getProperty("api_url_update_fcm", "")
                val signUp: String = properties.getProperty("api_url_sign_up", "")
                val nickNameGenerator: String =
                    properties.getProperty("api_url_nickname_generate", "")
                val nickNameCheck: String = properties.getProperty("api_url_check_nick_name", "")
                val upLoadImage: String = properties.getProperty("api_url_upload_image", "")
                val refreshToken: String = properties.getProperty("api_url_refresh_token", "")
                val noticeUrl: String = properties.getProperty("api_url_notice", "")
                val notificationUnRead: String =
                    properties.getProperty("api_url_notification_un_read", "")
                val notificationRead: String =
                    properties.getProperty("api_url_notification_read", "")

                val popularUrl: String = properties.getProperty("api_url_card_feed_popular", "")
                val latestUrl: String = properties.getProperty("api_url_card_feed_latest", "")
                val relatedTag: String = properties.getProperty("api_url_tag_related", "")
                val tags: String = properties.getProperty("api_url_tags", "")
                val tagCards: String = properties.getProperty("api_url_tag_cards", "")
                val tagRank: String = properties.getProperty("api_url_tag_rank", "")
                val tagFavorite: String = properties.getProperty("api_url_tag_favorite", "")
                val cardBackgroundImageDefault =
                    properties.getProperty("api_url_card_background_image_default", "")
                val cardBackgroundUpload: String =
                    properties.getProperty("api_url_upload_card_background", "")
                val uploadCard: String = properties.getProperty("api_url_upload_card", "")
                val banedChecked: String = properties.getProperty("api_url_upload_baned", "")
                val answerCard: String = properties.getProperty("api_url_upload_card_answer", "")
                val cardDistance: String = properties.getProperty("api_url_card_distance", "")
                val backgroundImageCheck: String =
                    properties.getProperty("api_url_background_image_check", "")
                val cardLike: String = properties.getProperty("api_url_card_like", "")
                val cardDetail: String = properties.getProperty("api_url_card_detail", "")
                val cardDelete: String = properties.getProperty("api_url_card_delete", "")
                val cardComment: String = properties.getProperty("api_url_card_comment", "")
                val cardCommentMore: String =
                    properties.getProperty("api_url_card_comment_more", "")
                val cardReports: String = properties.getProperty("api_url_reports_card", "")
                val cardBlock: String = properties.getProperty("api_url_block_member", "")
                val cardUnblock: String = properties.getProperty("api_url_unblock_member", "")
                val blocks: String = properties.getProperty("api_url_blocks", "")
                val blocksPaging: String = properties.getProperty("api_url_blocks_paging", "")
                val myProfile: String = properties.getProperty("api_url_my_profile", "")
                val myProfileCommentCard: String =
                    properties.getProperty("api_url_profile_comment_card", "")
                val myProfileCard: String = properties.getProperty("api_url_profile_card", "")
                val following: String = properties.getProperty("api_url_following", "")
                val followingNext: String = properties.getProperty("api_url_following_next", "")
                val follower: String = properties.getProperty("api_url_follower", "")
                val followerNext: String = properties.getProperty("api_url_follower_next", "")
                val otherProfile: String = properties.getProperty("api_url_other_profile", "")
                val follow : String = properties.getProperty("api_url_follow" , "")
                val unFollow : String = properties.getProperty("api_url_un_follow","")
                val updateProfile : String = properties.getProperty("api_url_update_profile" ,"")
                val activityRestriction: String = properties.getProperty("api_url_activity_restriction", "")
                val transferCode: String = properties.getProperty("api_url_transfer_code", "")
                val refreshTransferCode: String = properties.getProperty("api_url_refresh_transfer_code", "")
                val transferAccount: String = properties.getProperty("api_url_transfer_account", "")
                val withdrawalAccount: String = properties.getProperty("api_url_withdrawal_account", "")
                val rejoinableDate: String = properties.getProperty("api_url_rejoinable_date", "")

                buildConfigField("String", "BASE_URL", baseUrl)
                buildConfigField("String", "API_URL", apiUrl)
                buildConfigField("String", "API_URL_TYPE", apiUrlType)
                buildConfigField("String", "API_URL_QUERY", apiUrlQuery)
                buildConfigField("String", "API_SECURITY_KEY", rsaKey)
                buildConfigField("String", "API_URL_LOGIN", loginUrl)
                buildConfigField("String", "API_URL_CHECK_SIGN_UP", checkSignUp)
                buildConfigField("String", "API_URL_FCM_UPDATE", updateFcm)
                buildConfigField("String", "API_URL_SIGN_UP", signUp)
                buildConfigField("String", "API_URL_NICKNAME_GENERATOR", nickNameGenerator)
                buildConfigField("String", "API_URL_CHECK_NICKNAME_AVAILABLE", nickNameCheck)
                buildConfigField("String", "API_URL_UPLOAD_IMAGE", upLoadImage)
                buildConfigField("String", "API_URL_REFRESH_TOKEN", refreshToken)
                buildConfigField("String", "API_URL_NOTICE", noticeUrl)
                buildConfigField("String", "API_URL_NOTIFICATION_UN_READ", notificationUnRead)
                buildConfigField("String", "API_URL_NOTIFICATION_READ", notificationRead)
                buildConfigField("String", "API_URL_CARD_FEED_POPULAR", popularUrl)
                buildConfigField("String", "API_URL_CARD_FEED_LATEST", latestUrl)
                buildConfigField("String", "API_URL_CARD_FEED_DISTANCE", cardDistance)
                buildConfigField("String", "API_URL_TAG_RELATED", relatedTag)
                buildConfigField("String", "API_URL_TAGS", tags)
                buildConfigField("String", "API_URL_TAG_CARDS", tagCards)
                buildConfigField("String", "API_URL_TAG_RANK", tagRank)
                buildConfigField("String", "API_URL_TAG_FAVORITE", tagFavorite)
                buildConfigField("String", "API_URL_CARD_IMAGE_DEFAULT", cardBackgroundImageDefault)
                buildConfigField("String", "API_URL_UPLOAD_CARD_IMAGE", cardBackgroundUpload)
                buildConfigField("String", "API_URL_UPLOAD_CARD", uploadCard)
                buildConfigField("String", "API_URL_CHECKED_BANED", banedChecked)
                buildConfigField("String", "API_URL_UPLOAD_CARD_ANSWER", answerCard)
                buildConfigField(
                    "String",
                    "API_URL_UPLOAD_BACKGROUND_IMAGE_CHECK",
                    backgroundImageCheck
                )
                buildConfigField("String", "API_URL_REPORTS_CARDS", cardReports)
                buildConfigField("String", "API_URL_CARD_LIKE", cardLike)
                buildConfigField("String", "API_URL_CARD_DETAIL", cardDetail)
                buildConfigField("String", "API_URL_CARD_DELETE", cardDelete)
                buildConfigField("String", "API_URL_CARD_COMMENT", cardComment)
                buildConfigField("String", "API_URL_CARD_COMMENT_MORE", cardCommentMore)
                buildConfigField("String", "API_URL_BLOCK_MEMBER", cardBlock)
                buildConfigField("String", "API_URL_UNBLOCK_MEMBER", cardUnblock)
                buildConfigField("String", "API_URL_BLOCKS", blocks)
                buildConfigField("String", "API_URL_BLOCKS_PAGING", blocksPaging)
                buildConfigField("String", "API_URL_MY_PROFILE", myProfile)
                buildConfigField("String", "API_URL_MY_PROFILE_COMMENT_CARD", myProfileCommentCard)
                buildConfigField("String", "API_URL_MY_PROFILE_CARD", myProfileCard)
                buildConfigField("String", "API_URL_FOLLOWING", following)
                buildConfigField("String", "API_URL_FOLLOWING_NEXT", followingNext)
                buildConfigField("String", "API_URL_FOLLOWER", follower)
                buildConfigField("String", "API_URL_FOLLOWER_NEXT", followerNext)
                buildConfigField("String", "API_URL_OTHER_PROFILE", otherProfile)
                buildConfigField("String", "API_URL_FOLLOW_USER", follow)
                buildConfigField("String", "API_URL_UN_FOLLOW_USER", unFollow)
                buildConfigField("String", "API_URL_UPDATE_PROFILE", updateProfile)
                buildConfigField("String", "API_URL_ACTIVITY_RESTRICTION", activityRestriction)
                buildConfigField("String", "API_URL_TRANSFER_CODE", transferCode)
                buildConfigField("String", "API_URL_REFRESH_TRANSFER_CODE", refreshTransferCode)
                buildConfigField("String", "API_URL_TRANSFER_ACCOUNT", transferAccount)
                buildConfigField("String", "API_URL_WITHDRAWAL_ACCOUNT", withdrawalAccount)
                buildConfigField("String", "API_URL_REJOINABLE_DATE", rejoinableDate)
            }
            buildFeatures.buildConfig = true
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
        }
        extensions.getByType<KotlinAndroidProjectExtension>().apply {
            jvmToolchain(21)
        }
        dependencies {
            "implementation"(libs.findLibrary("hilt-android").get())
            "ksp"(libs.findLibrary("hilt-compiler").get())
            "implementation"(libs.findLibrary("squareup-retrofit2-retrofit").get())
            "implementation"(
                libs.findLibrary("squareup-retrofit2-converter-kotlinx-serialization").get()
            )
            "implementation"(libs.findLibrary("squareup-okhttp3-logging-interceptor").get())
            "implementation"(libs.findLibrary("jetbrains-kotlinx-serialization-json").get())
            "implementation"(libs.findLibrary("google-gson").get())
        }
    }
}