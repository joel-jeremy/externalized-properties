package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

public interface ThrowingProxyInterface {
    @ExternalizedProperty("property.that.throws")
    default String throwRuntimeException() {
        throw new RuntimeException("Hi from ThrowingProxyInterface.throwingProperty!");
    }

    @ExternalizedProperty("property.that.throws")
    default String throwException() throws Exception {
        throw new Exception("Hi from ThrowingProxyInterface.throwingProperty!");
    }
}
