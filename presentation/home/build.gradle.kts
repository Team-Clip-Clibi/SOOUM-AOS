plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.home"
}

dependencies {
    implementation(libs.paging.compose)
    implementation(libs.paging.runtime)
}