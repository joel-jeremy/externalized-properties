package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;

import java.lang.reflect.Type;

/**
 * Supports conversion of values to a double/Double.
 */
public class DoubleConverter implements Converter<Double> {

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return double.class.equals(targetType) || Double.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Double> convert(
            InvocationContext context, 
            String valueToConvert, 
            Type targetType
    ) {
        return ConversionResult.of(Double.parseDouble(valueToConvert));
    }
}
