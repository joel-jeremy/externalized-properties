package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;

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
public class ResolvedPropertyConversionHandlerContext {
    private final ResolvedPropertyConverter resolvedPropertyConverter;
    private final ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo;
    private final ResolvedProperty resolvedProperty;
    private final Class<?> expectedType;
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
    public ResolvedPropertyConversionHandlerContext(
            ResolvedPropertyConverter resolvedPropertyConverter,
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo,
            ResolvedProperty resolvedProperty
    ) {
        this(
            resolvedPropertyConverter, 
            externalizedPropertyMethodInfo,
            resolvedProperty, 
            externalizedPropertyMethodInfo.returnType(), 
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
     * @param expectedType The type to convert to. This could be different from the return type of 
     * the property method.
     * @param expectedTypeGenericTypeParameters The generic type parameters of the class returned by 
     * {@link #expectedType()}, if there are any. 
     * 
     * <p>For example, if {@link #expectedType()} is a parameterized type e.g. {@code List<String>}, 
     * this should contain a {@code String} type/class.
     * 
     * <p>Another example is if {@link #expectedType()} is an array with a generic component type
     * e.g. {@code Optional<Integer>[]}, this should contain an {@code Integer} type/class.
     * 
     * <p>It is also possible for {@link #expectedType()} to be a parameterized type which contains
     * another parameterized type parameter e.g. {@code Optional<List<String>>}, in this case, 
     * this should contain a {@code List<String>} parameterized type.
     */
    public ResolvedPropertyConversionHandlerContext(
            ResolvedPropertyConverter resolvedPropertyConverter,
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo,
            ResolvedProperty resolvedProperty, 
            Class<?> expectedType,
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
     * @param expectedType The type to convert to. This could be different from the return type of 
     * the property method.
     * @param expectedTypeGenericTypeParameters The generic type parameters of the class returned by 
     * {@link #expectedType()}, if there are any. 
     * 
     * <p>For example, if {@link #expectedType()} is a parameterized type e.g. {@code List<String>}, 
     * this should contain a {@code String} type/class.
     * 
     * <p>Another example is if {@link #expectedType()} is an array with a generic component type
     * e.g. {@code Optional<Integer>[]}, this should contain an {@code Integer} type/class.
     * 
     * <p>It is also possible for {@link #expectedType()} to be a parameterized type which contains
     * another parameterized type parameter e.g. {@code Optional<List<String>>}, in this case, 
     * this should contain a {@code List<String>} parameterized type.
     */
    public ResolvedPropertyConversionHandlerContext(
            ResolvedPropertyConverter resolvedPropertyConverter,
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo,
            ResolvedProperty resolvedProperty, 
            Class<?> expectedType,
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
     * The type to convert resolved property to.
     * 
     * @return The type to convert resolved property to.
     */
    public Class<?> expectedType() {
        return expectedType;
    }

    /**
     * The generic type parameters of the class returned by {@link #expectedType()}, if there are any.
     * Otherwise, this shall return an empty list.
     * 
     * @return The type parameters of the class returned by {@link #expectedType()}, 
     * if there are any. Otherwise, this shall return an empty list.
     */
    public List<Type> expectedTypeGenericTypeParameters() {
        return expectedTypeGenericTypeParameters;
    }
}
