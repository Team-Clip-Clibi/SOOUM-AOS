plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.reports"
}

dependencies {
    implementation(project(":core:ui"))
}