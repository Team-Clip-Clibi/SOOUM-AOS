plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.device"
}

dependencies {
    implementation(libs.security.crypto)
}