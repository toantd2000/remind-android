pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "ReMind"
include(":app")
include(":core:model")
include(":core:common")
include(":core:designsystem")
include(":core:domain")
include(":core:alarm")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:network")
include(":core:ads:api")
include(":core:ads:impl")
include(":features:alarms")
include(":features:settings")
include(":features:mission")
include(":features:remind")



