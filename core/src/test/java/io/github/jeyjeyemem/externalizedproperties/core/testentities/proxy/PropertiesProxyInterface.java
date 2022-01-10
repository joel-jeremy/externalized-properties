package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

import java.util.Properties;

public interface PropertiesProxyInterface {
    // Let's use ClasspathResolver for this.
    @ExternalizedProperty("classpath:test.properties")
    Properties properties();
}
