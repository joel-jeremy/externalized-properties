package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.StripEmptyValues;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ListProxyInterface {
    @ExternalizedProperty("property.list")
    List<String> listProperty();

    @ExternalizedProperty("property.list.object")
    List<Object> listPropertyObject();

    @ExternalizedProperty("property.list.custom.delimiter")
    @Delimiter("#")
    List<String> listCustomDelimiter();

    @ExternalizedProperty("property.list.integer")
    List<Integer> listInteger();

    @ExternalizedProperty("property.list.wildcard")
    List<?> listPropertyWildcard();

    @ExternalizedProperty("property.collection")
    Collection<String> collectionProperty();

    @ExternalizedProperty("property.collection.custom.delimiter")
    @Delimiter("#")
    Collection<String> collectionCustomDelimiter();

    @ExternalizedProperty("property.collection.integer")
    Collection<Integer> collectionInteger();

    @ExternalizedProperty("property.collection.wildcard")
    Collection<?> collectionPropertyWildcard();

    @ExternalizedProperty("property.list.stripempty")
    @StripEmptyValues
    List<String> listPropertyStripEmpty();

    @ExternalizedProperty("property.list.nested.generics")
    List<Optional<String>> listPropertyNestedGenerics();

    @ExternalizedProperty("property.list.nested.generics.array")
    List<Optional<String>[]> listPropertyNestedGenericsArray();

    @ExternalizedProperty("property.list.T")
    <T> List<T> listPropertyT();
}
