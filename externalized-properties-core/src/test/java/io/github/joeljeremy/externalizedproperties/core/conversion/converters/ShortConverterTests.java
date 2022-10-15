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

public class ShortConverterTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a Short")
    void test1() {
      ShortConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Short.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a primitive short")
    void test2() {
      ShortConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Short.TYPE);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is not a Short/short")
    void test3() {
      ShortConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to a Short")
    void test1() {
      ShortConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::shortWrapperProperty, // This method returns a Short wrapper class
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "1");

      assertNotNull(result);
      Object wrapperValue = result.value();
      assertNotNull(wrapperValue);
      assertTrue(wrapperValue instanceof Short);
      assertEquals((short) 1, (Short) wrapperValue);
    }

    @Test
    @DisplayName("should convert value to a primitive short")
    void test2() {
      ShortConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::shortPrimitiveProperty, // This method returns an short primitive
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "2");

      assertNotNull(result);
      Object primitiveValue = result.value();
      assertNotNull(primitiveValue);
      assertTrue(primitiveValue instanceof Short);
      assertEquals((short) 2, (short) primitiveValue);
    }

    @Test
    @DisplayName("should throw when value is not a valid Short/short")
    void test3() {
      ShortConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::shortPrimitiveProperty, // This method returns an short primitive
              externalizedProperties(converter));

      assertThrows(NumberFormatException.class, () -> converter.convert(context, "invalid_value"));
    }
  }

  private static ShortConverter converterToTest() {
    return new ShortConverter();
  }

  private static ExternalizedProperties externalizedProperties(ShortConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property.short.primitive")
    short shortPrimitiveProperty();

    @ExternalizedProperty("property.short.wrapper")
    Short shortWrapperProperty();
  }
}
