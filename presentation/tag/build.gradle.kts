plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.presentation.tag"
}

dependencies {
    implementation(libs.paging.compose)
    implementation(libs.paging.runtime)
    implementation(project(":core:ui"))
    implementation(libs.cropper)
}