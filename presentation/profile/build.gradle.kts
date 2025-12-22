plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.profile"
}

dependencies {
    implementation(libs.paging.compose)
    implementation(libs.paging.runtime)
    implementation(libs.cropper)
    implementation(project(":presentation:settings"))
}