package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;

public interface VoidReturnTypeProxyInterface {
    // Invalid: Void return types not allowed.
    @ExternalizedProperty("test.invalid.method.void")
    void invalidVoidMethod();

    // Invalid: Void return types not allowed.
    @ExternalizedProperty("test.invalid.method.void")
    Void invalidVoidClassMethod();
}
