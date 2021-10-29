package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

public interface JavaPropertiesProxyInterface {
    @ExternalizedProperty("java.version")
    String javaVersion();

    @ExternalizedProperty("JAVA_HOME")
    String javaHomeEnv();
}