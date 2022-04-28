package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;

public interface SystemPropertiesProxyInterface {
    @ExternalizedProperty("java.version")
    String javaVersion();
}