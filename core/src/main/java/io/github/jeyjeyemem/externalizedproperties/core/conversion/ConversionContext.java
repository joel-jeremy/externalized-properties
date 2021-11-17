package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.reflect.Type;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Context object for {@link Converter}/{@link ConversionHandler}s.
 * This contains the resolved property to be converted and the expected type
 * to which the resolved property value should be converted to.
 */
public class ConversionContext {
    private final Converter converter;
    private final ResolvedProperty resolvedProperty;
    private final Type expectedType;
    private final Class<?> rawExpectedType;
    private final Type[] expectedTypeGenericTypeParameters;

    /**
     * Constructor.
     * 
     * @param converter The converter. 
     * This is here to allow for recursive conversion in {@link ConversionHandler}
     * implementations.
     * @param resolvedProperty The resolved property.
     * @param expectedType The type to convert to. This could be different from the generic return type of 
     * the property method. This may be the same class as the raw expected type if the actual expected type 
     * is not a generic type. 
     * @param expectedTypeGenericTypeParameters The generic type parameters of the type returned by 
     * {@link #expectedType()}, if there are any. 
     * 
     * <p>For example, if {@link #expectedType()} is a parameterized type e.g. {@code List<String>}, 
     * this should contain a {@code String} type/class.
     * 
     * <p>It is also possible for {@link #expectedType()} to be a parameterized type which contains
     * another parameterized type e.g. {@code Optional<List<String>>}, in this case, 
     * this should contain a {@code List<String>} parameterized type.
     */
    public ConversionContext(
            Converter converter,
            ResolvedProperty resolvedProperty,
            Type expectedType,
            Type... expectedTypeGenericTypeParameters
    ) {
        this.converter = requireNonNull(
            converter, 
            "converter"
        );
        this.resolvedProperty = requireNonNull(resolvedProperty, "resolvedProperty");
        this.expectedType = requireNonNull(expectedType, "expectedType");
        this.rawExpectedType = getRawExpectedType(expectedType);
        this.expectedTypeGenericTypeParameters = requireNonNull(
            expectedTypeGenericTypeParameters, 
            "expectedTypeGenericTypeParameters"
        );
    }

    /**
     * The converter.
     * 
     * @return The converter.
     */
    public Converter converter() {
        return converter;
    }

    /**
     * The resolved property.
     * 
     * @return The resolved property.
     */
    public ResolvedProperty resolvedProperty() {
        return resolvedProperty;
    }

    /**
     * The raw type to convert resolved property to.
     * 
     * @return The raw type to convert resolved property to.
     */
    public Class<?> rawExpectedType() {
        return rawExpectedType;
    }

    /**
     * The type to convert resolved property to.
     * 
     * @return The type to convert resolved property to.
     */
    public Type expectedType() {
        return expectedType;
    }

    /**
     * The generic type parameters of the class returned by {@link #expectedType()}, if there are any.
     * Otherwise, this shall return an empty list.
     * 
     * @return The generic type parameters of the class returned by {@link #expectedType()}, 
     * if there are any. Otherwise, this shall return an empty list.
     */
    public Type[] expectedTypeGenericTypeParameters() {
        return expectedTypeGenericTypeParameters;
    }

    private Class<?> getRawExpectedType(Type type) {
        return TypeUtilities.getRawType(type);
    }
}
