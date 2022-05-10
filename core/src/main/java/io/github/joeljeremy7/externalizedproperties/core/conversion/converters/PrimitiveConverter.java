package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Type;

/**
 * Supports conversion of values to Java's primitive types.
 */
public class PrimitiveConverter implements Converter<Object> {
    /**
     * The {@link ConverterProvider} for {@link PrimitiveConverter}.
     * 
     * @return The {@link ConverterProvider} for {@link PrimitiveConverter}.
     */
    public static ConverterProvider<PrimitiveConverter> provider() {
        return (externalizedProperties, rootConverter) -> new PrimitiveConverter();
    }

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
        return Boolean.class.equals(targetType) || 
            Integer.class.equals(targetType) ||
            Long.class.equals(targetType) ||
            Short.class.equals(targetType) ||
            Float.class.equals(targetType) ||
            Double.class.equals(targetType) ||
            Byte.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Object> convert(
            ProxyMethod proxyMethod,
            String valueToConvert,
            Type targetType
    ) {
        if (boolean.class.equals(targetType) || Boolean.class.equals(targetType)) {
            return ConversionResult.of(Boolean.parseBoolean(valueToConvert));
        } 
        else if (int.class.equals(targetType) || Integer.class.equals(targetType)) {
            return ConversionResult.of(Integer.parseInt(valueToConvert));
        } 
        else if (long.class.equals(targetType) || Long.class.equals(targetType)) {
            return ConversionResult.of(Long.parseLong(valueToConvert));
        } 
        else if (short.class.equals(targetType) || Short.class.equals(targetType)) {
            return ConversionResult.of(Short.parseShort(valueToConvert));
        } 
        else if (float.class.equals(targetType) || Float.class.equals(targetType)) {
            return ConversionResult.of(Float.parseFloat(valueToConvert));
        } 
        else if (double.class.equals(targetType) || Double.class.equals(targetType)) {
            return ConversionResult.of(Double.parseDouble(valueToConvert));
        } 
        else if (byte.class.equals(targetType) || Byte.class.equals(targetType)) {
            return ConversionResult.of(Byte.parseByte(valueToConvert));
        }

        // Not a primitive.
        return ConversionResult.skip();
    }
}
