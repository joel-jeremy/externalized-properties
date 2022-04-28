package io.github.joeljeremy7.externalizedproperties.core.testentities.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;

public interface SystemPropertiesProxyInterface {
    @ExternalizedProperty("java.version")
    String javaVersion();
}