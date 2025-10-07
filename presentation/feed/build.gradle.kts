plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.feed"
}

dependencies {
    implementation(libs.paging.compose)
    implementation(libs.paging.runtime)
}