package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.reflect.Type;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Context object for {@link ConversionHandler}s.
 * This contains information such as the value to be converted and the expected type.
 */
public class ConversionContext {
    private final Converter converter;
    private final String value;
    private final Type expectedType;

    /**
     * Constructor.
     * 
     * @param converter The converter. 
     * This is here to allow for recursive conversion in {@link ConversionHandler}
     * implementations.
     * @param value The value to convert.
     * @param expectedType The type to convert to.
     */
    public ConversionContext(
            Converter converter,
            String value,
            Type expectedType
    ) {
        this.converter = requireNonNull(
            converter, 
            "converter"
        );
        this.value = requireNonNull(value, "value");
        this.expectedType = requireNonNull(expectedType, "expectedType");
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
     * The value to convert.
     * 
     * @return The value to convert.
     */
    public String value() {
        return value;
    }

    /**
     * The raw type to convert resolved property to.
     * 
     * @implNote This calculates raw type every time the method is called.
     * If the raw type needs to used several times, it's best to cache the
     * result of this method in a variable and use that instead.
     * 
     * @return The raw type to convert resolved property to.
     */
    public Class<?> rawExpectedType() {
        return TypeUtilities.getRawType(expectedType);
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
     * 
     * @implNote This calculates the expected type's generic type parameter every time the method 
     * is called. If the generic type parameter needs to used several times, it's best to cache the
     * result of this method in a variable and use that instead.
     * 
     * @return The generic type parameters of the class returned by {@link #expectedType()}, 
     * if there are any. Otherwise, this shall return an empty list.
     */
    public Type[] expectedTypeGenericTypeParameters() {
        return TypeUtilities.getTypeParameters(expectedType);
    }
}
