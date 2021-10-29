package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverterContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.StripEmptyValues;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;

import java.util.Arrays;
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
    public Object[] convert(ResolvedPropertyConversionHandlerContext context) {
        requireNonNull(context, "context");
        
        ExternalizedPropertyMethodInfo propertyMethodInfo = context.externalizedPropertyMethodInfo();

        ResolvedProperty resolvedProperty = context.resolvedProperty();

        String propertyValue = resolvedProperty.value();
        if (propertyValue.isEmpty()) {
            return new String[0];
        }
        
        final String[] values = getValues(context);

        Class<?> arrayType = context.expectedType().getComponentType();
        if (arrayType == null) {
            throw new ResolvedPropertyConversionException(String.format(
                "Externalized Property method does not return an array. " +
                "Externalized property method: %s",
                propertyMethodInfo.methodSignatureString()
            ));
        }
        
        // If array is String[] or Object[], return the string values.
        if (Object.class.equals(arrayType) ||
                String.class.equals(arrayType)) {
            return values;
        }

        ResolvedPropertyConverter resolvedPropertyConverter = 
            context.resolvedPropertyConverter();

        // Convert and return values.
        return IntStream.range(0, values.length).mapToObj(i -> {
            String value = values[i];
            return resolvedPropertyConverter.convert(
                new ResolvedPropertyConverterContext(
                    context.externalizedPropertyMethodInfo(),
                    ResolvedProperty.with(
                        indexedName(resolvedProperty.name(), i),
                        value
                    ), 
                    arrayType
                )
            );
        })
        .toArray();
    }

    private String[] getValues(ResolvedPropertyConversionHandlerContext context) {
        ExternalizedPropertyMethodInfo propertyMethodInfo = context.externalizedPropertyMethodInfo();
        String propertyValue = context.resolvedProperty().value();

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
        return propertyValue.split(delimiter);
    }

    private static String indexedName(String name, int index) {
        return name + "[" + index + "]";
    }
}
