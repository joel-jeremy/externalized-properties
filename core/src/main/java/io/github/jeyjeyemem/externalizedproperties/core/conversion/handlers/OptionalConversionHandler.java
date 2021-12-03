package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.reflect.Type;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Supports conversion of values to an {@link Optional}.
 */
public class OptionalConversionHandler implements ConversionHandler<Optional<?>> {
    
    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return Optional.class.equals(expectedType);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<?> convert(ConversionContext context) {
        return convertInternal(context);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<?> convert(PropertyMethodConversionContext context) {
        return convertInternal(context);
    }

    private Optional<?> convertInternal(ConversionContext context) {
        requireNonNull(context, "context");

        try {
            Type[] genericTypeParams = context.expectedTypeGenericTypeParameters();
            if (genericTypeParams.length == 0) {
                throw new ConversionException(
                    "Optional generic type parameter is required."
                );
            }

            Type optionalGenericTypeParameter = genericTypeParams[0];

            // Do not allow Optional<T>, Optional<T extends ...>, etc.
            throwIfOptionalHasTypeVariable(optionalGenericTypeParameter);

            String value = context.value();
            Class<?> rawOptionalType = TypeUtilities.getRawType(optionalGenericTypeParameter);

            // If Optional<String> or Optional<Object>, return String value.
            if (String.class.equals(rawOptionalType) || Object.class.equals(rawOptionalType)) {
                return Optional.of(value);
            }

            return convertToOptionalType(
                context, 
                value, 
                optionalGenericTypeParameter
            );
        } catch (Exception ex) {
            throw new ConversionException(String.format(
                    "Failed to convert value to an Optional: %s",
                    context.value()
                ),  
                ex
            );
        }
    }

    private Optional<?> convertToOptionalType(
            ConversionContext context, 
            String value,
            Type optionalGenericTypeParameter
    ) {
        Converter converter = context.converter();

        // Convert property and wrap in Optional.
        return Optional.ofNullable(
            converter.convert(
                value, 
                optionalGenericTypeParameter
            )
        );
    }

    private void throwIfOptionalHasTypeVariable(Type optionalGenericTypeParameter) {
        if (TypeUtilities.isTypeVariable(optionalGenericTypeParameter)) {
            throw new ConversionException(
                "Type variables e.g. Optional<T> are not supported."
            );
        }
    }
}
