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
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class UUIDConverterTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a UUID")
    void test1() {
      UUIDConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(UUID.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return false when target type is not a UUID")
    void test2() {
      UUIDConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to a UUID")
    void test1() {
      UUIDConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::uuidProperty, externalizedProperties(converter));

      UUID uuid = UUID.randomUUID();

      ConversionResult<UUID> result = converter.convert(context, uuid.toString());

      assertNotNull(result);
      assertEquals(uuid, result.value());
    }

    @Test
    @DisplayName("should throw when value is not a valid UUID")
    void test2() {
      UUIDConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::uuidProperty, externalizedProperties(converter));

      assertThrows(
          IllegalArgumentException.class, () -> converter.convert(context, "invalid_value"));
    }
  }

  private static UUIDConverter converterToTest() {
    return new UUIDConverter();
  }

  private static ExternalizedProperties externalizedProperties(UUIDConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property.uuid")
    UUID uuidProperty();
  }
}
