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
        maven("https://jitpack.io")
    }
}

rootProject.name = "SOOUM"
include(":app")
include(":presentation")
include(":domain")
include(":data")
include(":data:network")
include(":data:device")
include(":core:core-design")
include(":presentation:splash")
include(":core:core-common")
include(":core:ui")
include(":presentation:sign-up")
include(":data:repository")
include(":presentation:feed")
include(":data:token")
include(":data:paging")
include(":data:device:location_provider")
include(":data:device:datastore_local")
include(":data:device:device_info")
include(":presentation:home")
include(":presentation:write")
include(":presentation:reports")
