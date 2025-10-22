package convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import com.android.build.api.dsl.Lint
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class SOOUMLintConvention : Plugin<Project> {
    override fun apply(target: Project) = with(target) {

        // com.android.application 플러그인이 적용될 때까지 대기 .
        pluginManager.withPlugin("com.android.application") {
            extensions.configure<ApplicationExtension> {
                configureLint(lint)
            }
            extensions.configure<KotlinAndroidProjectExtension> {
                configureKotlinCompilerWarnings()
            }
        }

        //com.android.library"플러그인이 적용될 때까지 대기
        pluginManager.withPlugin("com.android.library") {
            extensions.configure<LibraryExtension> {
                configureLint(lint)
            }
            extensions.configure<KotlinAndroidProjectExtension> {
                configureKotlinCompilerWarnings()
            }
        }
    }
}

private fun configureLint(lint: Lint) {
    lint.apply {
        disable.add("HardcodedText") //하드코딩 경고
        disable.add("IconMissingContentDescription") //설명 누락시(contentDescription) 경고
        fatal.add("NewApi") //minSdk 보다
        fatal.add("ObsoleteSdkInt") // 오래된 방식의 버전 체크 오류
        fatal.add("Deprecated") // 지원 종료 api error
        abortOnError = true //error 또는 fatal 로 지정된 문제 발견시 빌드 중단
        checkReleaseBuilds = false //Debug & release 검사
        htmlReport = true //html 보고서 출력
        checkGeneratedSources = false //Hilt or KSP 자동 생성 코드는 검사 제외
        checkTestSources = false // 테스트 코드 Lint 검사 제외
        warningsAsErrors = true //경고 창일 경우 error
    }
}
// Kotlin 컴파일러 경고 규칙관리
private fun KotlinAndroidProjectExtension.configureKotlinCompilerWarnings() {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}
