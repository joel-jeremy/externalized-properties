package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.StripEmptyValues;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.regex.Pattern;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion of values to an array.
 * 
 * @implNote By default, this uses ',' as default delimiter when splitting resolved property values.
 * This can overriden by annotating the proxy method with {@link Delimiter} annotation
 * in which case the {@link Delimiter#value()} attribute will be used as the delimiter.
 * 
 * @implNote If stripping of empty values from the array is desired, 
 * the method can be annotated with the {@link StripEmptyValues} annotation. 
 */
public class ArrayConversionHandler implements ConversionHandler<Object[]> {
    private static final String DEFAULT_DELIMITER = ",";

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return targetType != null && targetType.isArray();
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Object[]> convert(ConversionContext context) {
        requireNonNull(context, "context");
        
        // Do not allow T[].
        throwIfArrayHasTypeVariables(context);

        Class<?> rawTargetType = context.rawTargetType();
        String propertyValue = context.value();
        if (propertyValue.isEmpty()) {
            return ConversionResult.of(newArray(rawTargetType, 0));
        }

        final String[] values = getValues(context);

        Class<?> rawArrayComponentType = rawTargetType.getComponentType();
        if (rawArrayComponentType == null) {
            // Not an array.
            return ConversionResult.skip();
        }
        
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
            convertedArray[i] = context.converter().convert(
                context.with(arrayComponentType, values[i])
            );
        }

        return convertedArray;
    }

    private Object[] newArray(Class<?> arrayComponentType, int length) {
        return (Object[])Array.newInstance(
            arrayComponentType, 
            length
        );
    }

    private String[] getValues(ConversionContext context) {
        String value = context.value();
        ProxyMethodInfo proxyMethodInfo = context.proxyMethodInfo().orElse(null);
        if (proxyMethodInfo != null) {
            // Determine delimiter.
            String delimiter = proxyMethodInfo.findAnnotation(Delimiter.class)
                .map(d -> d.value())
                .orElse(DEFAULT_DELIMITER);

            if (proxyMethodInfo.hasAnnotation(StripEmptyValues.class)) {
                // Filter empty values.
                return Arrays.stream(value.split(delimiter))
                    .filter(v -> !v.isEmpty())
                    .toArray(String[]::new);
            }
            return value.split(Pattern.quote(delimiter));
        }

        return value.split(DEFAULT_DELIMITER);
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
