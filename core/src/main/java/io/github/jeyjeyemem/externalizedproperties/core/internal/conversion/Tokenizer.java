package io.github.jeyjeyemem.externalizedproperties.core.internal.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.StripEmptyValues;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;

import java.util.Arrays;
import java.util.regex.Pattern;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Tokenizer that splits {@link ConversionContext}'s value using a specified delimiter.
 */
public class Tokenizer {

    private final String delimiter;

    /**
     * Constructor.
     * 
     * @param delimiter The default delimiter to use. 
     * The delimiter value will be quoted via {@link Pattern#quote(String)}.
     */
    public Tokenizer(String delimiter) {
        this.delimiter = Pattern.quote(
            requireNonNull(delimiter, "delimiter")
        );
    }

    /** {@inheritDoc} */
    public String[] tokenizeValue(ConversionContext context) {
        ProxyMethod proxyMethod = context.proxyMethod();
        String delimiter = determineDelimiter(proxyMethod);
        String[] tokens = context.value().split(delimiter);
        return stripEmptyValuesIfNecessary(proxyMethod, tokens);
    }

    private String determineDelimiter(ProxyMethod proxyMethod) {
        Delimiter delimiterOverride = 
            proxyMethod.findAnnotation(Delimiter.class).orElse(null);

        return delimiterOverride != null ? 
            Pattern.quote(delimiterOverride.value()) : 
            delimiter;
    }

    private String[] stripEmptyValuesIfNecessary(
            ProxyMethod proxyMethod, 
            String[] tokens
    ) {
        if (proxyMethod.hasAnnotation(StripEmptyValues.class)) {
            // Filter empty values.
            return Arrays.stream(tokens)
                .filter(v -> !v.isEmpty())
                .toArray(String[]::new);
        }
        return tokens;
    }
}
