package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Supports conversion of values to Java's primitive types.
 */
public class PrimitiveConversionHandler implements ConversionHandler<Object> {
    // private final WeakHashMap<Class<?>, Function<String, Object>> conversionMapping = 
    //     getConversionMapping();

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        if (targetType == null) {
            return false;
        }

        if (targetType.isPrimitive()) {
            return true;
        }

        if (Boolean.class.equals(targetType)) {
            return true;
        } else if (Integer.class.equals(targetType)) {
            return true;
        } else if (Short.class.equals(targetType)) {
            return true;
        } else if (Long.class.equals(targetType)) {
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
        return convertInternal(context);
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(PropertyMethodConversionContext context) {
        return convertInternal(context);
    }

    private Object convertInternal(ConversionContext context) {
        requireNonNull(context, "context");

        Class<?> targetType = context.rawExpectedType();

        try {
            if (Boolean.class.equals(targetType) || Boolean.TYPE.equals(targetType)) {
                return Boolean.parseBoolean(context.value());
            } else if (Integer.class.equals(targetType) || Integer.TYPE.equals(targetType)) {
                return Integer.parseInt(context.value());
            } else if (Short.class.equals(targetType) || Short.TYPE.equals(targetType)) {
                return Short.parseShort(context.value());
            } else if (Long.class.equals(targetType) || Long.TYPE.equals(targetType)) {
                return Long.parseLong(context.value());
            } else if (Float.class.equals(targetType) || Float.TYPE.equals(targetType)) {
                return Float.parseFloat(context.value());
            } else if (Double.class.equals(targetType) || Double.TYPE.equals(targetType)) {
                return Double.parseDouble(context.value());
            } else if (Byte.class.equals(targetType) || Byte.TYPE.equals(targetType)) {
                return Byte.parseByte(context.value());
            }
    
            throw new ConversionException(
                "Type is not a primitive/primitive wrapper type: " + 
                targetType
            );
        } catch (Exception ex) {
            throw new ConversionException(
                String.format(
                    "Failed to convert value to %s type: %s",
                    context.expectedType(),
                    context.value()
                ),  
                ex
            );
        }
    }

    // private static WeakHashMap<Class<?>, Function<String, Object>> getConversionMapping() {
    //     // Weak hash map as to not prevent GC from collecting the classes used as keys.
    //     WeakHashMap<Class<?>, Function<String, Object>> conversionMapping = new WeakHashMap<>();
    //     conversionMapping.put(Boolean.class, conversionFunction(Boolean.class));
    //     conversionMapping.put(Boolean.TYPE, conversionFunction(Boolean.TYPE));
    //     conversionMapping.put(Byte.class, conversionFunction(Byte.class));
    //     conversionMapping.put(Byte.TYPE, conversionFunction(Byte.TYPE));
    //     conversionMapping.put(Short.class, conversionFunction(Short.class));
    //     conversionMapping.put(Short.TYPE, conversionFunction(Short.TYPE));
    //     conversionMapping.put(Integer.class, conversionFunction(Integer.class));
    //     conversionMapping.put(Integer.TYPE, conversionFunction(Integer.TYPE));
    //     conversionMapping.put(Long.class, conversionFunction(Long.class));
    //     conversionMapping.put(Long.TYPE, conversionFunction(Long.TYPE));
    //     conversionMapping.put(Float.class, conversionFunction(Float.class));
    //     conversionMapping.put(Float.TYPE, conversionFunction(Float.TYPE));
    //     conversionMapping.put(Double.class, conversionFunction(Double.class));
    //     conversionMapping.put(Double.TYPE, conversionFunction(Double.TYPE));
    //     return conversionMapping;
    // }

    // private static Function<String, Object> conversionFunction(Class<?> targetType) {
    //     if (Boolean.class.equals(targetType) || Boolean.TYPE.equals(targetType)) {
    //         return Boolean::parseBoolean;
    //     } else if (Integer.class.equals(targetType) || Integer.TYPE.equals(targetType)) {
    //         return Integer::parseInt;
    //     } else if (Short.class.equals(targetType) || Short.TYPE.equals(targetType)) {
    //         return Short::parseShort;
    //     } else if (Long.class.equals(targetType) || Long.TYPE.equals(targetType)) {
    //         return Long::parseLong;
    //     } else if (Float.class.equals(targetType) || Float.TYPE.equals(targetType)) {
    //         return Float::parseFloat;
    //     } else if (Double.class.equals(targetType) || Double.TYPE.equals(targetType)) {
    //         return Double::parseDouble;
    //     } else if (Byte.class.equals(targetType) || Byte.TYPE.equals(targetType)) {
    //         return Byte::parseByte;
    //     }

    //     throw new ConversionException(
    //         "Type is not a primitive/primitive wrapper type: " + 
    //         targetType
    //     );
    // }
}
