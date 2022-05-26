package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Convert;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.TypeUtilities;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.conversion.Delimiter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.StripEmptyValues;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.Tokenizer;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * Supports conversion of values to an array.
 * 
 * @apiNote By default, this uses ',' as default delimiter when splitting resolved property values.
 * This can overriden by annotating the proxy interface method with {@link Delimiter} annotation
 * in which case the {@link Delimiter#value()} attribute will be used as the delimiter.
 * 
 * @apiNote If stripping of empty values from the array is desired, 
 * the proxy interface method can be annotated with the {@link StripEmptyValues} annotation. 
 */
public class ArrayConverter implements Converter<Object[]> {
    private final Tokenizer tokenizer = new Tokenizer(",");

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return targetType != null && targetType.isArray();
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Object[]> convert(
            ProxyMethod proxyMethod,
            String valueToConvert,
            Type targetType
    ) {
        // Do not allow T[].
        throwIfArrayHasTypeVariables(targetType);

        Class<?> rawTargetType = TypeUtilities.getRawType(targetType);
        Class<?> rawArrayComponentType = rawTargetType.getComponentType();
        if (rawArrayComponentType == null) {
            // Not an array.
            return ConversionResult.skip();
        }

        if (valueToConvert.isEmpty()) {
            return ConversionResult.of(newArray(rawTargetType, 0));
        }

        final String[] values = tokenizer.tokenizeValue(proxyMethod, valueToConvert);
        
        // If array is String[] or Object[], return the string values.
        if (String.class.equals(rawArrayComponentType) || 
                Object.class.equals(rawArrayComponentType)) {
            return ConversionResult.of(values);
        }

        // Generic array component type handling e.g. Optional<String>[]
        GenericArrayType genericArrayType = TypeUtilities.asGenericArrayType(
            targetType
        );
        
        if (genericArrayType != null) {
            Type genericArrayComponentType = genericArrayType.getGenericComponentType();
        
            return ConversionResult.of(
                convertValuesToArrayComponentType(
                    proxyMethod,
                    values,
                    genericArrayComponentType
                )
            );
        }

        // Just convert to raw type.
        return ConversionResult.of(
            convertValuesToArrayComponentType(
                proxyMethod,
                values,
                rawArrayComponentType
            )
        );
    }

    private Object[] convertValuesToArrayComponentType(
            ProxyMethod proxyMethod,
            String[] values,
            Type arrayComponentType
    ) {
        Object[] convertedArray = newArray(
            TypeUtilities.getRawType(arrayComponentType), 
            values.length
        );

        ConverterProxy rootConverter = proxyMethod.externalizedProperties()
            .initialize(ConverterProxy.class);
        
        for (int i = 0; i < values.length; i++) {
            Object converted = rootConverter.convert(
                values[i],
                arrayComponentType
            );
            convertedArray[i] = converted;
        }

        return convertedArray;
    }

    private Object[] newArray(Class<?> arrayComponentType, int length) {
        return (Object[])Array.newInstance(
            arrayComponentType, 
            length
        );
    }

    private void throwIfArrayHasTypeVariables(Type targetType) {
        GenericArrayType genericArray = TypeUtilities.asGenericArrayType(targetType);
        if (genericArray != null && 
                TypeUtilities.isTypeVariable(genericArray.getGenericComponentType())) {
            throw new ConversionException("Type variables e.g. T[] are not supported.");
        }
    }

    private static interface ConverterProxy {
        @Convert
        Object convert(String valueToConvert, Type targetType);
    }
}
