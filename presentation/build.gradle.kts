plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.presentation"
}
dependencies {
    implementation(project(":domain"))
    implementation(libs.androidx.activity.compose)
}

