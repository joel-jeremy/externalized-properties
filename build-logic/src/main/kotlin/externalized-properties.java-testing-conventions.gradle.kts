plugins {
  id("externalized-properties.java-conventions")
  id("java-test-fixtures")
  id("jvm-test-suite")
}

testing {
  suites {
    register<JvmTestSuite>("integrationTest") {
      testType = TestSuiteType.INTEGRATION_TEST
      targets {
        all {
          testTask {
            shouldRunAfter(tasks.named("test"))
          }
        }
      }
    }
    withType<JvmTestSuite>().configureEach {
      // Use specific junit-jupiter version for all test suites.
      useJUnitJupiter(libs.versions.junitjupiter)
    }
  }
}

tasks.withType<Test>().configureEach {
  if (javaVersion.isCompatibleWith(JavaVersion.VERSION_17)) {
    // We are reflectively setting environment variable in some unit tests.
    // As of Java 17, this is no longer permitted. We need this flag to re-enable
    // the unit test hack. We are opening java.util because we are reflectively
    // accessing the internal mutable map of System.getenv() (which is effectively
    // a java.util.Collections.UnmodifiableMap instance - unless changed in a future
    // Java version).
    jvmArgs = jvmArgs.plus("--add-opens=java.base/java.util=ALL-UNNAMED")
  }
}

tasks.named("check") {
  dependsOn(testing.suites.named<JvmTestSuite>("integrationTest"))
}
