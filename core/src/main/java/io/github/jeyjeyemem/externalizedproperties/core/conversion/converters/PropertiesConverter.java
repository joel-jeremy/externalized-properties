package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionException;

import java.io.StringReader;
import java.util.Properties;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion of values to a {@link Properties} instance.
 */
public class PropertiesConverter implements Converter<Properties> {
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
