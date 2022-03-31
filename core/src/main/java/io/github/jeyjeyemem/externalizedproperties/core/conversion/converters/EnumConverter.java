package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionException;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion of values to enums.
 */
public class EnumConverter implements Converter<Enum<?>> {
    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return targetType != null && targetType.isEnum();
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<? extends Enum<?>> convert(ConversionContext context) {
        requireNonNull(context, "context");
        
        Class<?> enumClass = context.rawTargetType();
        
        Object[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null) {
            // Not an enum.
            return ConversionResult.skip();
        }

        for (Object enumConstant : enumConstants) {
            Enum<?> enumValue = (Enum<?>)enumConstant;
            if (enumValue.name().equals(context.value())) {
                return ConversionResult.of(enumValue);
            }
        }
        
        throw new ConversionException(String.format(
            "Invalid (%s) enum value: %s",
            enumClass.getName(),
            context.value()
        ));
    }
}
