package io.github.jeyjeyemem.externalizedproperties.core.conversion;

/**
 * API to convert values to various supported types.
 */
public interface Converter {
    /**
     * Convert value based on the provided conversion context.
     * 
     * @param context The conversion context.
     * @return The converted value.
     */
    Object convert(ConversionContext context);
}
