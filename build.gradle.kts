plugins {
  base
  id("externalized-properties.nexus-publish-conventions")
  id("externalized-properties.java-reporting-conventions")
  id("externalized-properties.sonar-conventions")
  id("externalized-properties.coveralls-conventions")
  id("externalized-properties.snyk-conventions")
  id("externalized-properties.dependency-updates-conventions")
  id("eclipse")
  id("idea")
}

allprojects {
  group = "io.github.joel-jeremy.externalized-properties"

  val snapshotSuffix = if (rootProject.hasProperty("release")) ""  else "-SNAPSHOT"
  version = "1.0.0-beta.1${snapshotSuffix}"
}
