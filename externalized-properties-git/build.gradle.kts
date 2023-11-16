plugins {
  id("externalized-properties.java-library-conventions")
  id("externalized-properties.java-multi-jvm-test-conventions")
  id("externalized-properties.java-testing-conventions")
  id("externalized-properties.java-code-quality-conventions")
  id("externalized-properties.java-publish-conventions")
  id("externalized-properties.eclipse-conventions")
}

description = "Externalized Properties Git Module"

tasks.named<Jar>("jar") {
  manifest {
    attributes(mapOf(
      "Automatic-Module-Name" to "io.github.joeljeremy.externalizedproperties.git"
    ))
  }
}

dependencies {
  implementation(project(":externalized-properties-core"))
  implementation(libs.jgit)
  testImplementation(testFixtures(project(":externalized-properties-core")))
  testImplementation("org.eclipse.jgit:org.eclipse.jgit.ssh.apache:${libs.versions.jgit.get()}")
  testImplementation("org.eclipse.jgit:org.eclipse.jgit.junit.http:${libs.versions.jgit.get()}")
  testImplementation("org.eclipse.jgit:org.eclipse.jgit.junit.ssh:${libs.versions.jgit.get()}")
  // For JsonReader.
  testImplementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
  // Required by jgit.junit modules.
  testRuntimeOnly("junit:junit:4.13.2")
}
