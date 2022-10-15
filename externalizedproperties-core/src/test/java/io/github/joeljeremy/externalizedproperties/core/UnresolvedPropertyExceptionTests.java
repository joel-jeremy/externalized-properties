package io.github.joeljeremy.externalizedproperties.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class UnresolvedPropertyExceptionTests {
  @Nested
  class Constructor {
    @Test
    @DisplayName("should set unresolved property name")
    void test1() {
      String unresolvedPropertyName = "test.property";

      UnresolvedPropertyException unresolvedPropertyException =
          new UnresolvedPropertyException(
              unresolvedPropertyName, "test.property cannot be resolved");

      assertEquals(unresolvedPropertyName, unresolvedPropertyException.externalizedPropertyName());
    }

    @Test
    @DisplayName("should set unresolved property name")
    void test2() {
      String unresolvedPropertyName = "test.property";

      UnresolvedPropertyException unresolvedPropertyException =
          new UnresolvedPropertyException(
              unresolvedPropertyName,
              "test.property cannot be resolved",
              new RuntimeException("cause"));

      assertEquals(unresolvedPropertyName, unresolvedPropertyException.externalizedPropertyName());
    }
  }

  @Nested
  class ExternalizedPropertyNameMethod {
    @Test
    @DisplayName("should return an unmodifiable set")
    void test1() {
      String unresolvedPropertyNames = "test.property.1";

      UnresolvedPropertyException unresolvedPropertyException =
          new UnresolvedPropertyException(
              unresolvedPropertyNames, "Properties cannot be resolved: " + unresolvedPropertyNames);

      String unresolvedProperty = unresolvedPropertyException.externalizedPropertyName();

      assertEquals("test.property.1", unresolvedProperty);
    }
  }
}
