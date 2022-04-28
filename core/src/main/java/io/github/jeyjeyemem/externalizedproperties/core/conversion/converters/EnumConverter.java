package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ConverterProvider;
import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Type;

/**
 * Supports conversion of values to enums.
 */
public class EnumConverter implements Converter<Enum<?>> {
    /**
     * The {@link ConverterProvider} for {@link EnumConverter}.
     * 
     * @return The {@link ConverterProvider} for {@link EnumConverter}.
     */
    public static ConverterProvider<EnumConverter> provider() {
        return (externalizedProperties, rootConverter) -> new EnumConverter();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return targetType != null && targetType.isEnum();
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<? extends Enum<?>> convert(
            ProxyMethod proxyMethod,
            String valueToConvert,
            Type targetType
    ) { 
        Class<?> enumClass = TypeUtilities.getRawType(targetType);
        
        Object[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null) {
            // Not an enum.
            return ConversionResult.skip();
        }

        for (Object enumConstant : enumConstants) {
            Enum<?> enumValue = (Enum<?>)enumConstant;
            if (enumValue.name().equals(valueToConvert)) {
                return ConversionResult.of(enumValue);
            }
        }
        
        throw new ConversionException(String.format(
            "Invalid (%s) enum value: %s",
            enumClass.getName(),
            valueToConvert
        ));
    }
}
