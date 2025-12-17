plugins {
    id("sooum.android.core")
    id("sooum.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.phew.core.ui"
}

dependencies {
    api(project(":core:core-design"))
    api(project(":core:core-common"))
}
