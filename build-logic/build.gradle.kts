plugins {
    `kotlin-dsl`
}
group = "sooum.buildlogic"
version = "1.0-SNAPSHOT"

gradlePlugin {
    plugins {
        create("applicationConvention") {
            id = "sooum.android.application"
            implementationClass = "convention.ApplicationConvention"
        }
        create("networkConvention") {
            id = "sooum.android.network"
            implementationClass = "convention.NetworkConvention"
        }
        create("hiltConvention") {
            id = "sooum.android.hilt"
            implementationClass = "convention.HiltConvention"
        }
        create("domainConvention") {
            id = "sooum.android.domain"
            implementationClass = "convention.DomainConvention"
        }
        create("coreConvention") {
            id = "sooum.android.core"
            implementationClass = "convention.CoreConvention"
        }
        create("presentationConvention") {
            id = "sooum.android.presentation"
            implementationClass = "convention.PresentationConvention"
        }
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    compileOnly("com.android.tools.build:gradle:8.6.0")
    compileOnly("org.jetbrains.kotlin.android:org.jetbrains.kotlin.android.gradle.plugin:2.1.0")
}