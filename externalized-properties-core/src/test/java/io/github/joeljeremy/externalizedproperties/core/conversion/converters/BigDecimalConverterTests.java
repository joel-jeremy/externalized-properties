package io.github.joeljeremy.externalizedproperties.core.conversion.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class BigDecimalConverterTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a BigDecimal")
    void test1() {
      BigDecimalConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(BigDecimal.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return false when target type is not a BigDecimal")
    void test2() {
      BigDecimalConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to a BigDecimal")
    void test1() {
      BigDecimalConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::bigDecimalProperty, externalizedProperties(converter));

      ConversionResult<BigDecimal> result = converter.convert(context, "1.0");

      assertNotNull(result);
      assertEquals(BigDecimal.valueOf(1.0), result.value());
    }

    @Test
    @DisplayName("should throw when value is not a valid BigDecimal")
    void test2() {
      BigDecimalConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::bigDecimalProperty, externalizedProperties(converter));

      assertThrows(NumberFormatException.class, () -> converter.convert(context, "invalid_value"));
    }
  }

  private static BigDecimalConverter converterToTest() {
    return new BigDecimalConverter();
  }

  private static ExternalizedProperties externalizedProperties(
      BigDecimalConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property.bigdemical")
    BigDecimal bigDecimalProperty();
  }
}
