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

class DomainConvention : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")
        extensions.getByType<LibraryExtension>().apply {
            namespace = "com.phew.domain"
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
                val appType: String = properties.getProperty("appType", "")
                val tokenKey: String = properties.getProperty("tokenKey", "")
                val transformation: String = properties.getProperty("transformation", "")
                val fcmTokenKey: String = properties.getProperty("fcm_token_key", "")
                val notifyKey: String = properties.getProperty("notify_key", "")
                val userInfoKey: String = properties.getProperty("userInfo_key", "")
                val encryptAlgorithm: String = properties.getProperty("encrypt_algorithm", "")
                val decodeAlgorithm: String = properties.getProperty("decode_algorithm", "")

                buildConfigField("String", "APP_TYPE", appType)
                buildConfigField("String", "TOKEN_KEY", tokenKey)
                buildConfigField("String", "TRANSFORMATION", transformation)
                buildConfigField("String", "FCM_TOKEN_KEY", fcmTokenKey)
                buildConfigField("String", "NOTIFY_KEY", notifyKey)
                buildConfigField("String", "USER_INFO_KEY", userInfoKey)
                buildConfigField("String", "ENCRYPT_ALGORITHM", encryptAlgorithm)
                buildConfigField("String", "DECODE_ALGORITHM", decodeAlgorithm)

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
            "implementation"(libs.findLibrary("okhttp3").get())
            //project
            "implementation"(project(":core:core-common"))
            // test
            "testImplementation"(libs.findLibrary("junit").get())
            "testImplementation"(libs.findLibrary("mockk").get())
            "testImplementation"(libs.findLibrary("truth").get())
            "testImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
            "androidTestImplementation"(libs.findLibrary("androidx-junit").get())
            "androidTestImplementation"(libs.findLibrary("androidx-espresso-core").get())

        }
    }
}