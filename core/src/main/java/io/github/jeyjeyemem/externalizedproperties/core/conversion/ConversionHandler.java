package io.github.jeyjeyemem.externalizedproperties.core.conversion;

/**
 * API for handling conversion of values to a supported type.
 * 
 * @param <T> The type to convert to.
 */
public interface ConversionHandler<T> {
    /**
     * Checks if the handler can convert values to the target type.
     * 
     * @param targetType The target type to convert to.
     * @return {@code true}, if the implementation can convert values 
     * to the specified type. Otherwise, {@code false}.
     */
    boolean canConvertTo(Class<?> targetType);

    /**
     * Convert value to the target type.
     * 
     * @implNote If implementation does not support conversion to the target type, 
     * it may return {@link ConversionResult#skip()} to instruct the converter to skip 
     * the conversion handler and proceed to convert using the next registered conversion 
     * handler.
     * However, if an exception is thrown, the conversion will fail and will not attempt
     * to convert using the other registered conversion handlers.
     * 
     * @param context The conversion context which contains information
     * on the value to convert and the target type. This may also contain information
     * on the proxy method info (if the conversion request came from a proxy method 
     * invocation).
     * @return The conversion result.
     */
    ConversionResult<? extends T> convert(ConversionContext context);
}
