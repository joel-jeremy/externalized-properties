package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

public interface EnumProxyInterface {
    @ExternalizedProperty("property.enum")
    TestEnum enumProperty();

    public static enum TestEnum {
        NONE,
        ONE,
        TWO,
        THREE
    }
}
