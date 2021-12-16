package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
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
    public Enum<?> convert(ConversionContext context) {
        requireNonNull(context, "context");
        
        Class<?> enumClass = context.rawTargetType();
        
        Object[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null) {
            throw new ConversionException("Type is not an enum: " + enumClass.getName());
        }

        try {
            for (Object enumConstant : enumConstants) {
                Enum<?> enumValue = (Enum<?>)enumConstant;
                if (enumValue.name().equals(context.value())) {
                    return enumValue;
                }
            }
        } catch (Exception ex) {
            throw new ConversionException(String.format(
                    "Failed to convert value to an enum of type %s: %s",
                    enumClass.getName(),
                    context.value()
                ),
                ex
            );
        }
        
        throw new ConversionException(String.format(
            "Invalid (%s) value: %s",
            enumClass.getName(),
            context.value()
        ));
    }
}
