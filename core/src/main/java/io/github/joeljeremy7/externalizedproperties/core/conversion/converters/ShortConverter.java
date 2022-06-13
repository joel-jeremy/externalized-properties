package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;

import java.lang.reflect.Type;

/**
 * Supports conversion of values to a short/Short.
 */
public class ShortConverter implements Converter<Short> {

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return short.class.equals(targetType) || Short.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Short> convert(
            InvocationContext context, 
            String valueToConvert, 
            Type targetType
    ) {
        return ConversionResult.of(Short.parseShort(valueToConvert));
    }
}
