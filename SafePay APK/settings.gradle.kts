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
//        maven {
//            url = uri("https://payu.jfrog.io/artifactory/android-sdk")
//        }
        gradlePluginPortal()
    }

    plugins {
        id("androidx.navigation.safeargs.kotlin") version "2.7.7" // Use latest or match your Navigation version
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SafePay - APK"
include(":app")
 