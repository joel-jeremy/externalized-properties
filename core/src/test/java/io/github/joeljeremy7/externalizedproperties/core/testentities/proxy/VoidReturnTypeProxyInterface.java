package io.github.joeljeremy7.externalizedproperties.core.testentities.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;

public interface VoidReturnTypeProxyInterface {
    // Invalid: Void return types not allowed.
    @ExternalizedProperty("test.invalid.method.void")
    void invalidVoidMethod();

    // Invalid: Void return types not allowed.
    @ExternalizedProperty("test.invalid.method.void")
    Void invalidVoidClassMethod();
}
