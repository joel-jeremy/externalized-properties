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

public class IntegerConverterTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is an Integer")
    void test1() {
      IntegerConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a primitive int")
    void test2() {
      IntegerConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.TYPE);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is not an Integer/int")
    void test3() {
      IntegerConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Boolean.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to an Integer")
    void test1() {
      IntegerConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::intWrapperProperty, // This method returns a Integer wrapper class
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "1");

      assertNotNull(result);
      Object wrapperValue = result.value();
      assertNotNull(wrapperValue);
      assertTrue(wrapperValue instanceof Integer);
      assertEquals(1, (Integer) wrapperValue);
    }

    @Test
    @DisplayName("should convert value to a primitive int")
    void test2() {
      IntegerConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::intPrimitiveProperty, // This method returns an int primitive
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "2");

      assertNotNull(result);
      Object primitiveValue = result.value();
      assertNotNull(primitiveValue);
      assertTrue(primitiveValue instanceof Integer);
      assertEquals(2, (int) primitiveValue);
    }

    @Test
    @DisplayName("should throw when value is not a valid Integer/int")
    void test3() {
      IntegerConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::intPrimitiveProperty, // This method returns an int primitive
              externalizedProperties(converter));

      assertThrows(NumberFormatException.class, () -> converter.convert(context, "invalid_value"));
    }
  }

  private static IntegerConverter converterToTest() {
    return new IntegerConverter();
  }

  private static ExternalizedProperties externalizedProperties(IntegerConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property.int.primitive")
    int intPrimitiveProperty();

    @ExternalizedProperty("property.int.wrapper")
    Integer intWrapperProperty();
  }
}
