package io.github.jeyjeyemem.externalizedproperties.core.processing;

import io.github.jeyjeyemem.externalizedproperties.core.Processor;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ProcessingException;

import java.util.Base64;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Processor to apply base 64 decoding to a property.
 */
public class Base64Decode implements Processor {
    private final Base64.Decoder decoder;

    /**
     * Default constructor. This will use {@link Base64#getDecoder()} to do
     * the base 64 decoding.
     */
    public Base64Decode() {
        this(Base64.getDecoder());
    }

    /**
     * Default constructor. 
     * 
     * @param decoder The base 64 decoder to use to decode the property.
     */
    public Base64Decode(Base64.Decoder decoder) {
        this.decoder = requireNonNull(decoder, "decoder");
    }

    /** {@inheritDoc} */
    @Override
    public String processProperty(String property) {
        requireNonNull(property, "property");
        try {
            byte[] bytes = property.getBytes();
            byte[] decoded = decoder.decode(bytes);
            return new String(decoded);
        } catch (Exception ex) {
            throw new ProcessingException(
                "Exception occurred while attempting to decode property using Base64: " +
                property,
                ex
            );
        }
    }
}