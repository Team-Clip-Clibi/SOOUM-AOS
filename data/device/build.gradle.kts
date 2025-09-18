plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.device"
}

dependencies {
    implementation(libs.security.crypto)
    implementation(libs.google.gson)
    implementation(project(":core:core-common"))
}