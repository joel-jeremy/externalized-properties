package io.github.jeyjeyemem.externalizedproperties.core.processing;

import io.github.jeyjeyemem.externalizedproperties.core.ProcessingContext;
import io.github.jeyjeyemem.externalizedproperties.core.Processor;

import java.util.Base64;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Processor to apply base 64 decoding to a property.
 */
public class Base64DecodeProcessor implements Processor {
    private final Base64.Decoder decoder;

    /**
     * Default constructor. This will use {@link Base64#getDecoder()} to do
     * the base 64 decoding.
     */
    public Base64DecodeProcessor() {
        this(Base64.getDecoder());
    }

    /**
     * Default constructor. 
     * 
     * @param decoder The base 64 decoder to use to decode the property.
     */
    public Base64DecodeProcessor(Base64.Decoder decoder) {
        this.decoder = requireNonNull(decoder, "decoder");
    }

    /** {@inheritDoc} */
    @Override
    public String process(ProcessingContext context) {
        requireNonNull(context, "context");

        try {
            byte[] bytes = context.value().getBytes();
            byte[] decoded = decoder.decode(bytes);
            return new String(decoded);
        } catch (Exception ex) {
            throw new ProcessingException(
                "Exception occurred while attempting to decode value using Base64: " +
                context.value(),
                ex
            );
        }
    }
}