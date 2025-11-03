plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.presentation.settings"
}

dependencies {
    implementation(project(":core:ui"))
}