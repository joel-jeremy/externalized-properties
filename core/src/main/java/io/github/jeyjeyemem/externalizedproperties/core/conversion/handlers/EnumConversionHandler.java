package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Supports conversion of values to an enum.
 */
public class EnumConversionHandler<T extends Enum<T>> implements ConversionHandler<T> {

    private final Class<T> enumClass;

    /**
     * Constructor.
     * 
     * @param enumClass The enum class to convert resolved properties to.
     */
    public EnumConversionHandler(Class<T> enumClass) {
        this.enumClass = Arguments.requireNonNull(enumClass, "enumClass");
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return enumClass.equals(expectedType);
    }

    /** {@inheritDoc} */
    @Override
    public T convert(ConversionContext context) {
        return convertInternal(context);
    }

    /** {@inheritDoc} */
    @Override
    public T convert(PropertyMethodConversionContext context) {
        return convertInternal(context);
    }

    private T convertInternal(ConversionContext context) {
        requireNonNull(context, "context");

        try {
            return Enum.valueOf(enumClass, context.value());
        } catch (Exception ex) {
            throw new ConversionException(String.format(
                    "Failed to convert value to an enum of type %s: %s",
                    enumClass,
                    context.value()
                ),
                ex
            );
        }
    }
}
