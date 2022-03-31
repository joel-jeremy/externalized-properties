package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;

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