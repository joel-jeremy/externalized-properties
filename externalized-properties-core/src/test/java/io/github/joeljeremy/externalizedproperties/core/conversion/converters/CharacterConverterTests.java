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
import io.github.joeljeremy.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CharacterConverterTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a Character")
    void test1() {
      CharacterConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Character.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a primitive char")
    void test2() {
      CharacterConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Character.TYPE);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is not a Character/char")
    void test3() {
      CharacterConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to a Character")
    void test1() {
      CharacterConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::charWrapperProperty, // This method returns a Character wrapper class
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "j");

      assertNotNull(result);
      Object wrapperValue = result.value();
      assertNotNull(wrapperValue);
      assertTrue(wrapperValue instanceof Character);
      assertEquals('j', (Character) wrapperValue);
    }

    @Test
    @DisplayName("should convert value to a primitive char")
    void test2() {
      CharacterConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::charPrimitiveProperty, // This method returns an char primitive
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "j");

      assertNotNull(result);
      Object primitiveValue = result.value();
      assertNotNull(primitiveValue);
      assertTrue(primitiveValue instanceof Character);
      assertEquals('j', (char) primitiveValue);
    }

    @Test
    @DisplayName("should throw when value is not a valid Character/char")
    void test3() {
      CharacterConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::charPrimitiveProperty, // This method returns an char primitive
              externalizedProperties(converter));

      assertThrows(ConversionException.class, () -> converter.convert(context, "invalid_value"));
    }
  }

  private static CharacterConverter converterToTest() {
    return new CharacterConverter();
  }

  private static ExternalizedProperties externalizedProperties(CharacterConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property.char.primitive")
    char charPrimitiveProperty();

    @ExternalizedProperty("property.char.wrapper")
    Character charWrapperProperty();
  }
}
