package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;

public interface NoPropertyNameProxyInterface {
    @ExternalizedProperty
    String resolve(String propertyName);
}
