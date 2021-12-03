package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

public interface ProxyInterface {
    @ExternalizedProperty("test")
    String test();
    
    @ExternalizedProperty("testInt")
    int testInt();
}
