package io.github.jeyjeyemem.externalizedproperties.core.conversion;

/**
 * Used to convert resolved properties to various types.
 */
public interface Converter {
    /**
     * Convert the resolved property value to a given type.
     * 
     * @param context The context object containing the resolved property and
     * information regarding the type to convert the resolved property to.
     * @return The converted value.
     */
    Object convert(ConversionContext context);

    /**
     * Convert the resolved property value to a given type.
     * 
     * @param context The context object containing the resolved property and
     * information regarding the property method and the type to convert
     * the resolved property to.
     * @return The converted value.
     */
    Object convert(PropertyMethodConversionContext context);
}
