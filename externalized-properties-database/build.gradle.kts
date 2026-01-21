plugins {
  id("externalized-properties.java-library-conventions")
  id("externalized-properties.java-multi-jvm-test-conventions")
  id("externalized-properties.java-testing-conventions")
  id("externalized-properties.java-code-quality-conventions")
  id("externalized-properties.java-publish-conventions")
  id("externalized-properties.eclipse-conventions")
}

description = "Externalized Properties Database Module"

tasks.named<Jar>("jar") {
  manifest {
    attributes(mapOf(
      "Automatic-Module-Name" to "io.github.joeljeremy.externalizedproperties.database"
    ))
  }
}

dependencies {
  implementation(project(":externalized-properties-core"))
  testImplementation(testFixtures(project(":externalized-properties-core")))
  testImplementation("com.h2database:h2:2.4.240")
}

testing {
  suites {
    named<JvmTestSuite>("integrationTest") {
      dependencies {
        implementation(project(project.path))
        implementation(testFixtures(project(project.path)))
        implementation(project(":externalized-properties-core"))
        implementation(testFixtures(project(":externalized-properties-core")))
        implementation(project.dependencies.platform("org.testcontainers:testcontainers-bom:1.21.4"))
        implementation("org.testcontainers:junit-jupiter")
        implementation("org.testcontainers:postgresql")
        implementation("org.testcontainers:mysql")
        implementation("org.testcontainers:oracle-xe")
        implementation("org.testcontainers:mssqlserver")
        implementation("org.testcontainers:mariadb")
        implementation("org.testcontainers:db2")
        implementation("com.zaxxer:HikariCP:7.0.2")
        runtimeOnly("org.postgresql:postgresql:42.7.8")
        runtimeOnly("mysql:mysql-connector-java:8.0.33")
        runtimeOnly("com.oracle.database.jdbc:ojdbc8:23.26.0.0.0")
        runtimeOnly("com.microsoft.sqlserver:mssql-jdbc:13.2.1.jre11")
        runtimeOnly("org.xerial:sqlite-jdbc:3.51.1.0")
        runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.5.7")
        runtimeOnly("com.ibm.db2:jcc:12.1.3.0")
      }
    }
  }
}
