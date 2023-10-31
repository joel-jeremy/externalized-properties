plugins {
    id("org.gradle.toolchains.foojay-resolver") version "0.7.0"
}

toolchainManagement {
  jvm { 
    javaRepositories {
      repository("foojay") { 
        resolverClass.set(org.gradle.toolchains.foojay.FoojayToolchainResolver::class.java)
      }
    }
  }
}

dependencyResolutionManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }

  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}
