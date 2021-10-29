package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

public interface ThrowingProxyInterface {
    @ExternalizedProperty("property.that.throws")
    default String throwingProperty() {
        throw new RuntimeException("Hi from ThrowingProxyInterface.throwingProperty!");
    }
}
