package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;

import java.util.WeakHashMap;
import java.util.function.Function;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Primitive property conversion handler which supports converting to Java's primitive types.
 */
public class PrimitiveConversionHandler implements ConversionHandler<Object> {
    private final WeakHashMap<Class<?>, Function<String, Object>> conversionMapping = 
        getConversionMapping();

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return conversionMapping.containsKey(expectedType);
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(ConversionContext context) {
        return convertInternal(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object convert(PropertyMethodConversionContext context) {
        return convertInternal(context);
    }

    private Object convertInternal(ConversionContext context) {
        requireNonNull(context, "context");
        Function<String, Object> conversion = conversionMapping.get(context.rawExpectedType());
        
        if (conversion == null) {
            // Means classes used as map key were unloaded/GCed.
            conversion = getConversionFunction(context.rawExpectedType());
            conversionMapping.putIfAbsent(context.rawExpectedType(), conversion);
        }

        try {
            return conversion.apply(context.resolvedProperty().value());
        } catch (Exception ex) {
            throw new ConversionException(
                String.format(
                    "Failed to convert property %s to a %s. Property value: %s",
                    context.resolvedProperty().name(),
                    context.expectedType(),
                    context.resolvedProperty().value()
                ),  
                ex
            );
        }
    }

    private static WeakHashMap<Class<?>, Function<String, Object>> getConversionMapping() {
        // Weak hash map as to not prevent GC from collecting the classes used as keys.
        WeakHashMap<Class<?>, Function<String, Object>> conversionMapping = new WeakHashMap<>();
        conversionMapping.put(Boolean.class, getConversionFunction(Boolean.class));
        conversionMapping.put(Boolean.TYPE, getConversionFunction(Boolean.TYPE));
        conversionMapping.put(Byte.class, getConversionFunction(Byte.class));
        conversionMapping.put(Byte.TYPE, getConversionFunction(Byte.TYPE));
        conversionMapping.put(Short.class, getConversionFunction(Short.class));
        conversionMapping.put(Short.TYPE, getConversionFunction(Short.TYPE));
        conversionMapping.put(Integer.class, getConversionFunction(Integer.class));
        conversionMapping.put(Integer.TYPE, getConversionFunction(Integer.TYPE));
        conversionMapping.put(Long.class, getConversionFunction(Long.class));
        conversionMapping.put(Long.TYPE, getConversionFunction(Long.TYPE));
        conversionMapping.put(Float.class, getConversionFunction(Float.class));
        conversionMapping.put(Float.TYPE, getConversionFunction(Float.TYPE));
        conversionMapping.put(Double.class, getConversionFunction(Double.class));
        conversionMapping.put(Double.TYPE, getConversionFunction(Double.TYPE));
        return conversionMapping;
    }

    private static Function<String, Object> getConversionFunction(Class<?> targetType) {
        if (Boolean.class.equals(targetType) || Boolean.TYPE.equals(targetType)) {
            return Boolean::parseBoolean;
        } else if (Integer.class.equals(targetType) || Integer.TYPE.equals(targetType)) {
            return Integer::parseInt;
        } else if (Short.class.equals(targetType) || Short.TYPE.equals(targetType)) {
            return Short::parseShort;
        } else if (Long.class.equals(targetType) || Long.TYPE.equals(targetType)) {
            return Long::parseLong;
        } else if (Float.class.equals(targetType) || Float.TYPE.equals(targetType)) {
            return Float::parseFloat;
        } else if (Double.class.equals(targetType) || Double.TYPE.equals(targetType)) {
            return Double::parseDouble;
        } else if (Byte.class.equals(targetType) || Byte.TYPE.equals(targetType)) {
            return Byte::parseByte;
        }

        throw new ConversionException(
            "Type is not a primitive/primitive wrapper type: " + 
            targetType
        );
    }
}
