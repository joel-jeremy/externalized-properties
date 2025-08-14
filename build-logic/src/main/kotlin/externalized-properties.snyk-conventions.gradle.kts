plugins {
  id("io.snyk.gradle.plugin.snykplugin")
}

snyk {
  setApi(findProperty("snykToken") as String?)
  setSeverity("low")
  setArguments("--org=3bd738c1-9bba-45b8-b13d-9870bd4a8a4f --all-sub-projects --sarif-file-output=snyk.sarif")
  setAutoDownload(true)
  setAutoUpdate(true)
}
