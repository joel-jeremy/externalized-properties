package io.github.joeljeremy7.externalizedproperties.core.internal.conversion;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Type;

/**
 * A no-op {@link Converter} implementation.
 * This is only used internally in creating the root converter.
 */
public class NoOpConverter implements Converter<Object> {

    /**
     * Singleton instance.
     */
    public static final NoOpConverter INSTANCE = Singleton.INSTANCE;

    private NoOpConverter(){}

    /** 
     * {@inheritDoc} 
     * 
     * @return {@code false}.
     */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return false;
    }

    /** 
     * {@inheritDoc} 
     *
     * @return {@link ConversionResult#skip()} 
     */
    @Override
    public ConversionResult<? extends Object> convert(
            ProxyMethod proxyMethod, 
            String valueToConvert, 
            Type targetType
    ) {
        return ConversionResult.skip();
    }

    private static class Singleton {
        private static final NoOpConverter INSTANCE = new NoOpConverter();
    }
}
