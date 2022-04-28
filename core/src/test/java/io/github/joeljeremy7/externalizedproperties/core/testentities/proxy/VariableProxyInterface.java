package io.github.joeljeremy7.externalizedproperties.core.testentities.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;

public interface VariableProxyInterface {
    @ExternalizedProperty("property-${custom.variable}")
    String variableProperty();

    @ExternalizedProperty("custom.variable")
    String customVariable();
}
