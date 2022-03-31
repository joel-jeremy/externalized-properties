package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.StripEmptyValues;

import java.util.Optional;
import java.util.Set;

public interface SetProxyInterface {
    @ExternalizedProperty("property.set")
    Set<String> setProperty();

    @ExternalizedProperty("property.set.object")
    Set<Object> setPropertyObject();

    @ExternalizedProperty("property.set.custom.delimiter")
    @Delimiter("#")
    Set<String> setCustomDelimiter();

    @ExternalizedProperty("property.set.integer")
    Set<Integer> setInteger();

    @ExternalizedProperty("property.set.wildcard")
    Set<?> setPropertyWildcard();

    @ExternalizedProperty("property.set.stripempty")
    @StripEmptyValues
    Set<String> setPropertyStripEmpty();

    @ExternalizedProperty("property.set.nested.generics")
    Set<Optional<String>> setPropertyNestedGenerics();

    @ExternalizedProperty("property.set.nested.generics.array")
    Set<Optional<String>[]> setPropertyNestedGenericsArray();

    @ExternalizedProperty("property.set.T")
    <T> Set<T> setPropertyT();
}
