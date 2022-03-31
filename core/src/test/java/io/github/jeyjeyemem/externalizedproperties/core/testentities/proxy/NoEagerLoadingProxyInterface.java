package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;

public interface NoEagerLoadingProxyInterface {
    String noAnnotation();

    @ExternalizedProperty("with.parameters")
    String withParameters(String willNotEagerLoad);
}