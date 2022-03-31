package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.StripEmptyValues;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.Tokenizer;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

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
    public ConversionResult<? extends Object[]> convert(ConversionContext context) {
        requireNonNull(context, "context");
        
        // Do not allow T[].
        throwIfArrayHasTypeVariables(context);

        Class<?> rawTargetType = context.rawTargetType();
        Class<?> rawArrayComponentType = rawTargetType.getComponentType();
        if (rawArrayComponentType == null) {
            // Not an array.
            return ConversionResult.skip();
        }

        String propertyValue = context.value();
        if (propertyValue.isEmpty()) {
            return ConversionResult.of(newArray(rawTargetType, 0));
        }

        final String[] values = tokenizer.tokenizeValue(context);
        
        // If array is String[] or Object[], return the string values.
        if (String.class.equals(rawArrayComponentType) || 
                Object.class.equals(rawArrayComponentType)) {
            return ConversionResult.of(values);
        }

        // Generic array component type handling e.g. Optional<String>[]
        GenericArrayType genericArrayType = TypeUtilities.asGenericArrayType(
            context.targetType()
        );
        
        if (genericArrayType != null) {
            Type genericArrayComponentType = genericArrayType.getGenericComponentType();
        
            return ConversionResult.of(
                convertValuesToArrayComponentType(
                    context,
                    values,
                    genericArrayComponentType
                )
            );
        }

        // Just convert to raw type.
        return ConversionResult.of(
            convertValuesToArrayComponentType(
                context,
                values,
                rawArrayComponentType
            )
        );
    }

    private Object[] convertValuesToArrayComponentType(
            ConversionContext context,
            String[] values,
            Type arrayComponentType
    ) {
        Object[] convertedArray = newArray(
            TypeUtilities.getRawType(arrayComponentType), 
            values.length
        );

        for (int i = 0; i < values.length; i++) {
            ConversionResult<?> converted = context.converter().convert(
                context.with(values[i], arrayComponentType)
            );
            convertedArray[i] = converted.value();
        }

        return convertedArray;
    }

    private Object[] newArray(Class<?> arrayComponentType, int length) {
        return (Object[])Array.newInstance(
            arrayComponentType, 
            length
        );
    }

    private void throwIfArrayHasTypeVariables(ConversionContext context) {
        Type genericTargetType = context.targetType();
        GenericArrayType genericArray = TypeUtilities.asGenericArrayType(genericTargetType);
        if (genericArray != null) {
            if (TypeUtilities.isTypeVariable(genericArray.getGenericComponentType())) {
                throw new ConversionException(
                    "Type variables e.g. T[] are not supported."
                );
            }
        }
    }
}
