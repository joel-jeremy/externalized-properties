package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.StripEmptyValues;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Supports conversion of resolved properties to an array.
 * 
 * @implNote By default, this uses ',' as default delimiter when splitting resolved property values.
 * This can overriden by annotating the externalized property method with {@link Delimiter} annotation
 * in which case the {@link Delimiter#value()} attribute will be used as the delimiter.
 * 
 * @implNote If stripping of empty values from the array is desired, 
 * the method can be annotated with the {@link StripEmptyValues} annotation. 
 */
public class ArrayPropertyConversionHandler implements ResolvedPropertyConversionHandler<Object[]> {
    private static final String DEFAULT_DELIMITER = ",";

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return expectedType != null && expectedType.isArray();
    }

    /** {@inheritDoc} */
    @Override
    public Object[] convert(ResolvedPropertyConversionContext context) {
        requireNonNull(context, "context");
        
        try {    
            // Do not allow T[].
            throwIfArrayHasTypeVariables(context);

            ExternalizedPropertyMethodInfo propertyMethodInfo = context.externalizedPropertyMethodInfo();
            final String[] values = getValues(context);

            Class<?> rawArrayComponentType = context.rawExpectedType().getComponentType();
            if (rawArrayComponentType == null) {
                throw new ResolvedPropertyConversionException(String.format(
                    "Externalized Property method does not return an array. " +
                    "Externalized property method: %s",
                    propertyMethodInfo.methodSignatureString()
                ));
            }
            
            // If array is String[] or Object[], return the string values.
            if (String.class.equals(rawArrayComponentType) || Object.class.equals(rawArrayComponentType)) {
                return values;
            }

            // Generic array component type handling.
            GenericArrayType genericArrayType = TypeUtilities.asGenericArrayType(context.expectedType());
            if (genericArrayType != null) {
                Type genericArrayComponentType = genericArrayType.getGenericComponentType();
                
                List<Type> genericTypeParametersOfGenericArrayComponentType = 
                    TypeUtilities.getTypeParameters(genericArrayComponentType);
            
                return convertValuesToArrayComponentType(
                    context,
                    values, 
                    genericArrayComponentType,
                    genericTypeParametersOfGenericArrayComponentType
                );
            }

            // Just convert to raw type.
            return convertValuesToArrayComponentType(
                context,
                values, 
                rawArrayComponentType,
                Collections.emptyList()
            );
        } catch (Exception ex) {
            throw new ResolvedPropertyConversionException(
                String.format(
                    "Failed to convert property %s to an array. Property value: %s",
                    context.resolvedProperty().name(),
                    context.resolvedProperty().value()
                ),  
                ex
            );
        }
    }

    private Object[] convertValuesToArrayComponentType(
            ResolvedPropertyConversionContext context, 
            String[] values,
            Type arrayComponentType, 
            List<Type> arrayComponentTypeGenericTypeParameters
    ) {
        ResolvedPropertyConverter resolvedPropertyConverter = 
            context.resolvedPropertyConverter();

        // Convert and return values.
        return IntStream.range(0, values.length).mapToObj(i -> {
            String value = values[i];
            return resolvedPropertyConverter.convert(
                new ResolvedPropertyConversionContext(
                    context.resolvedPropertyConverter(),
                    context.externalizedPropertyMethodInfo(),
                    ResolvedProperty.with(
                        indexedName(context.resolvedProperty().name(), i),
                        value
                    ), 
                    arrayComponentType,
                    arrayComponentTypeGenericTypeParameters
                )
            );
        })
        .toArray(arrayLength -> (Object[])Array.newInstance(
            TypeUtilities.getRawType(arrayComponentType), 
            arrayLength
        ));
    }

    private String[] getValues(ResolvedPropertyConversionContext context) {
        String propertyValue = context.resolvedProperty().value();
        if (propertyValue.isEmpty()) {
            return new String[0];
        }

        ExternalizedPropertyMethodInfo propertyMethodInfo = context.externalizedPropertyMethodInfo();
        // Determine delimiter.
        String delimiter = propertyMethodInfo.findAnnotation(Delimiter.class)
            .map(d -> d.value())
            .orElse(DEFAULT_DELIMITER);

        if (propertyMethodInfo.hasAnnotation(StripEmptyValues.class)) {
            // Filter empty values.
            return Arrays.stream(propertyValue.split(delimiter))
                .filter(v -> !v.isEmpty())
                .toArray(String[]::new);
        }
        return propertyValue.split(Pattern.quote(delimiter));
    }

    private void throwIfArrayHasTypeVariables(ResolvedPropertyConversionContext context) {
        Type genericExpectedType = context.expectedType();
        GenericArrayType genericArray = TypeUtilities.asGenericArrayType(genericExpectedType);
        if (genericArray != null) {
            if (TypeUtilities.isTypeVariable(genericArray.getGenericComponentType())) {
                throw new ResolvedPropertyConversionException(
                    "Type variables e.g. T[] are not supported."
                );
            }
        }
    }

    private static String indexedName(String name, int index) {
        return name + "[" + index + "]";
    }
}
