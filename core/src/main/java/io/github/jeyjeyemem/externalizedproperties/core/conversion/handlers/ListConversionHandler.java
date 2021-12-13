package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.StripEmptyValues;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.IntFunction;
import java.util.regex.Pattern;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion of values to a {@link List} or {@link Collection}.
 * 
 * @implNote By default, this uses ',' as default delimiter when splitting values.
 * This can overriden by annotating the proxy method with {@link Delimiter} annotation
 * in which case the {@link Delimiter#value()} attribute will be used as the delimiter. 
 * 
 * @implNote If stripping of empty values from the list/collection is desired, 
 * the proxy method can be annotated with the {@link StripEmptyValues} annotation.  
 */
public class ListConversionHandler implements ConversionHandler<List<?>> {
    private static final String DEFAULT_DELIMITER = ",";
    private final IntFunction<List<?>> listFactory;

    /**
     * Default constructor. 
     * This uses {@link ArrayList} as {@link List} or {@link Collection} implementation.
     */
    public ListConversionHandler() {
        this(ArrayList::new);
    }

    /**
     * Constructor.
     * 
     * @param listFactory The list factory. This must return a list instance
     * given a length. This must not return null.
     */
    public ListConversionHandler(IntFunction<List<?>> listFactory) {
        this.listFactory = requireNonNull(listFactory, "listFactory");
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return List.class.equals(targetType) ||
            Collection.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public List<?> convert(ConversionContext context) {
        requireNonNull(context, "context");
        
        try {
            Type[] genericTypeParams = context.targetTypeGenericTypeParameters();

            // Assume initially as List of strings.
            Type targetListType = String.class;
            if (genericTypeParams.length > 0) {
                // Do not allow List<T>, List<T extends ...>, etc.
                targetListType = throwIfListHasTypeVariable(genericTypeParams[0]);
            }

            String propertyValue = context.value();
            if (propertyValue.isEmpty()) {
                return newList(0);
            }

            final String[] values = getValues(context);
            
            Class<?> rawTargetListType = TypeUtilities.getRawType(targetListType);

            // If List<String> or List<Object>, return String values.
            if (String.class.equals(rawTargetListType) || 
                    Object.class.equals(rawTargetListType)) {
                return newList(values);
            }

            return convertValuesToListType(
                context,
                values,
                targetListType
            );
        } catch (Exception ex) {
            throw new ConversionException(
                String.format(
                    "Failed to convert value to a List/Collection: %s",
                    context.value()
                ),  
                ex
            );
        }
    }

    private List<?> convertValuesToListType(
            ConversionContext context,
            String[] values,
            Type listType
    ) {
        List<Object> convertedList = newList(values.length);

        for (int i = 0; i < values.length; i++) {
            convertedList.add(context.converter().convert(
                context.with(listType, values[i])
            ));
        }

        return convertedList;
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

    private List<Object> newList(int length) {
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>)listFactory.apply(length);
        if (list == null) {
            throw new IllegalStateException(
                "List factory implementation must not return null."
            );
        }
        return list;
    }

    private List<Object> newList(Object[] values) {
        List<Object> list = newList(values.length);
        for (Object value : values) {
            list.add(value);
        }
        return list;
    }

    private Type throwIfListHasTypeVariable(Type listGenericTypeParameter) {
        if (TypeUtilities.isTypeVariable(listGenericTypeParameter)) {
            throw new ConversionException(
                "Type variables e.g. List<T> are not supported."
            );
        }

        return listGenericTypeParameter;
    }
}
