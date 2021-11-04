package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Supports conversion of resolved properties to an {@link Optional}.
 */
public class OptionalPropertyConversionHandler implements ResolvedPropertyConversionHandler<Optional<?>> {
    
    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return Optional.class.equals(expectedType);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<?> convert(ResolvedPropertyConversionContext context) {
        requireNonNull(context, "context");

        try {
            Type optionalGenericTypeParameter = context.expectedTypeGenericTypeParameters()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResolvedPropertyConversionException(
                    "Optional generic type parameter is required."
                ));

            // Do not allow Optional<T>, Optional<T extends ...>, etc.
            throwIfOptionalHasTypeVariable(optionalGenericTypeParameter);

            ResolvedProperty resolvedProperty = context.resolvedProperty();
            Class<?> rawOptionalType = TypeUtilities.getRawType(optionalGenericTypeParameter);

            // If Optional<String> or Optional<Object>, return String value.
            if (String.class.equals(rawOptionalType) || Object.class.equals(rawOptionalType)) {
                return Optional.of(resolvedProperty.value());
            }

            return convertToOptionalType(
                context, 
                resolvedProperty, 
                optionalGenericTypeParameter
            );
        } catch (Exception ex) {
            throw new ResolvedPropertyConversionException(String.format(
                    "Failed to convert property %s to an Optional. Property value: %s",
                    context.resolvedProperty().name(),
                    context.resolvedProperty().value()
                ),  
                ex
            );
        }
    }

    private Optional<?> convertToOptionalType(
            ResolvedPropertyConversionContext context, 
            ResolvedProperty resolvedProperty,
            Type optionalGenericTypeParameter
    ) {
        ResolvedPropertyConverter resolvedPropertyConverter = 
            context.resolvedPropertyConverter();

        // Type parameter of the actual optional type parameter
        List<Type> genericTypeParameterOfOptionalType = 
            TypeUtilities.getTypeParameters(optionalGenericTypeParameter);

        // Convert property and wrap in Optional.
        return Optional.ofNullable(
            resolvedPropertyConverter.convert(
                new ResolvedPropertyConversionContext(
                    context.resolvedPropertyConverter(),
                    context.externalizedPropertyMethodInfo(),
                    resolvedProperty, 
                    optionalGenericTypeParameter,
                    genericTypeParameterOfOptionalType
                )
            )
        );
    }

    private void throwIfOptionalHasTypeVariable(Type optionalGenericTypeParameter) {
        if (TypeUtilities.isTypeVariable(optionalGenericTypeParameter)) {
            throw new ResolvedPropertyConversionException(
                "Type variables e.g. Optional<T> are not supported."
            );
        }
    }
}
