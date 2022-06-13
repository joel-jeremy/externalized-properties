package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;

import java.lang.reflect.Type;

/**
 * Supports conversion of values to a byte/Byte.
 */
public class ByteConverter implements Converter<Byte> {

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return byte.class.equals(targetType) || Byte.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Byte> convert(
            InvocationContext context, 
            String valueToConvert, 
            Type targetType
    ) {
        return ConversionResult.of(Byte.parseByte(valueToConvert));
    }
}
