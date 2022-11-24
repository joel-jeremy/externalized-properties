plugins {
  id("externalized-properties.java-library-conventions")
  id("externalized-properties.java-testing-conventions")
  id("externalized-properties.java-code-quality-conventions")
  id("externalized-properties.java-publish-conventions")
  id("externalized-properties.java-multi-jvm-test-conventions")
  // See https://youtrack.jetbrains.com/issue/KTIJ-19370
  @Suppress("DSL_SCOPE_VIOLATION")
  alias(libs.plugins.jmh)
}

description = "Externalized Properties core module"

tasks.named<Jar>("jar") {
  manifest {
    attributes(mapOf(
      "Automatic-Module-Name" to "io.github.joeljeremy.externalizedproperties.core"
    ))
  }
}

dependencies {
  testImplementation(platform("com.fasterxml.jackson:jackson-bom:2.14.0"))
  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
  // For testing custom JCE providers with DecryptProcessor.JceDecryptor.
  testImplementation("org.bouncycastle:bcprov-jdk18on:1.72")
}

jmh {
  jmhVersion.set("1.35")
  humanOutputFile.set(project.file("${project.buildDir}/reports/jmh/human.txt"))
  resultsFile.set(project.file("${project.buildDir}/reports/jmh/results.json"))
  resultFormat.set("JSON")
  jvmArgs.addAll(listOf("-Xmx2G"))
}
