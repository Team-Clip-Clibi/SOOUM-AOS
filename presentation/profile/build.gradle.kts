plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.profile"
}

dependencies {
    implementation(libs.paging.compose)
    implementation(libs.paging.runtime)
}