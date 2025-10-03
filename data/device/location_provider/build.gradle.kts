plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.location_provider"
}

dependencies {
    implementation(libs.google.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)
}