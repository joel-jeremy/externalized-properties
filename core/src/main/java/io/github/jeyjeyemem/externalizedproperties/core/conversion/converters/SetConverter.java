package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.StripEmptyValues;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.Tokenizer;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.function.IntFunction;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

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

    /**
     * Default constructor. 
     * Instances constructed via this constructor will use {@link HashSet} 
     * as {@link Set} implementation.
     */
    public SetConverter() {
        // Prevent hashmap resizing.
        // 0.75 is HashMap's default load factor.
        this(size -> new HashSet<>((int) (size/0.75f) + 1));
    }

    /**
     * Constructor.
     * 
     * @param setFactory The set factory. This must return a set instance
     * (optionally with given the length). This function must not return null.
     */
    public SetConverter(IntFunction<Set<?>> setFactory) {
        this.setFactory = requireNonNull(setFactory, "setFactory");
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return Set.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<? extends Set<?>> convert(ConversionContext context) {
        requireNonNull(context, "context");
        
        Type[] genericTypeParams = context.targetTypeGenericTypeParameters();

        // Assume initially as Set<String> when target type has no
        // generic type parameters.
        Type targetSetType = String.class;
        if (genericTypeParams.length > 0) {
            // Do not allow Set<T>, Set<T extends ...>, etc.
            targetSetType = throwIfTypeVariable(genericTypeParams[0]);
        }

        String propertyValue = context.value();
        if (propertyValue.isEmpty()) {
            return ConversionResult.of(newSet(0));
        }

        final String[] values = tokenizer.tokenizeValue(context);
        
        Class<?> rawTargetSetType = TypeUtilities.getRawType(targetSetType);

        // If Set<String> or Set<Object>, return String values.
        if (String.class.equals(rawTargetSetType) || 
                Object.class.equals(rawTargetSetType)) {
            return ConversionResult.of(newSet(values));
        }

        return ConversionResult.of(
            convertValuesToSetType(
                context,
                values,
                targetSetType
            )
        );
    }

    private Set<?> convertValuesToSetType(
            ConversionContext context,
            String[] values,
            Type setType
    ) {
        Set<Object> convertedSet = newSet(values.length);

        for (int i = 0; i < values.length; i++) {
            ConversionResult<?> converted = context.converter().convert(
                context.with(values[i], setType)
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
