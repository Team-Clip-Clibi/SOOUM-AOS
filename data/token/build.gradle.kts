import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("sooum.android.hilt")
}
val properties = Properties()
val localPropsFile = rootProject.file("local.properties")

if (localPropsFile.exists()) {
    localPropsFile.inputStream().use { properties.load(it) }
}
android {
    namespace = "com.phew.token"

    defaultConfig {
        val tokenKey = properties.getProperty("tokenKey", "")
        buildConfigField("String", "TOKEN_KEY", tokenKey)
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:core-common"))
}