package io.github.joeljeremy.externalizedproperties.core.conversion.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

public class BooleanConverterTests {
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a Boolean")
    void test1() {
      BooleanConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Boolean.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a primitive boolean")
    void test2() {
      BooleanConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Boolean.TYPE);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is not a Boolean/boolean")
    void test3() {
      BooleanConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to a Boolean")
    void test1() {
      BooleanConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::booleanWrapperProperty, // This method returns a Boolean wrapper class
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "true");

      assertNotNull(result);
      Object wrapperValue = result.value();
      assertNotNull(wrapperValue);
      assertTrue(wrapperValue instanceof Boolean);
      assertEquals(true, (Boolean) wrapperValue);
    }

    @Test
    @DisplayName("should convert value to a primitive boolean")
    void test2() {
      BooleanConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::booleanPrimitiveProperty, // This method returns a boolean primitive
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "false");

      assertNotNull(result);
      Object primitiveValue = result.value();
      assertNotNull(primitiveValue);
      assertTrue(primitiveValue instanceof Boolean);
      assertEquals(false, (boolean) primitiveValue);
    }

    @Test
    @DisplayName("should convert to false when property value is not a valid Boolean/boolean")
    void test3() {
      BooleanConverter converter = converterToTest();

      InvocationContext wrapperContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::booleanWrapperProperty, // This method returns a Boolean wrapper class
              externalizedProperties(converter));

      InvocationContext primitiveContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::booleanPrimitiveProperty, // This method returns a primitive boolean
              externalizedProperties(converter));

      ConversionResult<?> wrapperResult = converter.convert(wrapperContext, "invalid_boolean");
      ConversionResult<?> primitiveResult = converter.convert(primitiveContext, "invalid_boolean");

      assertNotNull(wrapperResult);
      assertNotNull(primitiveResult);
      Object wrapperValue = wrapperResult.value();
      Object primitiveValue = primitiveResult.value();

      assertNotNull(wrapperValue);
      assertNotNull(primitiveValue);

      assertTrue(wrapperValue instanceof Boolean);
      assertTrue(primitiveValue instanceof Boolean);

      assertEquals(false, (Boolean) wrapperValue);
      assertEquals(false, (boolean) primitiveValue);
    }
  }

  static BooleanConverter converterToTest() {
    return new BooleanConverter();
  }

  static ExternalizedProperties externalizedProperties(BooleanConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  static interface ProxyInterface {
    @ExternalizedProperty("property.boolean.primitive")
    boolean booleanPrimitiveProperty();

    @ExternalizedProperty("property.boolean.wrapper")
    Boolean booleanWrapperProperty();
  }
}
