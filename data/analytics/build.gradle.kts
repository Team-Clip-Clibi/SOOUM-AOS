plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.analytics"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    //module
    implementation(project(":core:core-common"))
}