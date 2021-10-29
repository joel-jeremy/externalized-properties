package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

public interface BasicProxyInterface {
    @ExternalizedProperty("property.1")
    String property1();


    @ExternalizedProperty("property.2")
    String property2();

    @ExternalizedProperty("property.with.default.value")
    default String propertyWithDefaultValue() {
        return "default.value";
    }

    @ExternalizedProperty("property.with.default.value")
    default String propertyWithDefaultValueParameter(String defaultValue) {
        return defaultValue;
    }

    // No annotation with constant default value.
    default String propertyWithNoAnnotationButWithDefaultValue() {
        return "default.value";
    }

    // No annotation but with default value parameter.
    default String propertyWithNoAnnotationButWithDefaultValueParameter(String defaultValue) {
        return defaultValue;
    }

    // No annotation ano no default value.
    String propertyWithNoAnnotationAndNoDefaultValue();
}
