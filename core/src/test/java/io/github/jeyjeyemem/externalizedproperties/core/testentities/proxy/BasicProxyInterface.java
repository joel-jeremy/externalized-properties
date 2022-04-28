package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;

public interface BasicProxyInterface {
    @ExternalizedProperty("property")
    String property();

    @ExternalizedProperty("property.1")
    String property1();

    @ExternalizedProperty("property.2")
    String property2();

    @ExternalizedProperty("property.3")
    String property3();
}
