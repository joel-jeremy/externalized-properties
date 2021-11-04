package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Context object for {@link ResolvedPropertyConversionHandler}s.
 * This contains the resolved property to be converted and the expected type
 * to which the resolved property value should be converted to. 
 * This also contains the externalized property method info and the root converter instance
 * for the specific externalized property method.
 */
public class ResolvedPropertyConversionContext {
    private final ResolvedPropertyConverter resolvedPropertyConverter;
    private final ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo;
    private final ResolvedProperty resolvedProperty;
    private final Type expectedType;
    private final Class<?> rawExpectedType;
    private final List<Type> expectedTypeGenericTypeParameters;

    /**
     * Constructor which constructs a context object to convert to whatever the 
     * return type and return type's generic type parameter of the property method is.
     * 
     * @param resolvedPropertyConverter The resolved property converter. 
     * This is here to allow for recursive conversion in {@link ResolvedPropertyConversionHandler}
     * implementations.
     * @param externalizedPropertyMethodInfo The externalized property method info.
     * @param resolvedProperty The resolved property.
     */
    public ResolvedPropertyConversionContext(
            ResolvedPropertyConverter resolvedPropertyConverter,
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo,
            ResolvedProperty resolvedProperty
    ) {
        this(
            resolvedPropertyConverter, 
            externalizedPropertyMethodInfo,
            resolvedProperty, 
            externalizedPropertyMethodInfo.genericReturnType(),
            externalizedPropertyMethodInfo.genericReturnTypeParameters()
        );
    }

    /**
     * Constructor.
     * 
     * @param resolvedPropertyConverter The resolved property converter. 
     * This is here to allow for recursive conversion in {@link ResolvedPropertyConversionHandler}
     * implementations.
     * @param externalizedPropertyMethodInfo The externalized property method info.
     * @param resolvedProperty The resolved property.
     * @param expectedType The generic type to convert to. This could be different from the generic
     * return type of the property method. This may be the same class as the raw expected type if the 
     * actual expected type is not a generic type. 
     * @param expectedTypeGenericTypeParameters The generic type parameters of the class returned by 
     * {@link #expectedType()}, if there are any. 
     * 
     * <p>For example, if {@link #expectedType()} is a parameterized type e.g. {@code List<String>}, 
     * this should contain a {@code String} type/class.
     * 
     * <p>It is also possible for {@link #expectedType()} to be a parameterized type which contains
     * another parameterized type parameter e.g. {@code Optional<List<String>>}, in this case, 
     * this should contain a {@code List<String>} parameterized type.
     */
    public ResolvedPropertyConversionContext(
            ResolvedPropertyConverter resolvedPropertyConverter,
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo,
            ResolvedProperty resolvedProperty,
            Type expectedType,
            Type... expectedTypeGenericTypeParameters
    ) {
        this(
            resolvedPropertyConverter, 
            externalizedPropertyMethodInfo,
            resolvedProperty, 
            expectedType,
            Arrays.asList(
                requireNonNull(
                    expectedTypeGenericTypeParameters,
                    "expectedTypeGenericTypeParameters"
                )
            )
        );
    }

    /**
     * Constructor.
     * 
     * @param resolvedPropertyConverter The resolved property converter. 
     * This is here to allow for recursive conversion in {@link ResolvedPropertyConversionHandler}
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
    public ResolvedPropertyConversionContext(
            ResolvedPropertyConverter resolvedPropertyConverter,
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo,
            ResolvedProperty resolvedProperty,
            Type expectedType,
            List<Type> expectedTypeGenericTypeParameters
    ) {
        this.resolvedPropertyConverter = requireNonNull(
            resolvedPropertyConverter, 
            "resolvedPropertyConverter"
        );
        this.externalizedPropertyMethodInfo = requireNonNull(
            externalizedPropertyMethodInfo, 
            "externalizedPropertyMethodInfo"
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
     * The resolved property converter.
     * 
     * @return The resolved property converter.
     */
    public ResolvedPropertyConverter resolvedPropertyConverter() {
        return resolvedPropertyConverter;
    }

    /**
     * The externalized property method info.
     * 
     * @return The externalized property method info.
     */
    public ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo() {
        return externalizedPropertyMethodInfo;
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
    public List<Type> expectedTypeGenericTypeParameters() {
        return expectedTypeGenericTypeParameters;
    }

    private Class<?> getRawExpectedType(Type type) {
        Class<?> rawType = TypeUtilities.getRawType(type);
        if (rawType == null) {
            throw new IllegalStateException("Unable to extract raw type from type: " + type);
        }
        return rawType;
    }
}
