plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.datastore_local"
}

dependencies {
    implementation(libs.google.gson)
    implementation(libs.security.crypto)
    implementation(project(":core:core-common"))
}