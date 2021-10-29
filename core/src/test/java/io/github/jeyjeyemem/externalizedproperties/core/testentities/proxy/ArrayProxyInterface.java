package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.StripEmptyValues;

public interface ArrayProxyInterface {
    @ExternalizedProperty("property.array")
    String[] arrayProperty();

    @ExternalizedProperty("property.array.object")
    Object[] arrayPropertyObject();

    @ExternalizedProperty("property.array.integer")
    Integer[] arrayInteger();

    @ExternalizedProperty("property.array.integer.primitive")
    int[] arrayIntegerPrimitive();

    @ExternalizedProperty("property.array.custom.delimiter")
    @Delimiter("|")
    Integer[] arrayCustomDelimiter();

    @ExternalizedProperty("property.array.stripempty")
    @StripEmptyValues
    String[] arrayPropertyStripEmpty();
}
