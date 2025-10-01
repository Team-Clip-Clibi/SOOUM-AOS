plugins {
    id("sooum.android.core")
}

android {
    namespace = "com.phew.core.ui"
}

dependencies {
    api(project(":core:core-design"))
}