plugins {
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.paging"
}

dependencies {
    implementation(libs.paging.runtime)
}