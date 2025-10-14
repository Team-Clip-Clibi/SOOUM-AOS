plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.presentation.feed"
}

dependencies {
    implementation(libs.paging.compose)
    implementation(libs.paging.runtime)
}