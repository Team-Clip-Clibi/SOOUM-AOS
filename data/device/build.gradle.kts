plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.device"
}

dependencies {
    implementation(libs.security.crypto)
    implementation(libs.google.gson)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.firebase.message)
    implementation(libs.google.play.services.location)
    implementation(project(":core:core-common"))
}