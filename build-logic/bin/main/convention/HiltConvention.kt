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

class HiltConvention : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("com.google.dagger.hilt.android")
        pluginManager.apply("com.google.devtools.ksp")
        extensions.getByType<LibraryExtension>().apply {
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
                val fileName : String = properties.getProperty("file_name","")
                val tokenKey = properties.getProperty("tokenKey", "")

                buildConfigField("String", "TOKEN_KEY", tokenKey)
                buildConfigField("String" ,"SOOUM_FILE_NAME" ,fileName)
            }
            buildFeatures.buildConfig = true
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
            packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE.md,LICENSE-notice.md}"
        }
        extensions.getByType<KotlinAndroidProjectExtension>().apply {
            jvmToolchain(21)
        }


        dependencies {
            "implementation"(libs.findLibrary("hilt-android").get())
            "ksp"(libs.findLibrary("hilt-compiler").get())
            "implementation"(libs.findLibrary("junit").get())
            "implementation"(libs.findLibrary("androidx-junit").get())
            "implementation"(libs.findLibrary("androidx-espresso-core").get())
            "testImplementation"(libs.findLibrary("mockk").get())
            "androidTestImplementation"(libs.findLibrary("mockk-android").get())
            "implementation"(libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}