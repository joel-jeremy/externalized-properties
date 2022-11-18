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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PathConverterTests {
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  static Path TEMP_FILE_PATH;

  @BeforeAll
  static void setup() throws IOException {
    TEMP_FILE_PATH = Files.createTempFile(PathConverterTests.class.getSimpleName(), ".tmp");
  }

  @AfterAll
  static void cleanup() throws IOException {
    Files.delete(TEMP_FILE_PATH);
  }

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a Path")
    void test1() {
      PathConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Path.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return false when target type is not a Path")
    void test2() {
      PathConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to a Path")
    void test1() {
      PathConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::pathProperty, externalizedProperties(converter));

      ConversionResult<Path> result = converter.convert(context, TEMP_FILE_PATH.toString());

      assertNotNull(result);
      assertEquals(TEMP_FILE_PATH, result.value());
      assertTrue(Files.exists(result.value()));
    }

    @Test
    @DisplayName("should convert value to a Path even if file in path is non-existent")
    void test2() {
      PathConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::pathProperty, externalizedProperties(converter));

      Path nonExistentPath = Paths.get("/path/to/non-existent.file");

      ConversionResult<Path> result = converter.convert(context, nonExistentPath.toString());

      assertNotNull(result);
      assertEquals(nonExistentPath, result.value());
      assertFalse(Files.exists(result.value()));
    }
  }

  static PathConverter converterToTest() {
    return new PathConverter();
  }

  static ExternalizedProperties externalizedProperties(PathConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  static interface ProxyInterface {
    @ExternalizedProperty("property.path")
    Path pathProperty();
  }
}
