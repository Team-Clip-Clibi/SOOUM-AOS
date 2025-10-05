plugins {
    id("sooum.android.core")
    id("sooum.android.hilt")
}

android {
    namespace = "com.phew.core.ui"
}

dependencies {
    api(project(":core:core-design"))
}