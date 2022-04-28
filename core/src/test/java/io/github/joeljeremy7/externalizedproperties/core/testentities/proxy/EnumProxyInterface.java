package io.github.joeljeremy7.externalizedproperties.core.testentities.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;

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
