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

public class EnumConverterTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is an enum")
    void test1() {
      EnumConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(TestEnum.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return false when target type is not an enum")
    void test2() {
      EnumConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(String.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert resolved property to enum")
    void test1() {
      EnumConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::enumProperty, externalizedProperties(converter));

      ConversionResult<? extends Enum<?>> result = converter.convert(context, TestEnum.ONE.name());
      assertNotNull(result);

      Enum<?> testEnum = result.value();
      assertEquals(TestEnum.ONE, testEnum);
    }

    @Test
    @DisplayName("should throw when property value is not a valid enum value")
    void test2() {
      EnumConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::enumProperty, externalizedProperties(converter));

      assertThrows(
          ConversionException.class,
          () -> {
            converter.convert(context, "INVALID_ENUM_VALUE");
          });
    }

    @Test
    @DisplayName("should return skipped result when target type is not an enum")
    void test3() {
      EnumConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::notSupportedNotAnEnum, externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "1");
      assertEquals(ConversionResult.skip(), result);
    }
  }

  private static EnumConverter converterToTest() {
    return new EnumConverter();
  }

  private static ExternalizedProperties externalizedProperties(EnumConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property.enum")
    TestEnum enumProperty();

    @ExternalizedProperty("property.not.supported")
    int notSupportedNotAnEnum();
  }

  private static enum TestEnum {
    NONE,
    ONE,
    TWO,
    THREE
  }
}
