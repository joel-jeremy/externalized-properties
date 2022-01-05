package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion of values to enums.
 */
public class EnumConversionHandler implements ConversionHandler<Enum<?>> {
    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return targetType != null && targetType.isEnum();
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Enum<?>> convert(ConversionContext context) {
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
            "Invalid (%s) value: %s",
            enumClass.getName(),
            context.value()
        ));
    }
}
