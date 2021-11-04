package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;

import java.util.Arrays;
import java.util.Collection;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * The default {@link ResolvedPropertyConverter} implementation.
 * This delegates to a configured collection of {@link ResolvedPropertyConversionHandler}s.
 */
public class InternalResolvedPropertyConverter implements ResolvedPropertyConverter {

    private final Collection<ResolvedPropertyConversionHandler<?>> resolvedPropertyConversionHandlers;

    /**
     * Constructor.
     * 
     * @param resolvedPropertyConversionHandlers The collection of {@link ResolvedPropertyConversionHandler}s
     * to handle the actual conversion.
     */
    public InternalResolvedPropertyConverter(
            ResolvedPropertyConversionHandler<?>... resolvedPropertyConversionHandlers
    ) {
        this(Arrays.asList(
            requireNonNull(
                resolvedPropertyConversionHandlers,
                "resolvedPropertyConversionHandlers"
            )
        ));
    }

    /**
     * Constructor.
     * 
     * @param resolvedPropertyConversionHandlers The collection of {@link ResolvedPropertyConversionHandler}s
     * to handle the actual conversion.
     */
    public InternalResolvedPropertyConverter(
            Collection<ResolvedPropertyConversionHandler<?>> resolvedPropertyConversionHandlers
    ) {
        this.resolvedPropertyConversionHandlers = requireNonNull(
            resolvedPropertyConversionHandlers, 
            "resolvedPropertyConversionHandlers"
        );
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(ResolvedPropertyConversionContext context) {
        requireNonNull(context, "context");

        ResolvedPropertyConversionHandler<?> handler = resolvedPropertyConversionHandlers.stream()
            .filter(c -> c.canConvertTo(context.rawExpectedType()))
            .findFirst()
            .orElseThrow(() -> new ResolvedPropertyConversionException(String.format(
                "No converter found to convert resolved property to expected type: %s. " +
                "Resolved property value: %s. " +
                "Externalized property method: %s.",
                context.rawExpectedType().getName(),
                context.resolvedProperty().value(), 
                context.externalizedPropertyMethodInfo().methodSignatureString()
            )));

        try {
            return handler.convert(new ResolvedPropertyConversionContext(
                this,
                context.externalizedPropertyMethodInfo(),
                context.resolvedProperty(),
                context.expectedType(),
                context.expectedTypeGenericTypeParameters()
            ));
        } catch (Exception ex) {
            throw new ResolvedPropertyConversionException(
                String.format(
                    "Exception occurred while converting resolved property to expected type: %s. " + 
                    "Resolved property value: %s. " + 
                    "Externalized property method: %s.",
                    context.rawExpectedType().getName(),
                    context.resolvedProperty().value(),
                    context.externalizedPropertyMethodInfo().methodSignatureString()
                ),
                ex
            );
        }
    }
    
}
