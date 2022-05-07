package io.github.joeljeremy7.externalizedproperties.core;

public interface VariableExpansionProxyInterface {
    @ExternalizedProperty("${test}")
    String test();
}
