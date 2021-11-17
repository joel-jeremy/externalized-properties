package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;

import java.lang.reflect.Type;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Context object for {@link ConversionHandler}s.
 * This contains the resolved property to be converted and the expected type
 * to which the resolved property value should be converted to. 
 * This also contains the externalized property method info and the root converter instance
 * for the specific externalized property method.
 */
public class PropertyMethodConversionContext extends ConversionContext {
    private final ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo;

    /**
     * Constructor which constructs a context object to convert to whatever the 
     * return type and return type's generic type parameter of the property method is.
     * 
     * @param converter The converter. 
     * This is here to allow for recursive conversion in {@link ConversionHandler}
     * implementations.
     * @param externalizedPropertyMethodInfo The externalized property method info.
     * @param resolvedProperty The resolved property.
     */
    public PropertyMethodConversionContext(
            Converter converter,
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo,
            ResolvedProperty resolvedProperty
    ) {
        this(
            converter, 
            externalizedPropertyMethodInfo,
            resolvedProperty, 
            externalizedPropertyMethodInfo.genericReturnType(),
            externalizedPropertyMethodInfo.genericReturnTypeGenericTypeParameters()
        );
    }

    /**
     * Constructor.
     * 
     * @param converter The converter. 
     * This is here to allow for recursive conversion in {@link ConversionHandler}
     * implementations.
     * @param externalizedPropertyMethodInfo The externalized property method info.
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
    public PropertyMethodConversionContext(
            Converter converter,
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo,
            ResolvedProperty resolvedProperty,
            Type expectedType,
            Type... expectedTypeGenericTypeParameters
    ) {
        super(
            converter, 
            resolvedProperty,
            expectedType,
            expectedTypeGenericTypeParameters
        );
        this.externalizedPropertyMethodInfo = requireNonNull(
            externalizedPropertyMethodInfo, 
            "externalizedPropertyMethodInfo"
        );
    }

    /**
     * The externalized property method info.
     * 
     * @return The externalized property method info.
     */
    public ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo() {
        return externalizedPropertyMethodInfo;
    }
}
