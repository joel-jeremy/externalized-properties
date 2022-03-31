package io.github.jeyjeyemem.externalizedproperties.core;

public interface ProxyInterface {
    @ExternalizedProperty("test")
    String test();
    
    @ExternalizedProperty("testInt")
    int testInt();
}
