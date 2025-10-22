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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "sumphp"
include(":sumphp")
include(":sumphp:app_01_compose_coffee")
include(":sumphp:app_02_kakao_empw")
include(":sumphp:app_03_profile_card")
include(":sumphp:app_04_key_pad")
include(":sumphp:app_05_counter")
include(":sumphp:app_06_stopwatch")
include(":sumphp:app_07_bubble_game")
