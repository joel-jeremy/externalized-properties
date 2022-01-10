package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;

import java.io.StringReader;
import java.util.Properties;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion of values to a {@link Properties} instance.
 */
public class PropertiesConversionHandler implements ConversionHandler<Properties> {
    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return Properties.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<? extends Properties> convert(ConversionContext context) {
        requireNonNull(context, "context");
        return ConversionResult.of(loadValueAsProperties(context));
    }

    private Properties loadValueAsProperties(ConversionContext context) {
        Properties properties = new Properties();
        try (StringReader reader = new StringReader(context.value())) {
            properties.load(reader);
            return properties;
        } catch (Exception ex) {
            throw new ConversionException("Failed to load properties.", ex);
        }
    }
}
