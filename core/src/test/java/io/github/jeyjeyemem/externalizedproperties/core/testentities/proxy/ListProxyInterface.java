package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.StripEmptyValues;

import java.util.Collection;
import java.util.List;

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
}
