plugins { 
  id("test-report-aggregation")
  id("jacoco-report-aggregation")
}

reporting {
  reports {
    val allCodeCoverageReport by creating(JacocoCoverageReport::class) {
      testSuiteName = "all"
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
    val testAggregateTestReport by creating(AggregateTestReport::class) { 
      testSuiteName = "test"
    }
    val integrationTestAggregateTestReport by creating(AggregateTestReport::class) { 
      testSuiteName = "integrationTest"
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
