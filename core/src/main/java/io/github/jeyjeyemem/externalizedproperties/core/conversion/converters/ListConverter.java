package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.StripEmptyValues;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.Tokenizer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntFunction;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion of values to a {@link List} or {@link Collection} instance.
 * 
 * @apiNote By default, this uses ',' as default delimiter when splitting resolved property values.
 * This can overriden by annotating the proxy interface method with {@link Delimiter} annotation
 * in which case the {@link Delimiter#value()} attribute will be used as the delimiter.
 * 
 * @apiNote If stripping of empty values from the array is desired, 
 * the proxy interface method can be annotated with the {@link StripEmptyValues} annotation.  
 */
public class ListConverter implements Converter<List<?>> {
    private final IntFunction<List<?>> listFactory;
    private final Tokenizer tokenizer = new Tokenizer(",");

    /**
     * Default constructor. 
     * Instances constructed via this constructor will use {@link ArrayList} 
     * as {@link List} or {@link Collection} implementation.
     */
    public ListConverter() {
        this(ArrayList::new);
    }

    /**
     * Constructor.
     * 
     * @param listFactory The list factory. This must return a list instance
     * (optionally with given the length). This function must not return null.
     */
    public ListConverter(IntFunction<List<?>> listFactory) {
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
    public ConversionResult<? extends List<?>> convert(ConversionContext context) {
        requireNonNull(context, "context");
        
        Type[] genericTypeParams = context.targetTypeGenericTypeParameters();

        // Assume initially as List<String> when target type has no
        // generic type parameters.
        Type targetListType = String.class;
        if (genericTypeParams.length > 0) {
            // Do not allow List<T>, List<T extends ...>, etc.
            targetListType = throwIfTypeVariable(genericTypeParams[0]);
        }

        String propertyValue = context.value();
        if (propertyValue.isEmpty()) {
            return ConversionResult.of(newList(0));
        }

        final String[] values = tokenizer.tokenizeValue(context);
        
        Class<?> rawTargetListType = TypeUtilities.getRawType(targetListType);

        // If List<String> or List<Object>, return String values.
        if (String.class.equals(rawTargetListType) || 
                Object.class.equals(rawTargetListType)) {
            return ConversionResult.of(newList(values));
        }

        return ConversionResult.of(
            convertValuesToListType(
                context,
                values,
                targetListType
            )
        );
    }

    private List<?> convertValuesToListType(
            ConversionContext context,
            String[] values,
            Type listType
    ) {
        List<Object> convertedList = newList(values.length);

        for (int i = 0; i < values.length; i++) {
            ConversionResult<?> converted = context.converter().convert(
                context.with(values[i], listType)
            );
            convertedList.add(converted.value());
        }

        return convertedList;
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

    private Type throwIfTypeVariable(Type listGenericTypeParameter) {
        if (TypeUtilities.isTypeVariable(listGenericTypeParameter)) {
            throw new ConversionException(
                "Type variables e.g. List<T> are not supported."
            );
        }
        return listGenericTypeParameter;
    }
}
