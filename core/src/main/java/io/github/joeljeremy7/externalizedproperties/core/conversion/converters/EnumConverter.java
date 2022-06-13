package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.TypeUtilities;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;

import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Supports conversion of values to enums.
 */
public class EnumConverter implements Converter<Enum<?>> {
    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return targetType.isEnum();
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Enum<?>> convert(
            InvocationContext context,
            String valueToConvert,
            Type targetType
    ) { 
        Class<?> enumClass = TypeUtilities.getRawType(targetType);
        Object[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null) {
            // Not an enum.
            return ConversionResult.skip();
        }

        for (Object enumConstant : enumConstants) {
            Enum<?> enumValue = (Enum<?>)enumConstant;
            if (enumValue.name().equals(valueToConvert)) {
                return ConversionResult.of(enumValue);
            }
        }
        
        throw new ConversionException(String.format(
            "Invalid (%s) enum value: %s. Valid values are: %s.",
            enumClass.getName(),
            valueToConvert,
            Arrays.toString(enumConstants)
        ));
    }
}
