plugins {
    id("sooum.android.network")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":domain"))
}

