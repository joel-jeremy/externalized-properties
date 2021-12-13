package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion of values to Java's primitive types.
 */
public class PrimitiveConversionHandler implements ConversionHandler<Object> {
    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        if (targetType == null) {
            return false;
        }

        if (targetType.isPrimitive()) {
            return true;
        }

        // Primitive wrappers.
        if (Boolean.class.equals(targetType)) {
            return true;
        } else if (Integer.class.equals(targetType)) {
            return true;
        } else if (Long.class.equals(targetType)) {
            return true;
        } else if (Short.class.equals(targetType)) {
            return true;
        } else if (Float.class.equals(targetType)) {
            return true;
        } else if (Double.class.equals(targetType)) {
            return true;
        } else if (Byte.class.equals(targetType)) {
            return true;
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(ConversionContext context) {
        requireNonNull(context, "context");

        Class<?> targetType = context.rawTargetType();

        try {
            if (Boolean.class.equals(targetType) || Boolean.TYPE.equals(targetType)) {
                return Boolean.parseBoolean(context.value());
            } else if (Integer.class.equals(targetType) || Integer.TYPE.equals(targetType)) {
                return Integer.parseInt(context.value());
            } else if (Long.class.equals(targetType) || Long.TYPE.equals(targetType)) {
                return Long.parseLong(context.value());
            } else if (Short.class.equals(targetType) || Short.TYPE.equals(targetType)) {
                return Short.parseShort(context.value());
            } else if (Float.class.equals(targetType) || Float.TYPE.equals(targetType)) {
                return Float.parseFloat(context.value());
            } else if (Double.class.equals(targetType) || Double.TYPE.equals(targetType)) {
                return Double.parseDouble(context.value());
            } else if (Byte.class.equals(targetType) || Byte.TYPE.equals(targetType)) {
                return Byte.parseByte(context.value());
            }
        } catch (Exception ex) {
            throw new ConversionException(
                String.format(
                    "Failed to convert value to %s type: %s",
                    context.targetType(),
                    context.value()
                ),  
                ex
            );
        }
    
        throw new ConversionException(
            "Type is not a primitive/primitive wrapper type: " + 
            targetType
        );
    }
}
