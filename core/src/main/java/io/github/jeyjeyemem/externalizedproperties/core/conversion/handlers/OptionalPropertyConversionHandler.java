package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverterContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
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
    public Optional<?> convert(ResolvedPropertyConversionHandlerContext context) {
        requireNonNull(context, "context");

        try {
            ResolvedProperty resolvedProperty = context.resolvedProperty();
            Type optionalGenericTypeParameter = context.expectedTypeGenericTypeParameters()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "Optional generic type parameter is required."
                ));
            
            // If Optional<String>, Optional<Object> or Optional<?>, return String value.
            if (String.class.equals(optionalGenericTypeParameter) ||
                    Object.class.equals(optionalGenericTypeParameter) ||
                    optionalGenericTypeParameter instanceof WildcardType) {
                return Optional.of(resolvedProperty.value());
            }

            ResolvedPropertyConverter resolvedPropertyConverter = 
                context.resolvedPropertyConverter();

            Class<?> converterExpectedType = TypeUtilities.getRawType(optionalGenericTypeParameter);
            List<Type> converterExpectedTypeGenericTypeParameters = 
                TypeUtilities.getTypeParameters(optionalGenericTypeParameter);

            // Convert property and wrap in Optional.
            return Optional.ofNullable(
                resolvedPropertyConverter.convert(
                    new ResolvedPropertyConverterContext(
                        context.externalizedPropertyMethodInfo(),
                        resolvedProperty, 
                        converterExpectedType,
                        converterExpectedTypeGenericTypeParameters
                    )
                )
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

    // private Class<?> determineExpectedType(Type optionalGenericTypeParameter) {
    //     if (optionalGenericTypeParameter instanceof Class<?>) {
    //         return (Class<?>)optionalGenericTypeParameter;
    //     } 
    //     else if (optionalGenericTypeParameter instanceof ParameterizedType) {
    //         ParameterizedType pt = (ParameterizedType)optionalGenericTypeParameter;
    //         return (Class<?>)pt.getRawType();
    //     } 
    //     else if (optionalGenericTypeParameter instanceof GenericArrayType) {
    //         GenericArrayType gat = (GenericArrayType)optionalGenericTypeParameter;
    //         if (gat.getGenericComponentType() instanceof ParameterizedType) {
    //             return (Class<?>)((ParameterizedType)gat.getGenericComponentType()).getRawType();
    //         }
    //     }
    //     else if (optionalGenericTypeParameter instanceof TypeVariable<?>) {
    //         throw new ResolvedPropertyConversionException(
    //             "Type variables in optional's generic parameter type e.g. Optional<T> are not supported."
    //         );
    //     }

    //     throw new ResolvedPropertyConversionException(
    //         "Could not determine list's generic parameter type."
    //     );
    // }
}
