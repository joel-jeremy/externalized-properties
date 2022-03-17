package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;

import java.lang.reflect.Type;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion of values to Java's primitive types.
 */
public class PrimitiveConverter implements Converter<Object> {
    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        if (targetType == null) return false;
        if (targetType.isPrimitive()) return true;

        // Primitive wrappers.
        if (Boolean.class.equals(targetType)) return true;
        else if (Integer.class.equals(targetType)) return true;
        else if (Long.class.equals(targetType)) return true;
        else if (Short.class.equals(targetType)) return true;
        else if (Float.class.equals(targetType)) return true;
        else if (Double.class.equals(targetType)) return true;
        else if (Byte.class.equals(targetType)) return true;

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<?> convert(ConversionContext context) {
        requireNonNull(context, "context");

        Type targetType = context.targetType();

        if (Boolean.class.equals(targetType) || Boolean.TYPE.equals(targetType)) {
            return ConversionResult.of(
                Boolean.parseBoolean(context.value())
            );
        } 
        else if (Integer.class.equals(targetType) || Integer.TYPE.equals(targetType)) {
            return ConversionResult.of(
                Integer.parseInt(context.value())
            );
        } 
        else if (Long.class.equals(targetType) || Long.TYPE.equals(targetType)) {
            return ConversionResult.of(
                Long.parseLong(context.value())
            );
        } 
        else if (Short.class.equals(targetType) || Short.TYPE.equals(targetType)) {
            return ConversionResult.of(
                Short.parseShort(context.value())
            );
        } 
        else if (Float.class.equals(targetType) || Float.TYPE.equals(targetType)) {
            return ConversionResult.of(
                Float.parseFloat(context.value())
            );
        } 
        else if (Double.class.equals(targetType) || Double.TYPE.equals(targetType)) {
            return ConversionResult.of(
                Double.parseDouble(context.value())
            );
        } 
        else if (Byte.class.equals(targetType) || Byte.TYPE.equals(targetType)) {
            return ConversionResult.of(
                Byte.parseByte(context.value())
            );
        }

        // Not a primitive.
        return ConversionResult.skip();
    }
}
