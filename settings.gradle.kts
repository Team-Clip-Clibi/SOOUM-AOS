pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        includeBuild("build-logic")
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SOOUM"
include(":app")
include(":presentation")
include(":domain")
include(":data")
include(":core")
include(":data:network")
include(":data:device")
include(":core:core-design")
include(":presentation:splash")
include(":core:core-common")
include(":presentation:sign-up")
include(":data:repository")
include(":presentation:home")
include(":data:token")
include(":data:paging")
