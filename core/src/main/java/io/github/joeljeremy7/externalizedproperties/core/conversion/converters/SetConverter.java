package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.TypeUtilities;
import io.github.joeljeremy7.externalizedproperties.core.conversion.Delimiter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.StripEmptyValues;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;

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
    private final SetFactory setFactory;
    /** Internal array converter. */
    private final ArrayConverter arrayConverter;

    /**
     * Default constructor. 
     * Instances constructed via this constructor will use {@link LinkedHashSet} 
     * as {@link Set} implementation.
     * 
     * @param rootConverter The root converter.
     */
    public SetConverter(Converter<?> rootConverter) {
        // Prevent hashmap resizing.
        // 0.75 is HashMap's default load factor.
        this(rootConverter, size -> new LinkedHashSet<>((int) (size/0.75f) + 1));
    }

    /**
     * Constructor.
     * 
     * @param rootConverter The root converter.
     * @param setFactory The {@link Set} factory. This must return a mutable {@link Set} 
     * instance (optionally with given the capacity). This function must not return null.
     */
    public SetConverter(
            Converter<?> rootConverter,
            SetFactory setFactory
    ) {
        this.arrayConverter = new ArrayConverter(
            requireNonNull(rootConverter, "rootConverter")
        );
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
     * @param setFactory The {@link Set} factory. This must return a mutable {@link Set} 
     * instance (optionally with given the capacity). This function must not return null.
     * 
     * @return The {@link ConverterProvider} for {@link SetConverter}.
     */
    public static ConverterProvider<SetConverter> provider(
            SetFactory setFactory
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
        if (valueToConvert.isEmpty()) {
            return ConversionResult.of(newSet(0));
        }

        GenericArrayType targetArrayType = toTargetArrayType(targetType);

        Object[] array = arrayConverter.convert(
            proxyMethod, 
            valueToConvert,
            targetArrayType
        ).value();

        return ConversionResult.of(newSet(array));
    }

    private Set<Object> newSet(int capacity) {
        @SuppressWarnings("unchecked")
        Set<Object> set = (Set<Object>)setFactory.newSet(capacity);
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

    /**
     * Convert target type to an array type such that:
     * <ul>
     *  <li>{@code Set<String>} becomes {@code String[]}</li>
     *  <li>{@code Set<Integer>} becomes {@code Integer[]}</li>
     *  <li>{@code Set<Optional<Integer>>} becomes {@code Optional<Integer>[]}</li>
     * </ul>
     * 
     * @param targetType The target type.
     * @return The array target type to pass to {@link ArrayConverter} when requesting 
     * to convert to an array.
     */
    private static GenericArrayType toTargetArrayType(Type targetType) {
        Type[] genericTypeParams = TypeUtilities.getTypeParameters(targetType);

        // Assume as Set<String> when target type has no generic type parameters.
        final Type targetSetType;
        if (genericTypeParams.length > 0) {
            targetSetType = genericTypeParams[0];
        } else {
            targetSetType = String.class;
        }

        return new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return targetSetType;
            }
        };
    }

    /**
     * Set factory.
     */
    public static interface SetFactory {
        /**
         * Create a new mutable {@link Set} instance (optionally with given the capacity). 
         * This function must not return null.
         * 
         * @param capacity The requested capacity of the {@link Set}.
         * @return A new mutable {@link Set} instance (optionally with given the capacity).
         */
        Set<?> newSet(int capacity);
    }
}
