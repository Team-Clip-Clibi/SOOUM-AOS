plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.token"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:core-common"))
}