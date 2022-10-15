package io.github.joeljeremy.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy.externalizedproperties.core.Converter;
import io.github.joeljeremy.externalizedproperties.core.ConverterFacade;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.TypeUtilities;
import io.github.joeljeremy.externalizedproperties.core.conversion.ConversionException;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Supports conversion of values to an {@link Optional} instance.
 *
 * <p>This is converter is registered out of the box (no need to explicitly register) as conversion
 * to an Optional is natively supported.
 */
public class OptionalConverter implements Converter<Optional<?>> {
  /** {@inheritDoc} */
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return Optional.class.equals(targetType);
  }

  /** {@inheritDoc} */
  @Override
  public ConversionResult<Optional<?>> convert(
      InvocationContext context, String valueToConvert, Type targetType) {
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
    if (String.class.equals(rawTargetOptionalType) || Object.class.equals(rawTargetOptionalType)) {
      return ConversionResult.of(Optional.of(valueToConvert));
    }

    return ConversionResult.of(convertToOptionalType(context, valueToConvert, targetOptionalType));
  }

  private Optional<?> convertToOptionalType(
      InvocationContext context, String valueToConvert, Type optionalGenericTypeParameter) {
    ConverterProxy rootConverter =
        context.externalizedProperties().initialize(ConverterProxy.class);

    Object converted = rootConverter.convert(valueToConvert, optionalGenericTypeParameter);
    // Convert property and wrap in Optional.
    return Optional.ofNullable(converted);
  }

  private Type throwIfTypeVariable(Type optionalGenericTypeParameter) {
    if (TypeUtilities.isTypeVariable(optionalGenericTypeParameter)) {
      throw new ConversionException("Type variables e.g. Optional<T> are not supported.");
    }

    return optionalGenericTypeParameter;
  }

  private static interface ConverterProxy {
    @ConverterFacade
    Object convert(String valueToConvert, Type targetType);
  }
}
