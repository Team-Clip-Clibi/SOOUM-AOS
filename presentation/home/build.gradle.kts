plugins {
    id("sooum.android.presentation")
}

android {
    namespace = "com.phew.home"
    
}

dependencies {
    implementation(project(":presentation:feed"))
    implementation(project(":presentation:write"))
    implementation(project(":presentation:reports"))
    implementation(project(":presentation:profile"))
}