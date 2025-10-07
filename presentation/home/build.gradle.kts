plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.home"
}

dependencies {
    implementation(project(":presentation:feed"))
}