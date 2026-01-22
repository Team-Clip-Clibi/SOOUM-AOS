plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.token"
}

dependencies {
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(project(":domain"))
    implementation(project(":core:core-common"))
    implementation(project(":data:network"))
    implementation(project(":data:device:device_info"))
}