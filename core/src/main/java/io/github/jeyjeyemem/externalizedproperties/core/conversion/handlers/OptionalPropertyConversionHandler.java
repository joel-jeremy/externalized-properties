package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverterContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
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
                .orElseThrow(() -> new IllegalStateException(
                    "Whut? Optional has no generic type parameter? Improssibru!")
                );
            
            // If Optional<String>, Optional<Object> or Optional<?>, return String value.
            if (String.class.equals(optionalGenericTypeParameter) ||
                    Object.class.equals(optionalGenericTypeParameter) ||
                    optionalGenericTypeParameter instanceof WildcardType) {
                return Optional.of(resolvedProperty.value());
            }

            ResolvedPropertyConverter resolvedPropertyConverter = 
                context.resolvedPropertyConverter();

            Class<?> converterExpectedType = determineExpectedType(optionalGenericTypeParameter);
            Type[] converterExpectedTypeGenericTypeParameters = 
                determineGenericParameterTypes(optionalGenericTypeParameter);

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

    private Class<?> determineExpectedType(Type optionalGenericTypeParameter) {
        if (optionalGenericTypeParameter instanceof Class<?>) {
            return (Class<?>)optionalGenericTypeParameter;
        } 
        else if (optionalGenericTypeParameter instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)optionalGenericTypeParameter;
            return (Class<?>)pt.getRawType();
        } 
        else if (optionalGenericTypeParameter instanceof TypeVariable<?>) {
            throw new ResolvedPropertyConversionException(
                "Type variables in list's generic parameter type are not supported."
            );
        }
        else {
            throw new ResolvedPropertyConversionException(
                "Could not determine list's generic parameter type."
            );
        }
    }

    private Type[] determineGenericParameterTypes(Type optionalGenericTypeParameter) {
        if (optionalGenericTypeParameter instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)optionalGenericTypeParameter;
            return pt.getActualTypeArguments();
        }

        return new Type[0];
    }
    
}
