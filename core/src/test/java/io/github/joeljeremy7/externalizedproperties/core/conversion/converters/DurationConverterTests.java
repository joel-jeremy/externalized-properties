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
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class DurationConverterTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a Duration")
    void test1() {
      DurationConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Duration.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return false when target type is not a Duration")
    void test2() {
      DurationConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert number value to a Duration (in milliseconds)")
    void test1() {
      DurationConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::durationProperty, externalizedProperties(converter));

      ConversionResult<Duration> result = converter.convert(context, "60");

      assertNotNull(result);
      assertEquals(Duration.ofMillis(60), result.value());
    }

    @ParameterizedTest
    // 2 days and 30 mins
    @ValueSource(strings = {"P2DT30M", "+P2DT30M", "-P2DT30M"})
    @DisplayName("should convert value (in ISO 8601 format) to a Duration")
    void test2(String iso8601Duration) {
      DurationConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::durationProperty, externalizedProperties(converter));

      ConversionResult<Duration> result = converter.convert(context, iso8601Duration);

      assertNotNull(result);
      assertEquals(Duration.parse(iso8601Duration), result.value());
    }

    @ParameterizedTest
    // 2 days and 30 mins
    @ValueSource(
        strings = {
          "invalid_value",
          "",
          "P", // No duration values
          "P2DT", // No time values
          "+", // No duration values
          "-", // No duration values
          "+Q", // Invalid duration values
          "-Q" // Invalid duration values
        })
    @DisplayName("should throw when value is not a valid Duration")
    void test3(String iso8601Duration) {
      DurationConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::durationProperty, externalizedProperties(converter));

      assertThrows(ConversionException.class, () -> converter.convert(context, iso8601Duration));
    }
  }

  private static DurationConverter converterToTest() {
    return new DurationConverter();
  }

  private static ExternalizedProperties externalizedProperties(DurationConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property.duration")
    Duration durationProperty();
  }
}
