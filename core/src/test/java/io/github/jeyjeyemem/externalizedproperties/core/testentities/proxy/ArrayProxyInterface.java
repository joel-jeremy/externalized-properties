package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.StripEmptyValues;

import java.util.Optional;

public interface ArrayProxyInterface {
    @ExternalizedProperty("property.array")
    String[] arrayProperty();

    @ExternalizedProperty("property.array.object")
    Object[] arrayPropertyObject();

    @ExternalizedProperty("property.array.custom.delimiter")
    @Delimiter("|")
    String[] arrayCustomDelimiter();

    @ExternalizedProperty("property.array.stripempty")
    @StripEmptyValues
    String[] arrayPropertyStripEmpty();

    @ExternalizedProperty("property.array.integer.wrapper")
    Integer[] arrayIntegerWrapper();

    @ExternalizedProperty("property.array.integer.primitive")
    int[] arrayIntegerPrimitive();

    @ExternalizedProperty("property.array.generic")
    Optional<String>[] arrayPropertyGeneric();

    @ExternalizedProperty("property.array.generic.wildcard")
    Optional<?>[] arrayPropertyGenericWildcard();

    @ExternalizedProperty("property.array.T")
    <T> T[] arrayPropertyT();
}
