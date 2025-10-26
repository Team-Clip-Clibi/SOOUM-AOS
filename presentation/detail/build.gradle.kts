plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.presentation.detail"
}

dependencies {
    implementation(libs.paging.compose)
    implementation(libs.paging.runtime)
    implementation(project(":core:ui"))
}