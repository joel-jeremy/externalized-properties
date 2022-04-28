package io.github.joeljeremy7.externalizedproperties.core.processing;

import io.github.joeljeremy7.externalizedproperties.core.Processor;
import io.github.joeljeremy7.externalizedproperties.core.ProcessorProvider;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.nio.charset.Charset;
import java.util.Base64;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

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
        this.defaultDecoder = requireNonNull(defaultDecoder, "defaultDecoder");
    }

    /**
     * The {@link ProcessorProvider} for {@link Base64DecodeProcessor}.
     * 
     * @return The {@link ProcessorProvider} for {@link Base64DecodeProcessor}.
     */
    public static ProcessorProvider<Base64DecodeProcessor> provider() {
        return externalizedProperties -> new Base64DecodeProcessor();
    }

    /** {@inheritDoc} */
    @Override
    public String process(ProxyMethod proxyMethod, String valueToProcess) {
        try {
            byte[] bytes = valueToProcess.getBytes(Charset.defaultCharset());
            Base64.Decoder decoderToUse = determineDecoder(proxyMethod);
            Charset charset = determineCharset(proxyMethod);
            byte[] decoded = decoderToUse.decode(bytes);
            return new String(decoded, charset);
        } catch (RuntimeException ex) {
            throw new ProcessingException(
                "Exception occurred while attempting to decode value using Base64: " +
                valueToProcess,
                ex
            );
        }
    }

    private Charset determineCharset(ProxyMethod proxyMethod) {
        return proxyMethod.findAnnotation(Base64Decode.class)
            .filter(b64 -> !b64.charset().isEmpty())
            .map(b64 -> Charset.forName(b64.charset()))
            .orElse(Charset.defaultCharset());
    }

    private Base64.Decoder determineDecoder(ProxyMethod proxyMethod) {
        String encoding = proxyMethod.findAnnotation(Base64Decode.class)
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