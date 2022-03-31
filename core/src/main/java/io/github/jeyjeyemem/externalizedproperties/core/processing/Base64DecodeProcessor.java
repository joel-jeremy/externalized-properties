package io.github.jeyjeyemem.externalizedproperties.core.processing;

import io.github.jeyjeyemem.externalizedproperties.core.ProcessingContext;
import io.github.jeyjeyemem.externalizedproperties.core.Processor;

import java.util.Base64;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Processor to apply base 64 decoding to a property.
 */
public class Base64DecodeProcessor implements Processor {
    private final Base64.Decoder defaultDecoder;

    /**
     * Default constructor. This will by default use {@link Base64#getDecoder()} 
     * to do the base 64 decoding.
     */
    public Base64DecodeProcessor() {
        this(Base64.getDecoder());
    }

    /**
     * Constructor. 
     * 
     * @param defaultDecoder The default base 64 decoder to use to decode the property.
     */
    public Base64DecodeProcessor(Base64.Decoder defaultDecoder) {
        this.defaultDecoder = requireNonNull(defaultDecoder, "decoder");
    }

    /** {@inheritDoc} */
    @Override
    public String process(ProcessingContext context) {
        requireNonNull(context, "context");

        try {
            byte[] bytes = context.value().getBytes();
            Base64.Decoder decoderToUse = determineDecoder(context);
            byte[] decoded = decoderToUse.decode(bytes);
            return new String(decoded);
        } catch (Exception ex) {
            throw new ProcessingException(
                "Exception occurred while attempting to decode value using Base64: " +
                context.value(),
                ex
            );
        }
    }

    private Base64.Decoder determineDecoder(ProcessingContext context) {
        String encoding = context.proxyMethod().findAnnotation(Base64Decode.class)
            .map(b64 -> b64.encoding())
            .orElse("");

        Base64.Decoder decoderToUse = this.defaultDecoder;
        if ("url".equalsIgnoreCase(encoding)) {
            decoderToUse = Base64.getUrlDecoder();
        } else if ("mime".equalsIgnoreCase(encoding)) {
            decoderToUse = Base64.getMimeDecoder();
        } else if ("basic".equalsIgnoreCase(encoding)) {
            decoderToUse = Base64.getDecoder();
        }
        return decoderToUse;
    }
}