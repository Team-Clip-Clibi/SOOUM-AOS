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
    //  TODO 성일님 이거 어떻게 할까요?
    implementation(libs.jetbrains.kotlinx.serialization.json)
}
