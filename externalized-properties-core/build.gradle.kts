plugins {
  id("externalized-properties.java-library-conventions")
  id("externalized-properties.java-multi-jvm-test-conventions")
  id("externalized-properties.java-testing-conventions")
  id("externalized-properties.java-code-quality-conventions")
  id("externalized-properties.java-publish-conventions")
  id("externalized-properties.eclipse-conventions")
  id("externalized-properties.jmh-conventions")
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
  testImplementation(platform("com.fasterxml.jackson:jackson-bom:2.20.1"))
  testImplementation("com.fasterxml.jackson.core:jackson-databind")
  testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
  // For testing custom JCE providers with DecryptProcessor.JceDecryptor.
  testImplementation("org.bouncycastle:bcprov-jdk18on:1.82")
}
