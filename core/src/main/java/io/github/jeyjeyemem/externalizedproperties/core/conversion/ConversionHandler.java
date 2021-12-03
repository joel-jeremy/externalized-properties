package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

/**
 * When an interface method that is marked with {@link ExternalizedProperty}
 * has a non-{@link String} return type, the proxy loader will attempt to convert resolved
 * properties to match the return type using {@link ConversionHandler}s.
 * 
 * @param <T> The type to convert to.
 */
public interface ConversionHandler<T> {
    /**
     * Checks if the handler can convert properties to the specified type.
     * 
     * @param expectedType The type to convert to.
     * @return {@code true}, if the implementation can convert resolved properties 
     * to the specified type. Otherwise, {@code false}.
     */
    boolean canConvertTo(Class<?> expectedType);

    /**
     * Convert resolved property to the expected type.
     * 
     * @param context The conversion context which contains
     * the resolved property and the type to convert to.
     * @return The converted value.
     */
    T convert(ConversionContext context);

    /**
     * Convert resolved property to the expected type.
     * 
     * @param context The conversion context which contains
     * the resolved property and externalized property method info.
     * @return The converted value.
     */
    T convert(PropertyMethodConversionContext context);
}
