package io.github.jeyjeyemem.externalizedproperties.core.internal.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.StripEmptyValues;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;

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
        ProxyMethodInfo proxyMethodInfo = context.proxyMethodInfo().orElse(null);
        String delimiterOverride = determineDelimiter(proxyMethodInfo);
        String[] tokens = context.value().split(delimiterOverride);
        return stripEmptyValuesIfNecessary(proxyMethodInfo, tokens);
    }

    // proxyMethodInfo may be null.
    private String determineDelimiter(ProxyMethodInfo proxyMethodInfo) {
        if (proxyMethodInfo == null) {
            return delimiter;
        }

        Delimiter delimiterOverride = 
            proxyMethodInfo.findAnnotation(Delimiter.class).orElse(null);

        return delimiterOverride != null ? 
            Pattern.quote(delimiterOverride.value()) : 
            delimiter;
    }

    // proxyMethodInfo may be null.
    private String[] stripEmptyValuesIfNecessary(
            ProxyMethodInfo proxyMethodInfo, 
            String[] tokens
    ) {
        if (proxyMethodInfo == null) {
            return tokens;
        }

        if (proxyMethodInfo.hasAnnotation(StripEmptyValues.class)) {
            // Filter empty values.
            return Arrays.stream(tokens)
                .filter(v -> !v.isEmpty())
                .toArray(String[]::new);
        }
        return tokens;
    }
}
