package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.StripEmptyValues;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
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
public class CollectionConversionHandler implements ConversionHandler<List<?>> {
    private static final String DEFAULT_DELIMITER = ",";

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return List.class.equals(expectedType) ||
            Collection.class.equals(expectedType);
    }

    /** {@inheritDoc} */
    @Override
    public List<?> convert(ConversionContext context) {
        requireNonNull(context, "context");
        return convertInternal(context, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<?> convert(PropertyMethodConversionContext context) {
        requireNonNull(context, "context");
        return convertInternal(context, context.externalizedPropertyMethodInfo());
    }

    private List<?> convertInternal(
            ConversionContext context,
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo
    ) {
        try {
            Type[] genericTypeParams = context.expectedTypeGenericTypeParameters();
            if (genericTypeParams.length == 0) {
                throw new ConversionException(
                    "List generic type parameter is required."
                );
            }

            Type listGenericTypeParameter = genericTypeParams[0];

            // Do not allow List<T>, List<T extends ...>, etc.
            throwIfListHasTypeVariable(listGenericTypeParameter);

            String propertyValue = context.resolvedProperty().value();
            if (propertyValue.isEmpty()) {
                return Collections.emptyList();
            }

            final String[] values = getValues(context, externalizedPropertyMethodInfo);
            
            Class<?> rawListType = TypeUtilities.getRawType(listGenericTypeParameter);

            // If List<String> or List<Object>, return String values.
            if (String.class.equals(rawListType) || Object.class.equals(rawListType)) {
                return Arrays.asList(values);
            }

            Type[] genericTypeParameterOfListType = 
                TypeUtilities.getTypeParameters(listGenericTypeParameter);

            return convertValuesToListType(
                context, 
                values, 
                listGenericTypeParameter,
                genericTypeParameterOfListType
            );
        } catch (Exception ex) {
            throw new ConversionException(
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
            ConversionContext context,
            String[] values,
            Type listType,
            Type[] listTypeGenericTypeParameters
    ) {
        Converter converter = context.converter();

        // Convert and return values.
        return IntStream.range(0, values.length).mapToObj(i -> {
            String value = values[i];
            return converter.convert(
                new ConversionContext(
                    converter,
                    ResolvedProperty.with(
                        indexedName(context.resolvedProperty().name(), i),
                        value
                    ), 
                    listType,
                    listTypeGenericTypeParameters
                )
            );
        })
        .collect(Collectors.toList());
    }

    private String[] getValues(
            ConversionContext context, 
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo
    ) {
        String propertyValue = context.resolvedProperty().value();

        if (externalizedPropertyMethodInfo != null) {
            // Determine delimiter.
            String delimiter = externalizedPropertyMethodInfo.findAnnotation(Delimiter.class)
                .map(d -> d.value())
                .orElse(DEFAULT_DELIMITER);

            if (externalizedPropertyMethodInfo.hasAnnotation(StripEmptyValues.class)) {
                // Filter empty values.
                return Arrays.stream(propertyValue.split(delimiter))
                    .filter(v -> !v.isEmpty())
                    .toArray(String[]::new);
            }
            return propertyValue.split(Pattern.quote(delimiter));
        }

        return propertyValue.split(DEFAULT_DELIMITER);
    }

    private static String indexedName(String name, int index) {
        return name + "[" + index + "]";
    }

    private void throwIfListHasTypeVariable(Type listGenericTypeParameter) {
        if (TypeUtilities.isTypeVariable(listGenericTypeParameter)) {
            throw new ConversionException(
                "Type variables e.g. List<T> are not supported."
            );
        }
    }
}
