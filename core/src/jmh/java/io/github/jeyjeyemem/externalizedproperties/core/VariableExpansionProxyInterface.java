package io.github.jeyjeyemem.externalizedproperties.core;

public interface VariableExpansionProxyInterface {
    @ExternalizedProperty("${test}")
    String test();
}
