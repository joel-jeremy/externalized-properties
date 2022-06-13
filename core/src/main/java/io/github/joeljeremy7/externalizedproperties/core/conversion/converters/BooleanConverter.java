package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;

import java.lang.reflect.Type;

/**
 * Supports conversion of values to a boolean/Boolean.
 */
public class BooleanConverter implements Converter<Boolean> {

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return boolean.class.equals(targetType) || Boolean.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Boolean> convert(
            InvocationContext context, 
            String valueToConvert, 
            Type targetType
    ) {
        return ConversionResult.of(Boolean.parseBoolean(valueToConvert));
    }
}
