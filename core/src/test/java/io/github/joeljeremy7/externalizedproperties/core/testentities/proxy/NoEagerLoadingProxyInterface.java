package io.github.joeljeremy7.externalizedproperties.core.testentities.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;

public interface NoEagerLoadingProxyInterface {
    String noAnnotation();

    @ExternalizedProperty("with.parameters")
    String withParameters(String willNotEagerLoad);
}