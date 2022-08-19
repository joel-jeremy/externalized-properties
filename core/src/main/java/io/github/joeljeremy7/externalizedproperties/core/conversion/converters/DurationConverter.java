package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.format.DateTimeParseException;

/**
 * Supports conversion of values to a {@link Duration}.
 * This supports values in ISO-8601 duration format and numeric values (in milliseconds).
 */
public class DurationConverter implements Converter<Duration> {

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return Duration.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Duration> convert(
            InvocationContext context, 
            String valueToConvert, 
            Type targetType
    ) {
        if (is8601DurationFormat(valueToConvert)) {
            try {
                // Handle as ISO-8601 duration format.
                return ConversionResult.of(Duration.parse(valueToConvert));
            } catch (DateTimeParseException e) {
                throw new ConversionException(
                    String.format("Invalid ISO 8601 duration format: %s", valueToConvert),
                    e
                );
            }
        }

        try {
            // Expect value in milliseconds.
            long durationInMillis = Long.parseLong(valueToConvert);
            return ConversionResult.of(Duration.ofMillis(durationInMillis));
        } catch (NumberFormatException e) {
            throw new ConversionException(
                String.format("Value must be a number (in milliseconds): %s", valueToConvert)
            );
        }
    }

    private static boolean is8601DurationFormat(String valueToConvert) {
        if (valueToConvert.length() > 0) {
            char firstChar = Character.toUpperCase(valueToConvert.charAt(0));
            if (firstChar == 'P') {
                return true;
            }

            if (firstChar == '+' || firstChar == '-') {
                if (valueToConvert.length() > 1) {
                    char secondChar = Character.toUpperCase(valueToConvert.charAt(1));
                    if (secondChar == 'P') {
                        // +P or -P
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
