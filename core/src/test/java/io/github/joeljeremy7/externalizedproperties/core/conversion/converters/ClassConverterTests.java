package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ClassConverterTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a Class")
    void test1() {
      ClassConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Class.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return false when target type is not a Class")
    void test2() {
      ClassConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to a Class")
    void test1() {
      ClassConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::classProperty, externalizedProperties(converter));

      ConversionResult<Class<?>> result = converter.convert(context, "java.lang.String");

      assertNotNull(result);
      assertEquals(String.class, result.value());
    }

    @Test
    @DisplayName("should throw when value is not a valid Class")
    void test2() {
      ClassConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::classProperty, externalizedProperties(converter));

      assertThrows(ConversionException.class, () -> converter.convert(context, "invalid_value"));
    }
  }

  private static ClassConverter converterToTest() {
    return new ClassConverter();
  }

  private static ExternalizedProperties externalizedProperties(ClassConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property.class")
    Class<?> classProperty();
  }
}
