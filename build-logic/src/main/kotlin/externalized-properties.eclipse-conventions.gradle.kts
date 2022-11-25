import org.gradle.plugins.ide.eclipse.model.AbstractClasspathEntry
import org.gradle.plugins.ide.eclipse.model.Classpath

plugins {
  id("eclipse")
}

eclipse.classpath.file {
  whenMerged { classpath: Classpath ->
    // To fix an issue in Eclipse buildship where dependent projects cannot resolve classes
    // of the dependee project if dependee has JMH sources.
    classpath.entries
        .filterIsInstance<AbstractClasspathEntry>()
        .filter { it.path.startsWith("src/jmh") }
        .forEach { it.entryAttributes["test"] = "true" }
  }
}
