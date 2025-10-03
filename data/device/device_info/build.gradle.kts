plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.device_info"
}

dependencies {
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.firebase.message)
    implementation(project(":core:core-common"))
}