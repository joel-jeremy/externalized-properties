package io.github.joeljeremy7.externalizedproperties.core.testentities.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;

public interface EnvironmentVariablesProxyInterface {
    /**
     * EnvironmentVariableResolver supports formatting of
     * property names such that path is converted to PATH.
     */
    @ExternalizedProperty("path")
    String path();

    /**
     * EnvironmentVariableResolver supports formatting of
     * property names such that java.home is converted to JAVA_HOME.
     */
    @ExternalizedProperty("java.home")
    String javaHome();
}