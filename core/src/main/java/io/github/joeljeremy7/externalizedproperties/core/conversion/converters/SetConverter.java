package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.TypeUtilities;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.conversion.Delimiter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.StripEmptyValues;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.Tokenizer;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntFunction;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion of values to a {@link Set} instance.
 * 
 * @apiNote By default, this uses ',' as default delimiter when splitting resolved property values.
 * This can overriden by annotating the proxy interface method with {@link Delimiter} annotation
 * in which case the {@link Delimiter#value()} attribute will be used as the delimiter.
 * 
 * @apiNote If stripping of empty values from the array is desired, 
 * the proxy interface method can be annotated with the {@link StripEmptyValues} annotation.  
 */
public class SetConverter implements Converter<Set<?>> {
    private final IntFunction<Set<?>> setFactory;
    private final Tokenizer tokenizer = new Tokenizer(",");
    private final Converter<?> rootConverter;

    /**
     * Default constructor. 
     * Instances constructed via this constructor will use {@link HashSet} 
     * as {@link Set} implementation.
     * 
     * @param rootConverter The root converter.
     */
    public SetConverter(Converter<?> rootConverter) {
        // Prevent hashmap resizing.
        // 0.75 is HashMap's default load factor.
        this(rootConverter, size -> new HashSet<>((int) (size/0.75f) + 1));
    }

    /**
     * Constructor.
     * 
     * @param rootConverter The root converter.
     * @param setFactory The set factory. This must return a set instance
     * (optionally with given the length). This function must not return null.
     */
    public SetConverter(
            Converter<?> rootConverter,
            IntFunction<Set<?>> setFactory
    ) {
        this.rootConverter = requireNonNull(rootConverter, "rootConverter");
        this.setFactory = requireNonNull(setFactory, "setFactory");
    }

    /**
     * The {@link ConverterProvider} for {@link SetConverter}.
     * 
     * @return The {@link ConverterProvider} for {@link SetConverter}.
     */
    public static ConverterProvider<SetConverter> provider() {
        return (externalizedProperties, rootConverter) -> 
            new SetConverter(rootConverter);
    }

    /**
     * The {@link ConverterProvider} for {@link SetConverter}.
     * 
     * @param setFactory The set factory. This must return a set instance
     * (optionally with given the length). This function must not return null.
     * @return The {@link ConverterProvider} for {@link SetConverter}.
     */
    public static ConverterProvider<SetConverter> provider(
            IntFunction<Set<?>> setFactory
    ) {
        requireNonNull(setFactory, "setFactory");
        return (externalizedProperties, rootConverter) -> 
            new SetConverter(rootConverter, setFactory);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return Set.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<? extends Set<?>> convert(
            ProxyMethod proxyMethod,
            String valueToConvert,
            Type targetType
    ) { 
        Type[] genericTypeParams = TypeUtilities.getTypeParameters(targetType);

        // Assume initially as Set<String> when target type has no
        // generic type parameters.
        Type targetSetType = String.class;
        if (genericTypeParams.length > 0) {
            // Do not allow Set<T>, Set<T extends ...>, etc.
            targetSetType = throwIfTypeVariable(genericTypeParams[0]);
        }

        if (valueToConvert.isEmpty()) {
            return ConversionResult.of(newSet(0));
        }

        final String[] values = tokenizer.tokenizeValue(proxyMethod, valueToConvert);
        
        Class<?> rawTargetSetType = TypeUtilities.getRawType(targetSetType);

        // If Set<String> or Set<Object>, return String values.
        if (String.class.equals(rawTargetSetType) || 
                Object.class.equals(rawTargetSetType)) {
            return ConversionResult.of(newSet(values));
        }

        return ConversionResult.of(
            convertValuesToSetType(
                proxyMethod,
                values,
                targetSetType
            )
        );
    }

    private Set<?> convertValuesToSetType(
            ProxyMethod proxyMethod,
            String[] values,
            Type setType
    ) {
        Set<Object> convertedSet = newSet(values.length);
        
        for (int i = 0; i < values.length; i++) {
            ConversionResult<?> converted = rootConverter.convert(
                proxyMethod,
                values[i], 
                setType
            );
            convertedSet.add(converted.value());
        }

        return convertedSet;
    }

    private Set<Object> newSet(int length) {
        @SuppressWarnings("unchecked")
        Set<Object> set = (Set<Object>)setFactory.apply(length);
        if (set == null) {
            throw new IllegalStateException(
                "Set factory implementation must not return null."
            );
        }
        return set;
    }

    private Set<Object> newSet(Object[] values) {
        Set<Object> set = newSet(values.length);
        for (Object value : values) {
            set.add(value);
        }
        return set;
    }

    private Type throwIfTypeVariable(Type setGenericTypeParameter) {
        if (TypeUtilities.isTypeVariable(setGenericTypeParameter)) {
            throw new ConversionException(
                "Type variables e.g. Set<T> are not supported."
            );
        }
        return setGenericTypeParameter;
    }
}
