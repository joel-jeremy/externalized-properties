package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;

import java.lang.reflect.Type;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion of values to an {@link Optional}.
 */
public class OptionalConversionHandler implements ConversionHandler<Optional<?>> {
    
    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return Optional.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Optional<?>> convert(ConversionContext context) {
        requireNonNull(context, "context");

        try {
            Type[] genericTypeParams = context.targetTypeGenericTypeParameters();
            
            // Assume initially as Optional of string type.
            Type targetOptionalType = String.class;
            if (genericTypeParams.length > 0) {
                // Do not allow Optional<T>, Optional<T extends ...>, etc.
                targetOptionalType = throwIfOptionalHasTypeVariable(genericTypeParams[0]);
            }

            String value = context.value();
            Class<?> rawTargetOptionalType = TypeUtilities.getRawType(targetOptionalType);

            // If Optional<String> or Optional<Object>, return String value.
            if (String.class.equals(rawTargetOptionalType) || 
                    Object.class.equals(rawTargetOptionalType)) {
                return ConversionResult.of(Optional.of(value));
            }

            return ConversionResult.of(
                convertToOptionalType(
                    context,
                    value,
                    targetOptionalType
                )
            );
        } catch (Exception ex) {
            throw new ConversionException(String.format(
                    "Failed to convert value to %s type: %s",
                    context.rawTargetType(),
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
        // Convert property and wrap in Optional.
        return Optional.ofNullable(context.converter().convert(
            context.with(optionalGenericTypeParameter, value)
        ));
    }

    private Type throwIfOptionalHasTypeVariable(Type optionalGenericTypeParameter) {
        if (TypeUtilities.isTypeVariable(optionalGenericTypeParameter)) {
            throw new ConversionException(
                "Type variables e.g. Optional<T> are not supported."
            );
        }

        return optionalGenericTypeParameter;
    }
}
