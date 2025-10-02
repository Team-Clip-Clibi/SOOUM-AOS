plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.repository"
}

dependencies {
    implementation(project(":data:device:datastore_local"))
    implementation(project(":data:device:device_info"))
    implementation(project(":data:device:location_provider"))
    implementation(project(":data:network"))
    implementation(project(":domain"))
    implementation(project(":core:core-common"))
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(libs.okhttp3)
}