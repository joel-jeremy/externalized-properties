package io.github.joeljeremy7.externalizedproperties.core.testentities.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;

public interface DefaultValueProxyInterface {
    @ExternalizedProperty("property.with.default.value")
    default String propertyWithDefaultValueParameter(String defaultValue) {
        return defaultValue;
    }

    @ExternalizedProperty("property.with.default.value")
    default String propertyWithDefaultValue() {
        return "default.value";
    }
}