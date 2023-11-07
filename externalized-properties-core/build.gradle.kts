plugins {
  id("externalized-properties.java-library-conventions")
  id("externalized-properties.java-multi-jvm-test-conventions")
  id("externalized-properties.java-testing-conventions")
  id("externalized-properties.java-code-quality-conventions")
  id("externalized-properties.java-publish-conventions")
  id("externalized-properties.eclipse-conventions")
  // See https://youtrack.jetbrains.com/issue/KTIJ-19370
  @Suppress("DSL_SCOPE_VIOLATION")
  alias(libs.plugins.jmh)
}

description = "Externalized Properties Core"

tasks.named<Jar>("jar") {
  manifest {
    attributes(mapOf(
      "Automatic-Module-Name" to "io.github.joeljeremy.externalizedproperties.core"
    ))
  }
}

dependencies {
  testImplementation(platform("com.fasterxml.jackson:jackson-bom:2.15.3"))
  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
  // For testing custom JCE providers with DecryptProcessor.JceDecryptor.
  testImplementation("org.bouncycastle:bcprov-jdk18on:1.72")
}

jmh {
  jmhVersion = "1.35"
  humanOutputFile = layout.buildDirectory.file("reports/jmh/human.txt")
  resultsFile = layout.buildDirectory.file("reports/jmh/results.json")
  resultFormat = "JSON"
  jvmArgs.addAll(listOf("-Xmx2G"))
}
