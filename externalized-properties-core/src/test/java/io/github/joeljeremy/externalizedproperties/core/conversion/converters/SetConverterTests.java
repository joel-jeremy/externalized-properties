package io.github.joeljeremy.externalizedproperties.core.conversion.converters;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy.externalizedproperties.core.Converter;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy.externalizedproperties.core.conversion.Delimiter;
import io.github.joeljeremy.externalizedproperties.core.conversion.StripEmptyValues;
import io.github.joeljeremy.externalizedproperties.core.conversion.converters.SetConverter.SetFactory;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SetConverterTests {
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class Constructor {
    @Test
    @DisplayName("should throw when set factory argument is null")
    void test1() {
      assertThrows(IllegalArgumentException.class, () -> new SetConverter(null));
    }
  }

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is a Set class")
    void test1() {
      SetConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Set.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return false when target type is not a Set class")
    void test2() {
      SetConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(String.class);
      assertFalse(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to a Set")
    void test1() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setProperty, externalizedProperties(converter));

      ConversionResult<Set<?>> result = converter.convert(context, "value1,value2,value3");

      assertNotNull(result);
      Set<?> set = result.value();

      assertNotNull(set);
      assertEquals(3, set.size());
      assertTrue(set.stream().allMatch(v -> v instanceof String));
      assertIterableEquals(new LinkedHashSet<>(Arrays.asList("value1", "value2", "value3")), set);
    }

    @Test
    @DisplayName("should convert to Set<String> when target type has no type parameters i.e. Set")
    void test2() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setRaw, externalizedProperties(converter));

      ConversionResult<Set<?>> result = converter.convert(context, "1,2,3");

      assertNotNull(result);
      Set<?> set = result.value();

      assertNotNull(set);
      assertEquals(3, set.size());
      // Strings and not Integers.
      assertTrue(set.stream().allMatch(v -> v instanceof String));
      assertIterableEquals(Arrays.asList("1", "2", "3"), set);
    }

    @Test
    @DisplayName("should convert value to a Set using custom delimiter")
    void test3() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setCustomDelimiter, externalizedProperties(converter));

      ConversionResult<Set<?>> result = converter.convert(context, "value1#value2#value3");

      assertNotNull(result);
      Set<?> set = result.value();

      assertNotNull(set);
      assertEquals(3, set.size());
      assertTrue(set.stream().allMatch(v -> v instanceof String));
      assertIterableEquals(new LinkedHashSet<>(Arrays.asList("value1", "value2", "value3")), set);
    }

    @Test
    @DisplayName("should convert value according to the Set's generic type parameter")
    void test4() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setInteger,
              externalizedProperties(converter, new IntegerConverter()));

      ConversionResult<Set<?>> result = converter.convert(context, "1,2,3");

      assertNotNull(result);
      Set<?> set = result.value();

      assertNotNull(set);
      assertEquals(3, set.size());
      assertTrue(set.stream().allMatch(v -> v instanceof Integer));
      assertIterableEquals(Arrays.asList(1, 2, 3), set);
    }

    @Test
    @DisplayName("should return String values when Set's generic type parameter is a wildcard.")
    void test5() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setPropertyWildcard, externalizedProperties(converter));

      ConversionResult<Set<?>> result = converter.convert(context, "value1,value2,value3");

      assertNotNull(result);
      Set<?> set = result.value();

      assertNotNull(set);
      assertEquals(3, set.size());
      assertTrue(set.stream().allMatch(v -> v instanceof String));
      assertIterableEquals(new LinkedHashSet<>(Arrays.asList("value1", "value2", "value3")), set);
    }

    @Test
    @DisplayName("should return String values when Set's generic type parameter is Object")
    void test6() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setPropertyObject, externalizedProperties(converter));

      ConversionResult<Set<?>> result = converter.convert(context, "value1,value2,value3");

      assertNotNull(result);
      Set<?> set = result.value();

      assertNotNull(set);
      assertEquals(3, set.size());
      assertTrue(set.stream().allMatch(v -> v instanceof String));
      assertIterableEquals(new LinkedHashSet<>(Arrays.asList("value1", "value2", "value3")), set);
    }

    @Test
    @DisplayName("should return empty Set when property value is empty")
    void test7() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setProperty, externalizedProperties(converter));

      // Value is empty.
      ConversionResult<Set<?>> result = converter.convert(context, "");

      assertNotNull(result);
      Set<?> set = result.value();

      assertNotNull(set);
      assertTrue(set.isEmpty());
    }

    @Test
    @DisplayName("should retain empty values from property value")
    void test8() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setProperty, externalizedProperties(converter));

      // Value has empty values.
      ConversionResult<Set<?>> result = converter.convert(context, "value1,value2,value3,,value5");

      assertNotNull(result);
      Set<?> set = result.value();

      assertNotNull(set);
      assertEquals(5, set.size());
      assertTrue(set.stream().allMatch(v -> v instanceof String));
      assertIterableEquals(
          new LinkedHashSet<>(Arrays.asList("value1", "value2", "value3", "", "value5")), set);
    }

    @Test
    @DisplayName("should strip empty values when annotated with @StripEmptyValues")
    void test9() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setPropertyStripEmpty, externalizedProperties(converter));

      // Value has empty values.
      ConversionResult<Set<?>> result = converter.convert(context, "value1,,value3,,value5");

      assertNotNull(result);
      Set<?> set = result.value();

      assertNotNull(set);
      assertEquals(3, set.size());
      assertTrue(set.stream().allMatch(v -> v instanceof String));
      assertIterableEquals(new LinkedHashSet<>(Arrays.asList("value1", "value3", "value5")), set);
    }

    @Test
    @DisplayName(
        "should throw when no rootConverter is registered that can handle "
            + "the Set's generic type parameter.")
    void test10() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setInteger, externalizedProperties(converter));

      // No registered rootConverter for Integer.
      assertThrows(
          ExternalizedPropertiesException.class, () -> converter.convert(context, "1,2,3,4,5"));
    }

    @Test
    @DisplayName(
        "should convert value according to the Set's generic type parameter. "
            + "Generic type parameter is also a parameterized type e.g. Set<Optional<String>>.")
    void test11() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setPropertyNestedGenerics, // Returns a Set<Optional<String>>.
              externalizedProperties(converter));

      ConversionResult<Set<?>> result = converter.convert(context, "value1,value2,value3");

      assertNotNull(result);
      Set<?> set = result.value();

      assertNotNull(set);
      assertEquals(3, set.size());
      assertTrue(set.stream().allMatch(v -> v instanceof Optional<?>));
      assertIterableEquals(
          new LinkedHashSet<>(
              Arrays.asList(Optional.of("value1"), Optional.of("value2"), Optional.of("value3"))),
          set);
    }

    @Test
    @DisplayName(
        "should convert value according to the Set's generic type parameter. "
            + "Generic type parameter is generic array e.g. Set<Optional<String>[]>.")
    void test12() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setPropertyNestedGenericsArray, // Returns a Set<Optional<String>[]>.
              externalizedProperties(converter, new ArrayConverter()));

      ConversionResult<Set<?>> result = converter.convert(context, "value1,value2,value3");

      assertNotNull(result);
      Set<?> set = result.value();

      assertNotNull(set);
      assertEquals(3, set.size());
      assertTrue(set.stream().allMatch(v -> v instanceof Optional<?>[]));

      // Set is a set of arrays (Set<Optional<String>[]>).
      // Convert to List for set content assertion.
      List<?> setAsList = new ArrayList<>(set);
      Optional<?>[] item1 = (Optional<?>[]) setAsList.get(0);
      Optional<?>[] item2 = (Optional<?>[]) setAsList.get(1);
      Optional<?>[] item3 = (Optional<?>[]) setAsList.get(2);

      assertArrayEquals(new Optional<?>[] {Optional.of("value1")}, item1);

      assertArrayEquals(new Optional<?>[] {Optional.of("value2")}, item2);

      assertArrayEquals(new Optional<?>[] {Optional.of("value3")}, item3);
    }

    @Test
    @DisplayName("should throw when target type has a type variable e.g. Set<T>")
    void test13() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setPropertyT, externalizedProperties(converter));

      assertThrows(ConversionException.class, () -> converter.convert(context, "value"));
    }

    @Test
    @DisplayName("should discard duplicate values")
    void test14() {
      SetConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setProperty, externalizedProperties(converter));

      // There are 4 value1 in value.
      ConversionResult<Set<?>> result =
          converter.convert(context, "value1,value1,value1,value1,value5");

      assertNotNull(result);
      Set<?> set = result.value();

      assertNotNull(set);
      assertEquals(2, set.size());
      assertTrue(set.stream().allMatch(v -> v instanceof String));
      assertIterableEquals(new LinkedHashSet<>(Arrays.asList("value1", "value5")), set);
    }

    /** Set factory tests. */
    @Test
    @DisplayName("should use provided set factory to construct sets.")
    void setFactoryTest1() {
      // Factory returns CopyOnWriteArraySet.
      SetConverter converter = converterToTest(capacity -> new CopyOnWriteArraySet<>());

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setProperty, externalizedProperties(converter));

      ConversionResult<Set<?>> result =
          converter.convert(context, "value1,value2,value3,value4,value5");

      assertNotNull(result);
      Set<?> set = result.value();

      // Default: Should use ',' as delimiter and will not strip empty values.
      // This will strip trailing empty values though.
      assertNotNull(set);
      assertTrue(set instanceof CopyOnWriteArraySet);
      assertEquals(5, set.size());
      assertTrue(set.stream().allMatch(v -> v instanceof String));
      assertIterableEquals(
          new CopyOnWriteArraySet<>(
              Arrays.asList("value1", "value2", "value3", "value4", "value5")),
          set);
    }

    @Test
    @DisplayName("should throw when provided set factory returns null.")
    void setFactoryTest2() {
      // Factory returns null.
      SetConverter converter = converterToTest(capacity -> null);

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setProperty, externalizedProperties(converter));

      // Throws IllegalStateException if set factory returned null.
      assertThrows(
          IllegalStateException.class, () -> converter.convert(context, "value1,value2,value3"));
    }

    @Test
    @DisplayName("should throw when provided set factory returns a populated set.")
    void setFactoryTest3() {
      // Factory returns a populated set.
      SetConverter converter =
          converterToTest(
              capacity -> new HashSet<>(Arrays.asList("should", "not", "be", "populated")));

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setProperty, externalizedProperties(converter));

      // Throws IllegalStateException if set factory returned a populated set.
      assertThrows(
          IllegalStateException.class, () -> converter.convert(context, "value1,value2,value3"));
    }
  }

  static SetConverter converterToTest(SetFactory setFactory) {
    return new SetConverter(setFactory);
  }

  static SetConverter converterToTest() {
    return new SetConverter();
  }

  static ExternalizedProperties externalizedProperties(
      SetConverter converterToTest, Converter<?>... additionalConverters) {
    return ExternalizedProperties.builder()
        .converters(converterToTest)
        .converters(additionalConverters)
        .build();
  }

  static interface ProxyInterface {
    @ExternalizedProperty("property.set")
    Set<String> setProperty();

    @ExternalizedProperty("property.set.object")
    Set<Object> setPropertyObject();

    @ExternalizedProperty("property.set.custom.delimiter")
    @Delimiter("#")
    Set<String> setCustomDelimiter();

    @ExternalizedProperty("property.set.integer")
    Set<Integer> setInteger();

    @ExternalizedProperty("property.set.raw")
    @SuppressWarnings("rawtypes")
    Set setRaw();

    @ExternalizedProperty("property.set.wildcard")
    Set<?> setPropertyWildcard();

    @ExternalizedProperty("property.set.stripempty")
    @StripEmptyValues
    Set<String> setPropertyStripEmpty();

    @ExternalizedProperty("property.set.nested.generics")
    Set<Optional<String>> setPropertyNestedGenerics();

    @ExternalizedProperty("property.set.nested.generics.array")
    Set<Optional<String>[]> setPropertyNestedGenericsArray();

    @ExternalizedProperty("property.set.T")
    <T> Set<T> setPropertyT();
  }
}
