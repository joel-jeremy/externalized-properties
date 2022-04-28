package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Type;

/**
 * API for handling conversion of values to various types.
 * 
 * @param <T> The target type for this converter.
 */
public interface Converter<T> {
    /**
     * Checks if the handler can convert values to the target type.
     * 
     * @param targetType The target type to convert to.
     * @return {@code true}, if the implementation can convert values 
     * to the specified type. Otherwise, {@code false}.
     */
    boolean canConvertTo(Class<?> targetType);

    /**
     * Convert value to the target type.
     * 
     * @implNote If implementation does not support conversion to the target type, 
     * it may return {@link ConversionResult#skip()} to indicate that the converter cannot 
     * handle conversion to the target type and that the conversion process should skip/move 
     * to the next registered converter in the conversion pipeline. However, if an exception 
     * is thrown, the conversion will fail and will not attempt to convert using the other 
     * registered converters.
     * 
     * @param proxyMethod The proxy method.
     * @param valueToConvert The value to convert.
     * @return The result of conversion with the return type of the proxy method as the 
     * target type to convert to, or {@link ConversionResult#skip()} if the converter cannot 
     * handle conversion to the target type and that the conversion process should skip/move 
     * to the next registered converter in the conversion pipeline.
     */
    default ConversionResult<? extends T> convert(
        ProxyMethod proxyMethod, 
        String valueToConvert
    ) {
        return convert(proxyMethod, valueToConvert, proxyMethod.returnType());
    }

    /**
     * Convert value to the target type.
     * 
     * @implNote If implementation does not support conversion to the target type, 
     * it may return {@link ConversionResult#skip()} to indicate that the converter cannot 
     * handle conversion to the target type and that the conversion process should skip/move 
     * to the next registered converter in the conversion pipeline. However, if an exception 
     * is thrown, the conversion will fail and will not attempt to convert using the other 
     * registered converters.
     * 
     * @param proxyMethod The proxy method.
     * @param valueToConvert The value to convert.
     * @param targetType The target type of the conversion.
     * @return The result of conversion to the target type or {@link ConversionResult#skip()}
     * if the converter cannot handle conversion to the target type and that the conversion 
     * process should skip/move to the next registered converter in the conversion pipeline.
     */
    ConversionResult<? extends T> convert(
        ProxyMethod proxyMethod, 
        String valueToConvert,
        Type targetType
    );
}
