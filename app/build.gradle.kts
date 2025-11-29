plugins{
    id("sooum.android.application")
}
dependencies {
    implementation(libs.androidx.appcompat)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.message)
    implementation(libs.kotlinx.coroutines.play.services)
}
