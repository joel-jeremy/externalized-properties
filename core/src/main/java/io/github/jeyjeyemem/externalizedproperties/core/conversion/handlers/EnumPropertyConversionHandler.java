package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Supports conversion of resolved properties to an enum.
 */
public class EnumPropertyConversionHandler<T extends Enum<T>> implements ResolvedPropertyConversionHandler<T> {

    private final Class<T> enumClass;

    /**
     * Constructor.
     * 
     * @param enumClass The enum class to convert resolved properties to.
     */
    public EnumPropertyConversionHandler(Class<T> enumClass) {
        this.enumClass = Arguments.requireNonNull(enumClass, "enumClass");
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return enumClass.equals(expectedType);
    }

    /** {@inheritDoc} */
    @Override
    public T convert(ResolvedPropertyConversionContext context) {
        requireNonNull(context, "context");

        try {
            return Enum.valueOf(enumClass, context.resolvedProperty().value());
        } catch (Exception ex) {
            throw new ResolvedPropertyConversionException(String.format(
                    "Failed to convert property %s to an enum of type %s. Property value: %s",
                    context.resolvedProperty().name(),
                    enumClass,
                    context.resolvedProperty().value()
                ),
                ex
            );
        }
    }
    
}
