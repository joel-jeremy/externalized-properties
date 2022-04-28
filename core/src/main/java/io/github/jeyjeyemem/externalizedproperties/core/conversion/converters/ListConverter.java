package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ConverterProvider;
import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.StripEmptyValues;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.Tokenizer;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;

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
    private final Converter<?> rootConverter;

    /**
     * Default constructor. 
     * Instances constructed via this constructor will use {@link ArrayList} 
     * as {@link List} or {@link Collection} implementation.
     * 
     * @param rootConverter The root converter.
     */
    public ListConverter(Converter<?> rootConverter) {
        this(rootConverter, ArrayList::new);
    }

    /**
     * Constructor.
     * 
     * @param rootConverter The root converter.
     * @param listFactory The list factory. This must return a list instance
     * (optionally with given the length). This function must not return null.
     */
    public ListConverter(
            Converter<?> rootConverter,
            IntFunction<List<?>> listFactory
    ) {
        this.rootConverter = requireNonNull(rootConverter, "rootConverter");
        this.listFactory = requireNonNull(listFactory, "listFactory");
    }

    /**
     * The {@link ConverterProvider} for {@link ListConverter}.
     * 
     * @return The {@link ConverterProvider} for {@link ListConverter}.
     */
    public static ConverterProvider<ListConverter> provider() {
        return (externalizedProperties, rootConverter) -> 
            new ListConverter(rootConverter);
    }

    /**
     * The {@link ConverterProvider} for {@link ListConverter}.
     * 
     * @param listFactory The list factory. This must return a list instance
     * (optionally with given the length). This function must not return null.
     * @return The {@link ConverterProvider} for {@link ListConverter}.
     */
    public static ConverterProvider<ListConverter> provider(
            IntFunction<List<?>> listFactory
    ) {
        requireNonNull(listFactory, "listFactory");
        return (externalizedProperties, rootConverter) -> 
            new ListConverter(rootConverter, listFactory);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return List.class.equals(targetType) ||
            Collection.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<? extends List<?>> convert(
            ProxyMethod proxyMethod,
            String valueToConvert,
            Type targetType
    ) { 
        Type[] genericTypeParams = TypeUtilities.getTypeParameters(targetType);

        // Assume initially as List<String> when target type has no
        // generic type parameters.
        Type targetListType = String.class;
        if (genericTypeParams.length > 0) {
            // Do not allow List<T>, List<T extends ...>, etc.
            targetListType = throwIfTypeVariable(genericTypeParams[0]);
        }

        if (valueToConvert.isEmpty()) {
            return ConversionResult.of(newList(0));
        }

        final String[] values = tokenizer.tokenizeValue(proxyMethod, valueToConvert);
        
        Class<?> rawTargetListType = TypeUtilities.getRawType(targetListType);

        // If List<String> or List<Object>, return String values.
        if (String.class.equals(rawTargetListType) || 
                Object.class.equals(rawTargetListType)) {
            return ConversionResult.of(newList(values));
        }

        return ConversionResult.of(
            convertValuesToListType(
                proxyMethod,
                values,
                targetListType
            )
        );
    }

    private List<?> convertValuesToListType(
            ProxyMethod proxyMethod,
            String[] values,
            Type listType
    ) {
        List<Object> convertedList = newList(values.length);
        
        for (int i = 0; i < values.length; i++) {
            ConversionResult<?> converted = rootConverter.convert(
                proxyMethod,
                values[i], 
                listType
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
