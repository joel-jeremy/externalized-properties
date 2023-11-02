dependencyResolutionManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "externalized-properties"

includeBuild("build-logic")

include("externalized-properties-core")
include("externalized-properties-database")
include("externalized-properties-git")