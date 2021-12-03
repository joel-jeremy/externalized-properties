package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.TypeReference;

import java.lang.reflect.Type;

/**
 * This is an internal API to convert resolved properties to various types.
 */
public interface Converter {
    /**
     * Convert value to the expected type.
     * 
     * @param value The value to convert.
     * @param expectedType The type to convert to. To specify generic types, the
     * {@link TypeReference} class can be used to build the type and pass it here.
     * @return The converted value.
     */
    Object convert(String value, Type expectedType);

    /**
     * Convert value to the expected type.
     * 
     * @param externalizedPropertyMethodInfo The externalized property method info.
     * @param value The value to convert.
     * @param expectedType The type to convert to. To specify generic types, the
     * {@link TypeReference} class can be used to build the type and pass it here.
     * @return The converted value.
     */
    Object convert(
        ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo,
        String value, 
        Type expectedType
    );
}
