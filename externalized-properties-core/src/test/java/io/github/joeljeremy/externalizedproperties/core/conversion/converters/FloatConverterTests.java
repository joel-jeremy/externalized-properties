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

public class FloatConverterTests {
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a Float")
    void test1() {
      FloatConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Float.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a primitive float")
    void test2() {
      FloatConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Float.TYPE);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is not a Float/float")
    void test3() {
      FloatConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to a Float")
    void test1() {
      FloatConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::floatWrapperProperty, // This method returns a Float wrapper class
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "1");

      assertNotNull(result);
      Object wrapperValue = result.value();
      assertNotNull(wrapperValue);
      assertTrue(wrapperValue instanceof Float);
      assertEquals((float) 1, (Float) wrapperValue);
    }

    @Test
    @DisplayName("should convert value to a primitive float")
    void test2() {
      FloatConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::floatPrimitiveProperty, // This method returns an float primitive
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "2");

      assertNotNull(result);
      Object primitiveValue = result.value();
      assertNotNull(primitiveValue);
      assertTrue(primitiveValue instanceof Float);
      assertEquals((float) 2, (float) primitiveValue);
    }

    @Test
    @DisplayName("should throw when value is not a valid Float/float")
    void test3() {
      FloatConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::floatPrimitiveProperty, // This method returns an float primitive
              externalizedProperties(converter));

      assertThrows(NumberFormatException.class, () -> converter.convert(context, "invalid_value"));
    }
  }

  static FloatConverter converterToTest() {
    return new FloatConverter();
  }

  static ExternalizedProperties externalizedProperties(FloatConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  static interface ProxyInterface {
    @ExternalizedProperty("property.float.primitive")
    float floatPrimitiveProperty();

    @ExternalizedProperty("property.float.wrapper")
    Float floatWrapperProperty();
  }
}
