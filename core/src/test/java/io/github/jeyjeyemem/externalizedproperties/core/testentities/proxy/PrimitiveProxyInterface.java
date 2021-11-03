package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

public interface PrimitiveProxyInterface {
    @ExternalizedProperty("property.integer.primitive")
    int intPrimitiveProperty();

    @ExternalizedProperty("property.integer.wrapper")
    Integer integerWrapperProperty();

    @ExternalizedProperty("property.integer.wrapper")
    default Integer integerWrapperWithDefaultValue() {
        return -1;
    }

    @ExternalizedProperty("property.integer.primitive")
    default int intPrimitivePropertyWithDefaultValue() {
        return -1;
    }

    @ExternalizedProperty("property.integer.wrapper")
    default Integer integerWrapperWithDefaultValueParameter(Integer defaultValue) {
        return defaultValue;
    }

    @ExternalizedProperty("property.integer.primitive")
    default int intPrimitivePropertyWithDefaultValueParameter(int defaultValue) {
        return defaultValue;
    }

    @ExternalizedProperty("property.long.primitive")
    long longPrimitiveProperty();

    @ExternalizedProperty("property.long.wrapper")
    Long longWrapperProperty();

    @ExternalizedProperty("property.float.primitive")
    float floatPrimitiveProperty();

    @ExternalizedProperty("property.float.wrapper")
    Float floatWrapperProperty();

    @ExternalizedProperty("property.double.primitive")
    double doublePrimitiveProperty();

    @ExternalizedProperty("property.double.wrapper")
    Double doubleWrapperProperty();
}
