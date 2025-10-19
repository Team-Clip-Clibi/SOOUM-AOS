plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.presentation.write"
}


dependencies {
    implementation(libs.paging.compose)
    implementation(libs.paging.runtime)
    implementation(project(":core:ui"))
    implementation(libs.cropper)
}
