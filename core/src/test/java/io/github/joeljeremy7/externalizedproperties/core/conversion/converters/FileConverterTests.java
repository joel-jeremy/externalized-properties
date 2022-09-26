package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class FileConverterTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  private static File TEMP_FILE;

  @BeforeAll
  static void setup() throws IOException {
    TEMP_FILE = Files.createTempFile(FileConverterTests.class.getSimpleName(), ".tmp").toFile();
  }

  @AfterAll
  static void cleanup() throws IOException {
    Files.delete(TEMP_FILE.toPath());
  }

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a File")
    void test1() {
      FileConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(File.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return false when target type is not a File")
    void test2() {
      FileConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to a File")
    void test1() {
      FileConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::fileProperty, externalizedProperties(converter));

      ConversionResult<File> result = converter.convert(context, TEMP_FILE.getAbsolutePath());

      assertNotNull(result);
      assertEquals(TEMP_FILE, result.value());
      assertTrue(result.value().exists());
    }

    @Test
    @DisplayName("should convert value to a File even if file in path is non-existent")
    void test2() {
      FileConverter converter = converterToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::fileProperty, externalizedProperties(converter));

      File nonExistentFile = new File("/path/to/non-existent.file");

      ConversionResult<File> result = converter.convert(context, nonExistentFile.getAbsolutePath());

      assertNotNull(result);
      assertEquals(nonExistentFile, result.value());
      assertFalse(result.value().exists());
    }
  }

  private static FileConverter converterToTest() {
    return new FileConverter();
  }

  private static ExternalizedProperties externalizedProperties(FileConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property.file")
    File fileProperty();
  }
}
