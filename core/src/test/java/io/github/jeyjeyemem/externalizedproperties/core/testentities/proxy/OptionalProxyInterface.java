package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

import java.util.List;
import java.util.Optional;

public interface OptionalProxyInterface {
    @ExternalizedProperty("property.optional")
    Optional<String> optionalProperty();

    @ExternalizedProperty("property.optional.with.default.value")
    default Optional<String> optionalPropertyWithDefaultValue() {
        return Optional.of("default.value");
    }

    @ExternalizedProperty("property.optional.with.default.value")
    default Optional<String> optionalPropertyWithDefaultValueParameter(String defaultValue) {
        return Optional.ofNullable(defaultValue);
    }

    // No annotation with default value.
    default Optional<String> optionalPropertyWithNoAnnotationAndWithDefaultValue() {
        return Optional.of("default.value");
    }

     // No annotation with provided default value.
     default Optional<String> optionalPropertyWithNoAnnotationAndWithDefaultValueParameter(String defaultValue) {
        return Optional.ofNullable(defaultValue);
    }

    // No annotation ano no default value.
    Optional<String> optionalPropertyWithNoAnnotationAndNoDefaultValue();

    @ExternalizedProperty("property.optional.nonstring")
    Optional<Integer> nonStringOptionalProperty();

    @ExternalizedProperty("property.optional.object")
    Optional<Object> optionalPropertyObject();

    @ExternalizedProperty("property.optional.wildcard")
    Optional<?> optionalPropertyWildcard();

    @ExternalizedProperty("property.optional.nested.generics")
    Optional<List<String>> optionalPropertyNestedGenerics();

    @ExternalizedProperty("property.optional.nested.generics.array")
    Optional<Optional<String>[]> optionalPropertyNestedGenericsArray();

    @ExternalizedProperty("property.optional.T")
    <T> Optional<T> optionalPropertyT();

    @ExternalizedProperty("property.optional.array")
    Optional<String[]> optionalPropertyArray();
}
