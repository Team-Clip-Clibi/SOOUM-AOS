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

                buildConfigField("String", "BASE_URL", baseUrl)
                buildConfigField("String", "API_URL", apiUrl)
                buildConfigField("String", "API_URL_TYPE", apiUrlType)
                buildConfigField("String", "API_URL_QUERY", apiUrlQuery)
                buildConfigField("String", "API_SECURITY_KEY", rsaKey)
                buildConfigField("String", "API_URL_LOGIN", loginUrl)
                buildConfigField("String", "API_URL_CHECK_SIGN_UP", checkSignUp)
                buildConfigField("String", "API_URL_FCM_UPDATE", updateFcm)
                buildConfigField("String", "API_URL_SIGN_UP", signUp)
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