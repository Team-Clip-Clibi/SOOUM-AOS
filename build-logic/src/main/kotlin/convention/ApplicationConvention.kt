package convention

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import java.util.Properties

class ApplicationConvention : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("com.google.dagger.hilt.android")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.gms.google-services")
        pluginManager.apply("sooum.android.lint.convention")
        pluginManager.apply("com.google.firebase.crashlytics")
        extensions.getByType<ApplicationExtension>().apply {
            namespace = "com.phew.sooum"
            compileSdk = 36
            val propertiesKeys = Properties()
            val keyPropsFile = rootProject.file("keystore.properties")
            if (keyPropsFile.exists()) {
                keyPropsFile.inputStream().use { propertiesKeys.load(it) }
            }
            defaultConfig.apply {
                applicationId = "com.phew.sooum"
                minSdk = 31
                targetSdk = 36
                versionCode = 16
                versionName = "1.0.8"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                val appLink = propertiesKeys.getProperty("playStore_app_url", "")
                buildConfigField("String", "PLAY_STORE_LINK", appLink)
            }
            signingConfigs {
                create("release") {
                    if (keyPropsFile.exists()) {
                        storeFile = file(propertiesKeys.getProperty("storeFile"))
                        storePassword = propertiesKeys.getProperty("storePassword")
                        keyAlias = propertiesKeys.getProperty("keyAlias")
                        keyPassword = propertiesKeys.getProperty("keyPassword")
                    }
                }
            }
            buildTypes {
                getByName("debug") {
                    isMinifyEnabled = false
                    isDebuggable = true
                    versionNameSuffix = "-debug"
                    manifestPlaceholders["ADMOB_APP_ID"] =
                        propertiesKeys.getProperty("google_adsMob_id_debug", "")
                    val clarityKeyDebug = propertiesKeys.getProperty("clarityKey_dev", "")
                    buildConfigField("String", "CLARITY_PROJECT_ID", clarityKeyDebug)
                }
                getByName("release") {
                    isMinifyEnabled = true
                    isDebuggable = false
                    signingConfig = signingConfigs.getByName("release")
                    manifestPlaceholders["ADMOB_APP_ID"] =
                        propertiesKeys.getProperty("google_adsMob_id_release", "")
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                    val clarityKeyProd = propertiesKeys.getProperty("clarityKey_prod", "")
                    buildConfigField("String", "CLARITY_PROJECT_ID", clarityKeyProd)
                }
            }
            flavorDimensions += "environment"
            productFlavors {
                create("dev") {
                    dimension = "environment"
                    applicationIdSuffix = ".dev"
                    versionNameSuffix = "-dev"
                }
                create("prod") {
                    dimension = "environment"
                }
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
            buildFeatures.apply {
                compose = true
                buildConfig = true
            }
            composeOptions.kotlinCompilerExtensionVersion = "1.5.13"
            packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE.md,LICENSE-notice.md}"
        }
        extensions.getByType<KotlinAndroidProjectExtension>().apply {
            jvmToolchain(21)
        }
        dependencies {
            // androidx compose
            "implementation"(libs.findLibrary("androidx-core-ktx").get())
            "implementation"(libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
            "implementation"(libs.findLibrary("androidx-activity-compose").get())
            "implementation"(platform(libs.findLibrary("androidx-compose-bom").get()))
            "implementation"(libs.findLibrary("androidx-ui").get())
            "implementation"(libs.findLibrary("androidx-ui-graphics").get())
            "implementation"(libs.findLibrary("androidx-ui-tooling-preview").get())
            "implementation"(libs.findLibrary("androidx-material3").get())
            "implementation"(libs.findLibrary("androidx-material3-windowSize").get())
            "debugImplementation"(libs.findLibrary("androidx-ui-tooling").get())
            "debugImplementation"(libs.findLibrary("androidx-ui-test-manifest").get())
            "androidTestImplementation"(platform(libs.findLibrary("androidx-compose-bom").get()))
            "androidTestImplementation"(libs.findLibrary("androidx-ui-test-junit4").get())
            // test
            "testImplementation"(libs.findLibrary("junit").get())
            "androidTestImplementation"(libs.findLibrary("androidx-junit").get())
            "androidTestImplementation"(libs.findLibrary("androidx-espresso-core").get())
            "debugImplementation"(libs.findLibrary("androidx-ui-tooling").get())
            "debugImplementation"(libs.findLibrary("androidx-ui-test-manifest").get())
            // hilt
            "implementation"(libs.findLibrary("hilt-android").get())
            "implementation"(libs.findLibrary("hilt-navigation-compose").get())
            "ksp"(libs.findLibrary("hilt-compiler").get())
            //nav
            "implementation"(libs.findLibrary("compose-nav").get())
            //firebase
            "implementation"(libs.findLibrary("firebase-bom").get())
            "implementation"(libs.findLibrary("firebase-crashlytics").get())
            //Microsoft Clarity
            "implementation"(libs.findLibrary("mircrosoft-clarity").get())
            //Google AdsMob
            "implementation"(libs.findLibrary("google-gms-admob").get())
            //module
            add("implementation", project(":presentation"))
            add("implementation", project(":presentation:splash"))
            add("implementation", project(":presentation:sign-up"))
            add("implementation", project(":presentation:feed"))
            add("implementation", project(":presentation:home"))
            add("implementation", project(":presentation:reports"))
            add("implementation", project(":presentation:settings"))
            add("implementation", project(":domain"))
            add("implementation", project(":data"))
            add("implementation", project(":data:repository"))
            add("implementation", project(":data:network"))
            add("implementation", project(":data:device"))
            add("implementation", project(":data:device:datastore_local"))
            add("implementation", project(":data:device:location_provider"))
            add("implementation", project(":data:device:device_info"))
            add("implementation", project(":data:device:device_haptic"))
            add("implementation", project(":data:token"))
            add("implementation", project(":data:paging"))
            add("implementation", project(":data:analytics"))
            add("implementation", project(":core:core-design"))
            add("implementation", project(":core:core-common"))
            add("implementation", project(":core:ui"))
            add("implementation", project(":presentation:write"))
            add("implementation", project(":presentation:detail"))
            add("implementation", project(":presentation:profile"))
            add("implementation", project(":presentation:tag"))
        }
    }
}
