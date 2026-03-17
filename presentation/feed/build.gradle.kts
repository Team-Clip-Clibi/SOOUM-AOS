plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.presentation.feed"
}

dependencies {
    implementation(libs.paging.compose)
    implementation(libs.paging.runtime)
    api(libs.google.gms.admob)
    implementation(project(":core:ui"))
    implementation(project(":presentation:detail"))
}
