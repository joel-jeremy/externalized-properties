package io.github.jeyjeyemem.externalizedproperties.resolvers.database.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;

public interface DatabaseProxyInterface {
    @ExternalizedProperty("test.property.1")
    String property1();

    @ExternalizedProperty("test.property.2")
    String property2();

    @ExternalizedProperty("non.existent.property")
    String nonExistentProperty();
}