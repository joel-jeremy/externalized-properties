package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;

import java.lang.reflect.Type;

/**
 * Supports conversion of values to a {@link Class}.
 */
public class ClassConverter implements Converter<Class<?>> {

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return Class.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Class<?>> convert(
            InvocationContext context, 
            String valueToConvert, 
            Type targetType
    ) {
        try {
            return ConversionResult.of(Class.forName(valueToConvert));
        } catch (ClassNotFoundException e) {
            throw new ConversionException(
                String.format("Failed to load as Class: %s", valueToConvert), 
                e
            );
        }
    }
}
