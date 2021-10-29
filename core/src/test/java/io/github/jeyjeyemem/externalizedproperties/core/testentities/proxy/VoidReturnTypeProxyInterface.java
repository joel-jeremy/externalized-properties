package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

public interface VoidReturnTypeProxyInterface {
    // Invalid: Void return types not allowed.
    @ExternalizedProperty("test.invalid.method.void")
    void invalidVoidMethod();
}
