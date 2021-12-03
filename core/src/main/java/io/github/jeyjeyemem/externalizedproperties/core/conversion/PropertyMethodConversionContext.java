package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;

import java.lang.reflect.Type;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Extension of {@link ConversionContext}.
 * In addition to the information provided by {@link ConversionContext},
 * this contains some details about the externalized property method info.
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
     * @param value The value to convert.
     */
    public PropertyMethodConversionContext(
            Converter converter,
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo,
            String value
    ) {
        this(
            converter, 
            externalizedPropertyMethodInfo,
            value, 
            externalizedPropertyMethodInfo.genericReturnType()
        );
    }

    /**
     * Constructor.
     * 
     * @param converter The converter. 
     * This is here to allow for recursive conversion in {@link ConversionHandler}
     * implementations.
     * @param externalizedPropertyMethodInfo The externalized property method info.
     * @param value The value to convert.
     * @param expectedType The type to convert to.
     */
    public PropertyMethodConversionContext(
            Converter converter,
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo,
            String value,
            Type expectedType
    ) {
        super(
            converter, 
            value,
            expectedType
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
