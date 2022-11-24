plugins { 
  id("test-report-aggregation")
  id("jacoco-report-aggregation")
}

reporting {
  reports {
    register<JacocoCoverageReport>("allCodeCoverageReport") { 
      testType.set("all")
      reportTask {
        val testTasks = javaProjects().map { it.tasks.withType<Test>() }
        for (collection in testTasks) {
          collection.configureEach {
            val jacocoExtension = this.extensions.findByType<JacocoTaskExtension>()
            if (jacocoExtension != null) {
              this@reportTask.executionData(this)
              this@reportTask.mustRunAfter(this)
            }
          }
        }
      }
    }
    register<AggregateTestReport>("testAggregateTestReport") { 
      testType.set(TestSuiteType.UNIT_TEST)
    }
    register<AggregateTestReport>("integrationTestAggregateTestReport") { 
      testType.set(TestSuiteType.INTEGRATION_TEST)
    }
  }
}

tasks.register("reports") {
  dependsOn(reporting.reports.withType<JacocoCoverageReport>().map { it.reportTask })
  dependsOn(reporting.reports.withType<AggregateTestReport>().map { it.reportTask })
}

javaProjects().forEach {
  it.tasks.withType<Test>().configureEach {
    finalizedBy(tasks.withType<JacocoReport>())
  }
}

dependencies {
  javaProjects().forEach {
    testReportAggregation(project(it.path))
    jacocoAggregation(project(it.path))
  }
}
