package io.github.joeljeremy7.externalizedproperties.core.testentities.proxy;

public interface NoAnnotationProxyInterface {
    // No annotation with constant default value.
    default String propertyWithNoAnnotationButWithDefaultValue() {
        return "default.value";
    }

    // No annotation but with default value parameter.
    default String propertyWithNoAnnotationButWithDefaultValueParameter(String defaultValue) {
        return defaultValue;
    }

    // No annotation and no default value.
    String propertyWithNoAnnotationAndNoDefaultValue();
}
