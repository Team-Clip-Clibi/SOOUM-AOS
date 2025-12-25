plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.sign_up"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(libs.cropper)
}
