package io.github.joeljeremy7.externalizedproperties.core.testfixtures;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class StubConverter<T> implements Converter<T> {
    private final Predicate<Class<?>> canConvertTo;
    private final ConvertDelegate<T> convert;
    private final Map<ConverterResultKey, T> trackedConversionResults = 
        new HashMap<>();

    public StubConverter() {
        // Always skips.
        this(
            targetType -> true, 
            (pm, value, targetType) -> ConversionResult.skip()
        );
    }

    public StubConverter(ConvertDelegate<T> convert) {
        this(targetType -> true, convert);
    }

    public StubConverter(
            Predicate<Class<?>> canConvertTo, 
            ConvertDelegate<T> convert
    ) {
        this.canConvertTo = canConvertTo;
        this.convert = convert;
    }

    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return canConvertTo.test(targetType);
    }

    @Override
    public ConversionResult<T> convert(
            ProxyMethod proxyMethod, 
            String valueToConvert, 
            Type targetType
    ) {
        ConversionResult<T> result = 
            convert.convert(proxyMethod, valueToConvert, targetType);

        if (result != ConversionResult.skip()) {
            trackedConversionResults.putIfAbsent(
                new ConverterResultKey(valueToConvert, targetType), 
                result.value()
            );
        }
        return result;
    }

    public Map<ConverterResultKey, T> conversionResults() {
        return Collections.unmodifiableMap(trackedConversionResults);
    }
    
    public static interface ConvertDelegate<T> {
        ConversionResult<T> convert(
            ProxyMethod proxyMethod, 
            String valueToConvert, 
            Type targetType
        );
    }

    public static class ConverterResultKey {
        private final String valueToConvert;
        private final Type targetType;

        public ConverterResultKey(
                String valueToConvert,
                Type targetType
        ) {
            this.valueToConvert = valueToConvert;
            this.targetType = targetType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(valueToConvert, targetType);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ConverterResultKey)) {
                return false;
            }

            ConverterResultKey other = (ConverterResultKey)obj;
            return Objects.equals(valueToConvert, other.valueToConvert) &&
                Objects.equals(targetType, other.targetType);
        }
    }
}
