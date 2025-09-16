plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.repository"
}

dependencies {
    implementation(project(":data:device"))
    implementation(project(":data:network"))
    implementation(project(":domain"))
}