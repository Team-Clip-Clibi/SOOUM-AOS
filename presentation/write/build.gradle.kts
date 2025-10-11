plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.write"
}


dependencies {
    implementation(libs.paging.compose)
    implementation(libs.paging.runtime)
}