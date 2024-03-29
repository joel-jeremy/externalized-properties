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
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PatternConverterTests {
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a Pattern")
    void test1() {
      PatternConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Pattern.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return false when target type is not a Pattern")
    void test2() {
      PatternConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to a Pattern")
    void test1() {
      PatternConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::patternProperty, externalizedProperties(converter));

      String regex = "^externalizedproperties$";

      ConversionResult<Pattern> result = converter.convert(context, regex);

      assertNotNull(result);
      assertEquals(regex, result.value().pattern());
    }
  }

  static PatternConverter converterToTest() {
    return new PatternConverter();
  }

  static ExternalizedProperties externalizedProperties(PatternConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  static interface ProxyInterface {
    @ExternalizedProperty("property.pattern")
    Pattern patternProperty();
  }
}
