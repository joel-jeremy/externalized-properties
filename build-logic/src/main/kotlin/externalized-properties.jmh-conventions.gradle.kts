plugins {
  id("me.champeau.jmh")
}

jmh {
  jmhVersion = "1.37"
  humanOutputFile = layout.buildDirectory.file("reports/jmh/human.txt")
  resultsFile = layout.buildDirectory.file("reports/jmh/results.json")
  resultFormat = "JSON"
  jvmArgs.addAll(listOf("-Xmx2G"))
}