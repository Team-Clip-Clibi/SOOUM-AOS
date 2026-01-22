plugins {
    id("sooum.android.network")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // 임시 조치: network 레이어가 domain의 interceptor 인터페이스를 사용하기 때문에 추가
    // TODO: 클린 아키텍처 개선을 위해 InterceptorManager, GlobalEventBus 등을 
    // core-common 모듈로 이동하거나 별도의 infrastructure 모듈 생성 고려
    implementation(project(":domain"))
}
