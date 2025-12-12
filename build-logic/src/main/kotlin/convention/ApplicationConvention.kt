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
            val properties = Properties()
            val localPropsFile = rootProject.file("keystore.properties")
            if (localPropsFile.exists()) {
                localPropsFile.inputStream().use { properties.load(it) }
            }
            defaultConfig.apply {
                applicationId = "com.phew.sooum"
                minSdk = 31
                targetSdk = 36
                versionCode = 1
                versionName = "1.0.13"
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            signingConfigs {
                create("release") {
                    if (localPropsFile.exists()) {
                        storeFile = file(properties.getProperty("storeFile"))
                        storePassword = properties.getProperty("storePassword")
                        keyAlias = properties.getProperty("keyAlias")
                        keyPassword = properties.getProperty("keyPassword")
                    }
                }
            }
            buildTypes {
                getByName("debug") {
                    isMinifyEnabled = false
                    isDebuggable = true
                    versionNameSuffix = "-debug"
                    buildConfigField("String", "CLARITY_PROJECT_ID", "\"\"")
                }
                getByName("release") {
                    isMinifyEnabled = true
                    isDebuggable = false
                    signingConfig = signingConfigs.getByName("release")
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                    val clarityKey = properties.getProperty("clarityKey", "")
                    buildConfigField("String", "CLARITY_PROJECT_ID", clarityKey)
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
                    versionNameSuffix = "-prod"
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
            //module
            add("implementation", project(":presentation"))
            add("implementation", project(":presentation:splash"))
            add("implementation", project(":presentation:sign-up"))
            add("implementation", project(":presentation:feed"))
            add("implementation", project(":presentation:home"))
            add("implementation", project(":presentation:reports"))
            add("implementation", project(":domain"))
            add("implementation", project(":data"))
            add("implementation", project(":data:repository"))
            add("implementation", project(":data:network"))
            add("implementation", project(":data:device"))
            add("implementation", project(":data:device:datastore_local"))
            add("implementation", project(":data:device:location_provider"))
            add("implementation", project(":data:device:device_info"))
            add("implementation", project(":data:token"))
            add("implementation", project(":data:paging"))
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
