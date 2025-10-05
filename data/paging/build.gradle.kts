plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.paging"
}

dependencies {
    implementation(libs.paging.runtime)
    implementation(project(":domain"))
    implementation(project(":core:core-common"))
}