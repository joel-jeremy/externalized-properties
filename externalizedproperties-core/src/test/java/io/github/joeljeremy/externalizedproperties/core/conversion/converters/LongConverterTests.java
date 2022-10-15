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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class LongConverterTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a Long")
    void test1() {
      LongConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Long.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a primitive long")
    void test2() {
      LongConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Long.TYPE);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is not a Long/long")
    void test3() {
      LongConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to a Long")
    void test1() {
      LongConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::longWrapperProperty, // This method returns a Long wrapper class
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "1");

      assertNotNull(result);
      Object wrapperValue = result.value();
      assertNotNull(wrapperValue);
      assertTrue(wrapperValue instanceof Long);
      assertEquals((long) 1, (Long) wrapperValue);
    }

    @Test
    @DisplayName("should convert value to a primitive long")
    void test2() {
      LongConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::longPrimitiveProperty, // This method returns an long primitive
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "2");

      assertNotNull(result);
      Object primitiveValue = result.value();
      assertNotNull(primitiveValue);
      assertTrue(primitiveValue instanceof Long);
      assertEquals((long) 2, (long) primitiveValue);
    }

    @Test
    @DisplayName("should throw when value is not a valid Long/long")
    void test3() {
      LongConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::longPrimitiveProperty, // This method returns an long primitive
              externalizedProperties(converter));

      assertThrows(NumberFormatException.class, () -> converter.convert(context, "invalid_value"));
    }
  }

  private static LongConverter converterToTest() {
    return new LongConverter();
  }

  private static ExternalizedProperties externalizedProperties(LongConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property.long.primitive")
    long longPrimitiveProperty();

    @ExternalizedProperty("property.long.wrapper")
    Long longWrapperProperty();
  }
}
