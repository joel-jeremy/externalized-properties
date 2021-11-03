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
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Supports conversion of resolved properties to a {@link Collection} or a {@link List}.
 * 
 * @implNote By default, this uses ',' as default delimiter when splitting resolved property values.
 * This can overriden by annotating the externalized property method with {@link Delimiter} annotation
 * in which case the {@link Delimiter#value()} attribute will be used as the delimiter. 
 * 
 * @implNote If stripping of empty values from the collection/list is desired, 
 * the method can be annotated with the {@link StripEmptyValues} annotation.  
 */
public class ListPropertyConversionHandler implements ResolvedPropertyConversionHandler<List<?>> {
    private static final String DEFAULT_DELIMITER = ",";

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return List.class.equals(expectedType) ||
            Collection.class.equals(expectedType);
    }

    /** {@inheritDoc} */
    @Override
    public List<?> convert(ResolvedPropertyConversionHandlerContext context) {
        requireNonNull(context, "context");

        try {
            String propertyValue = context.resolvedProperty().value();
            if (propertyValue.isEmpty()) {
                return Collections.emptyList();
            }

            final String[] values = getValues(context);

            Type listGenericTypeParameter = context.expectedTypeGenericTypeParameters()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "List generic type parameter is required."
                ));
            
            // If List<String>, List<Object> or List<?>, return String values.
            if (String.class.equals(listGenericTypeParameter) || 
                    Object.class.equals(listGenericTypeParameter) ||
                    listGenericTypeParameter instanceof WildcardType) {
                return Arrays.asList(values);
            }

            return convertValuesToListType(
                context, 
                values, 
                listGenericTypeParameter
            );
        } catch (Exception ex) {
            throw new ResolvedPropertyConversionException(
                String.format(
                    "Failed to convert property %s to a List/Collection. Property value: %s",
                    context.resolvedProperty().name(),
                    context.resolvedProperty().value()
                ),  
                ex
            );
        }
    }

    private List<?> convertValuesToListType(
            ResolvedPropertyConversionHandlerContext context,
            String[] values,
            Type listGenericTypeParameter
    ) {
        ResolvedPropertyConverter resolvedPropertyConverter = 
            context.resolvedPropertyConverter();

        Class<?> converterExpectedType = TypeUtilities.getRawType(listGenericTypeParameter);
        List<Type> converterExpectedTypeGenericTypeParameters = 
            TypeUtilities.getTypeParameters(listGenericTypeParameter);

        // Convert and return values.
        return IntStream.range(0, values.length).mapToObj(i -> {
            String value = values[i];
            return resolvedPropertyConverter.convert(
                new ResolvedPropertyConverterContext(
                    context.externalizedPropertyMethodInfo(),
                    ResolvedProperty.with(
                        indexedName(context.resolvedProperty().name(), i),
                        value
                    ), 
                    converterExpectedType,
                    converterExpectedTypeGenericTypeParameters
                )
            );
        })
        .collect(Collectors.toList());
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
