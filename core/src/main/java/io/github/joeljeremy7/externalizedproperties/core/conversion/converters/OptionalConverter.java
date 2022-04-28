package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.TypeUtilities;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Type;
import java.util.Optional;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Supports conversion of values to an {@link Optional} instance.
 */
public class OptionalConverter implements Converter<Optional<?>> {

    private final Converter<?> rootConverter;

    /**
     * Constructor.
     * 
     * @param rootConverter The root converter.
     */
    public OptionalConverter(Converter<?> rootConverter) {
        this.rootConverter = requireNonNull(rootConverter, "rootConverter");
    }

    /**
     * The {@link ConverterProvider} for {@link OptionalConverter}.
     * 
     * @return The {@link ConverterProvider} for {@link OptionalConverter}.
     */
    public static ConverterProvider<OptionalConverter> provider() {
        return (externalizedProperties, rootConverter) -> 
            new OptionalConverter(rootConverter);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return Optional.class.equals(targetType);
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<? extends Optional<?>> convert(
            ProxyMethod proxyMethod,
            String valueToConvert,
            Type targetType
    ) { 
        Type[] genericTypeParams = TypeUtilities.getTypeParameters(targetType);
        
        // Assume initially as Optional of string type.
        Type targetOptionalType = String.class;
        if (genericTypeParams.length > 0) {
            // Do not allow Optional<T>, Optional<T extends ...>, etc.
            targetOptionalType = throwIfTypeVariable(genericTypeParams[0]);
        }

        if (valueToConvert.isEmpty()) {
            return ConversionResult.of(Optional.empty());
        }

        Class<?> rawTargetOptionalType = TypeUtilities.getRawType(targetOptionalType);

        // If Optional<String> or Optional<Object>, return String value.
        if (String.class.equals(rawTargetOptionalType) || 
                Object.class.equals(rawTargetOptionalType)) {
            return ConversionResult.of(Optional.of(valueToConvert));
        }

        return ConversionResult.of(
            convertToOptionalType(
                proxyMethod,
                valueToConvert,
                targetOptionalType
            )
        );
    }

    private Optional<?> convertToOptionalType(
            ProxyMethod proxyMethod,
            String valueToConvert,
            Type optionalGenericTypeParameter
    ) {
        ConversionResult<?> converted = rootConverter.convert(
            proxyMethod, 
            valueToConvert, 
            optionalGenericTypeParameter
        );
        // Convert property and wrap in Optional.
        return Optional.ofNullable(converted.value());
    }

    private Type throwIfTypeVariable(Type optionalGenericTypeParameter) {
        if (TypeUtilities.isTypeVariable(optionalGenericTypeParameter)) {
            throw new ConversionException(
                "Type variables e.g. Optional<T> are not supported."
            );
        }

        return optionalGenericTypeParameter;
    }
}
