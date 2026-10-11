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
// Secrets are read from local.properties (git-ignored), then from an environment variable,
// then from a Gradle property (e.g. ~/.gradle/gradle.properties). Never commit them.
val localProperties = java.util.Properties().apply {
    val file = settingsDir.resolve("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun secret(name: String): String? =
    localProperties.getProperty(name)?.takeIf { it.isNotBlank() }
        ?: providers.environmentVariable(name).orNull?.takeIf { it.isNotBlank() }
        ?: providers.gradleProperty(name).orNull?.takeIf { it.isNotBlank() }

// Only needed for the native Mapbox Maps SDK (com.mapbox.maps:android). This sample renders the
// map with Mapbox GL JS in a WebView, so the repository is added only when a token is configured.
val mapboxDownloadsToken = secret("MAPBOX_DOWNLOADS_TOKEN")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        if (mapboxDownloadsToken != null) {
            maven {
                url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
                authentication {
                    create<BasicAuthentication>("basic")
                }
                credentials {
                    username = "mapbox"
                    password = mapboxDownloadsToken
                }
            }
        }
    }
}

rootProject.name = "mapbox"
include(":app")