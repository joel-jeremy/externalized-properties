package io.github.joeljeremy.externalizedproperties.core.conversion.converters;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class DefaultConverterTests {
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertToMethod {
    @Test
    @DisplayName("should return true when target type is an Integer")
    void primitiveTest1() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a primitive int")
    void primitiveTest2() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Integer.TYPE);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a Long")
    void primitiveTest3() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Long.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a primitive long")
    void primitiveTest4() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Long.TYPE);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a Float")
    void primitiveTest5() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Float.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a primitive float")
    void primitiveTest6() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Float.TYPE);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a Double")
    void primitiveTest7() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Double.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a primitive double")
    void primitiveTest8() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Double.TYPE);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a List")
    void listTest1() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(List.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a Collection")
    void listTest2() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Collection.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is a Set")
    void setTest1() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Set.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is an Array")
    void arrayTest1() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(String[].class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is an enum")
    void enumTest1() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(TestEnum.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is LocalDateTime")
    void dateTimeTest1() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(LocalDateTime.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is LocalDate")
    void dateTimeTest2() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(LocalDate.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is LocalTime")
    void dateTimeTest3() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(LocalTime.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is OffsetDateTime")
    void dateTimeTest4() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(OffsetDateTime.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is OffsetTime")
    void dateTimeTest5() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(OffsetTime.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is ZonedDateTime")
    void dateTimeTest6() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(ZonedDateTime.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is Instant")
    void dateTimeTest7() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Instant.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is DayOfWeek")
    void dateTimeTest8() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(DayOfWeek.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is Month")
    void dateTimeTest9() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Month.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is MonthDay")
    void dateTimeTest10() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(MonthDay.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is Year")
    void dateTimeTest11() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Year.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is YearMonth")
    void dateTimeTest12() {
      DefaultConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(YearMonth.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return false when target type is not supported")
    void unsupportedTest1() {
      DefaultConverter converter = converterToTest();
      // Unsupported.
      boolean canConvert = converter.canConvertTo(Properties.class);
      assertFalse(canConvert);
    }

    // @Test
    // @DisplayName("should return false when target type is null")
    // void unsupportedTest2() {
    //     DefaultConverter converter = converterToTest();
    //     boolean canConvert = converter.canConvertTo(null);
    //     assertFalse(canConvert);
    // }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should return skip result when target type is not supported")
    void unsupportedTest1() {
      DefaultConverter converter = converterToTest();

      // Not primitive, List/Collection, array or Optional.
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::customType, // This method returns a CustomType.
              externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "");
      assertEquals(ConversionResult.skip(), result);
    }

    @Test
    @DisplayName("should convert resolved property to an Integer or primitive int")
    void primitiveTest1() {
      DefaultConverter converter = converterToTest();

      InvocationContext wrapperContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::intWrapperProperty, // This method returns a Integer wrapper class
              externalizedProperties(converter));

      InvocationContext primitiveContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::intPrimitiveProperty, // This method returns an int primitive
              externalizedProperties(converter));

      ConversionResult<?> wrapperResult = converter.convert(wrapperContext, "1");

      ConversionResult<?> primitiveResult = converter.convert(primitiveContext, "2");

      assertNotNull(wrapperResult);
      assertNotNull(primitiveResult);
      Object wrapperValue = wrapperResult.value();
      Object primitiveValue = primitiveResult.value();

      assertNotNull(wrapperValue);
      assertNotNull(primitiveValue);

      assertTrue(wrapperValue instanceof Integer);
      assertTrue(primitiveValue instanceof Integer);

      assertEquals(1, wrapperValue);
      assertEquals(2, primitiveValue);
    }

    @Test
    @DisplayName("should convert resolved property to a Long or primitive long")
    void primitiveTest2() {
      DefaultConverter converter = converterToTest();

      InvocationContext wrapperContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::longWrapperProperty, // This method returns a Long wrapper class
              externalizedProperties(converter));

      InvocationContext primitiveContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::longPrimitiveProperty, // This method returns an long primitive
              externalizedProperties(converter));

      ConversionResult<?> wrapperResult = converter.convert(wrapperContext, "1");

      ConversionResult<?> primitiveResult = converter.convert(primitiveContext, "2");

      assertNotNull(wrapperResult);
      assertNotNull(primitiveResult);
      Object wrapperValue = wrapperResult.value();
      Object primitiveValue = primitiveResult.value();

      assertNotNull(wrapperValue);
      assertNotNull(primitiveValue);

      assertTrue(wrapperValue instanceof Long);
      assertTrue(primitiveValue instanceof Long);

      assertEquals(1L, wrapperValue);
      assertEquals(2L, primitiveValue);
    }

    @Test
    @DisplayName("should convert resolved property to a Float or primitive float")
    void primitiveTest3() {
      DefaultConverter converter = converterToTest();

      InvocationContext wrapperContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::floatWrapperProperty, // This method returns a Float wrapper class
              externalizedProperties(converter));

      InvocationContext primitiveContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::floatPrimitiveProperty, // This method returns an float primitive
              externalizedProperties(converter));

      ConversionResult<?> wrapperResult = converter.convert(wrapperContext, "1.0");

      ConversionResult<?> primitiveResult = converter.convert(primitiveContext, "2.0");

      assertNotNull(wrapperResult);
      assertNotNull(primitiveResult);
      Object wrapperValue = wrapperResult.value();
      Object primitiveValue = primitiveResult.value();

      assertNotNull(wrapperValue);
      assertNotNull(primitiveValue);

      assertTrue(wrapperValue instanceof Float);
      assertTrue(primitiveValue instanceof Float);

      assertEquals(1.0F, wrapperValue);
      assertEquals(2.0F, primitiveValue);
    }

    @Test
    @DisplayName("should convert resolved property to a Double or primitive double")
    void primitiveTest4() {
      DefaultConverter converter = converterToTest();

      InvocationContext wrapperContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::doubleWrapperProperty, // This method returns a Double wrapper class
              externalizedProperties(converter));

      InvocationContext primitiveContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::doublePrimitiveProperty, // This method returns an double primitive
              externalizedProperties(converter));

      ConversionResult<?> wrapperResult = converter.convert(wrapperContext, "1.0");

      ConversionResult<?> primitiveResult = converter.convert(primitiveContext, "2.0");

      assertNotNull(wrapperResult);
      assertNotNull(primitiveResult);
      Object wrapperValue = wrapperResult.value();
      Object primitiveValue = primitiveResult.value();

      assertNotNull(wrapperValue);
      assertNotNull(primitiveValue);

      assertTrue(wrapperValue instanceof Double);
      assertTrue(primitiveValue instanceof Double);

      assertEquals(1.0D, wrapperValue);
      assertEquals(2.0D, primitiveValue);
    }

    @Test
    @DisplayName("should convert resolved property to a Short or primitive short")
    void primitiveTest5() {
      DefaultConverter converter = converterToTest();

      InvocationContext wrapperContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::shortWrapperProperty, // This method returns a Short wrapper class
              externalizedProperties(converter));

      InvocationContext primitiveContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::shortPrimitiveProperty, // This method returns a short primitive
              externalizedProperties(converter));

      ConversionResult<?> wrapperResult = converter.convert(wrapperContext, "1");

      ConversionResult<?> primitiveResult = converter.convert(primitiveContext, "2");

      assertNotNull(wrapperResult);
      assertNotNull(primitiveResult);
      Object wrapperValue = wrapperResult.value();
      Object primitiveValue = primitiveResult.value();

      assertNotNull(wrapperValue);
      assertNotNull(primitiveValue);

      assertTrue(wrapperValue instanceof Short);
      assertTrue(primitiveValue instanceof Short);

      assertEquals((short) 1, wrapperValue);
      assertEquals((short) 2, primitiveValue);
    }

    @Test
    @DisplayName("should convert resolved property to a Boolean or primitive boolean")
    void primitiveTest6() {
      DefaultConverter converter = converterToTest();

      InvocationContext wrapperContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::booleanWrapperProperty, // This method returns a Boolean wrapper class
              externalizedProperties(converter));

      InvocationContext primitiveContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::booleanPrimitiveProperty, // This method returns a boolean primitive
              externalizedProperties(converter));

      ConversionResult<?> wrapperResult = converter.convert(wrapperContext, "true");

      ConversionResult<?> primitiveResult = converter.convert(primitiveContext, "false");

      assertNotNull(wrapperResult);
      assertNotNull(primitiveResult);
      Object wrapperValue = wrapperResult.value();
      Object primitiveValue = primitiveResult.value();

      assertNotNull(wrapperValue);
      assertNotNull(primitiveValue);

      assertTrue(wrapperValue instanceof Boolean);
      assertTrue(primitiveValue instanceof Boolean);

      assertEquals(true, (Boolean) wrapperValue);
      assertEquals(false, (boolean) primitiveValue);
    }

    @Test
    @DisplayName("should convert resolved property to a Byte or primitive byte")
    void primitiveTest7() {
      DefaultConverter converter = converterToTest();

      InvocationContext wrapperContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::byteWrapperProperty, // This method returns a Byte wrapper class
              externalizedProperties(converter));

      InvocationContext primitiveContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::bytePrimitiveProperty, // This method returns a byte primitive
              externalizedProperties(converter));

      ConversionResult<?> wrapperResult = converter.convert(wrapperContext, "1");

      ConversionResult<?> primitiveResult = converter.convert(primitiveContext, "2");

      assertNotNull(wrapperResult);
      assertNotNull(primitiveResult);
      Object wrapperValue = wrapperResult.value();
      Object primitiveValue = primitiveResult.value();

      assertNotNull(wrapperValue);
      assertNotNull(primitiveValue);

      assertTrue(wrapperValue instanceof Byte);
      assertTrue(primitiveValue instanceof Byte);

      assertEquals((byte) 1, (Byte) wrapperValue);
      assertEquals((byte) 2, (byte) primitiveValue);
    }

    @Test
    @DisplayName("should convert resolved property to a List or Collection")
    void listTest1() {
      DefaultConverter converter = converterToTest();

      InvocationContext listContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::listProperty, // This method returns a List.
              externalizedProperties(converter));

      InvocationContext collectionContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::collectionProperty, // This method returns a Collection.
              externalizedProperties(converter));

      ConversionResult<?> listResult = converter.convert(listContext, "a,b,c");

      ConversionResult<?> collectionResult = converter.convert(collectionContext, "c,b,a");

      assertNotNull(listResult);
      assertNotNull(collectionResult);
      Object listValue = listResult.value();
      Object collectionValue = collectionResult.value();

      assertNotNull(listValue);
      assertNotNull(collectionValue);

      assertTrue(listValue instanceof List<?>);
      assertTrue(collectionValue instanceof Collection<?>);

      assertIterableEquals(Arrays.asList("a", "b", "c"), (List<?>) listValue);
      assertIterableEquals(Arrays.asList("c", "b", "a"), (Collection<?>) collectionValue);
    }

    @Test
    @DisplayName("should convert resolved property to a Set")
    void setTest1() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::setProperty, // This method returns a Set.
              externalizedProperties(converter));

      ConversionResult<?> setResult = converter.convert(context, "a,b,c");

      assertNotNull(setResult);
      Object setValue = setResult.value();

      assertNotNull(setValue);

      Set<?> expected = new HashSet<>(Arrays.asList("a", "b", "c"));
      assertEquals(expected, (Set<?>) setValue);
    }

    @Test
    @DisplayName("should convert resolved property to an array")
    void arrayTest1() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::arrayProperty, // This method returns a String[].
              externalizedProperties(converter));

      ConversionResult<?> arrayResult = converter.convert(context, "a,b,c");

      assertNotNull(arrayResult);
      Object arrayValue = arrayResult.value();

      assertNotNull(arrayValue);
      assertTrue(arrayValue.getClass().isArray());
      assertArrayEquals(new String[] {"a", "b", "c"}, (String[]) arrayValue);
    }

    @Test
    @DisplayName("should convert resolved property to enum")
    void enumTest1() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::enumProperty, externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, TestEnum.ONE.name());
      assertNotNull(result);

      Object testEnum = result.value();
      assertEquals(TestEnum.ONE, testEnum);
    }

    @Test
    @DisplayName("should convert value to LocalDateTime")
    void dateTimeTest1() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::localDateTime, externalizedProperties(converter));

      LocalDateTime input = LocalDateTime.of(2022, 12, 19, 12, 30, 0);
      String localDateTimeString = input.toString();

      ConversionResult<?> result = converter.convert(context, localDateTimeString);
      assertNotNull(result);

      Object localDateTime = result.value();
      assertTrue(localDateTime instanceof LocalDateTime);
      assertEquals(input, (LocalDateTime) localDateTime);
    }

    @Test
    @DisplayName("should convert value to LocalDate")
    void dateTimeTest2() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::localDate, externalizedProperties(converter));
      LocalDate input = LocalDate.of(2022, 12, 19);
      String localDateString = input.toString();

      ConversionResult<?> result = converter.convert(context, localDateString);

      assertNotNull(result);

      Object localDate = result.value();
      assertTrue(localDate instanceof LocalDate);
      assertEquals(input, (LocalDate) localDate);
    }

    @Test
    @DisplayName("should convert value to LocalTime")
    void dateTimeTest3() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::localTime, externalizedProperties(converter));

      LocalTime input = LocalTime.of(12, 30, 0);
      String localTimeString = input.toString();

      ConversionResult<?> result = converter.convert(context, localTimeString);
      assertNotNull(result);

      Object localDateTime = result.value();
      assertTrue(localDateTime instanceof LocalTime);
      assertEquals(input, (LocalTime) localDateTime);
    }

    @Test
    @DisplayName("should convert value to OffsetDateTime")
    void dateTimeTest4() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::offsetDateTime, externalizedProperties(converter));

      OffsetDateTime input =
          OffsetDateTime.of(
              LocalDate.of(2022, 12, 19), LocalTime.of(12, 30), ZoneOffset.ofHours(8));
      String offsetDateTimeString = input.toString();

      ConversionResult<?> result = converter.convert(context, offsetDateTimeString);
      assertNotNull(result);

      Object offsetDateTime = result.value();
      assertTrue(offsetDateTime instanceof OffsetDateTime);
      assertEquals(input, (OffsetDateTime) offsetDateTime);
    }

    @Test
    @DisplayName("should convert value to OffsetTime")
    void dateTimeTest5() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::offsetTime, externalizedProperties(converter));

      OffsetTime input = OffsetTime.of(LocalTime.of(12, 30), ZoneOffset.ofHours(8));
      String offsetTimeString = input.toString();

      ConversionResult<?> result = converter.convert(context, offsetTimeString);
      assertNotNull(result);

      Object offsetTime = result.value();
      assertTrue(offsetTime instanceof OffsetTime);
      assertEquals(input, (OffsetTime) offsetTime);
    }

    @Test
    @DisplayName("should convert value to ZonedDateTime")
    void dateTimeTest7() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::zonedDateTime, externalizedProperties(converter));

      ZonedDateTime input =
          ZonedDateTime.of(
              LocalDate.of(2022, 12, 19), LocalTime.of(12, 30), ZoneId.of("Asia/Manila"));
      String zonedDateTimeString = input.toString();

      ConversionResult<?> result = converter.convert(context, zonedDateTimeString);
      assertNotNull(result);

      Object zonedDateTime = result.value();
      assertTrue(zonedDateTime instanceof ZonedDateTime);
      assertEquals(input, (ZonedDateTime) zonedDateTime);
    }

    @Test
    @DisplayName("should convert value to Instant")
    void dateTimeTest8() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::instant, externalizedProperties(converter));

      Instant input = Instant.now();
      String instantString = input.toString();

      ConversionResult<?> result = converter.convert(context, instantString);
      assertNotNull(result);

      Object instant = result.value();
      assertTrue(instant instanceof Instant);
      assertEquals(input, (Instant) instant);
    }

    @Test
    @DisplayName("should convert value to DayOfWeek")
    void dateTimeTest9() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::dayOfWeek, externalizedProperties(converter));

      DayOfWeek input = DayOfWeek.SUNDAY;
      String dayOfWeekString = input.name();

      ConversionResult<?> result = converter.convert(context, dayOfWeekString);
      assertNotNull(result);

      Object dayOfWeek = result.value();
      assertTrue(dayOfWeek instanceof DayOfWeek);
      assertEquals(input, (DayOfWeek) dayOfWeek);
    }

    @Test
    @DisplayName("should convert value to Month")
    void dateTimeTest10() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::month, externalizedProperties(converter));

      Month input = Month.AUGUST;
      String monthString = input.name();

      ConversionResult<?> result = converter.convert(context, monthString);
      assertNotNull(result);

      Object month = result.value();
      assertTrue(month instanceof Month);
      assertEquals(input, (Month) month);
    }

    @Test
    @DisplayName("should convert value to MonthDay")
    void dateTimeTest11() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::monthDay, externalizedProperties(converter));

      MonthDay input = MonthDay.of(Month.DECEMBER, 19);
      String monthDayString = input.toString();

      ConversionResult<?> result = converter.convert(context, monthDayString);
      assertNotNull(result);

      Object monthDay = result.value();
      assertTrue(monthDay instanceof MonthDay);
      assertEquals(input, (MonthDay) monthDay);
    }

    @Test
    @DisplayName("should convert value to Year")
    void dateTimeTest12() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::year, externalizedProperties(converter));

      Year input = Year.of(2022);
      String yearString = input.toString();

      ConversionResult<?> result = converter.convert(context, yearString);
      assertNotNull(result);

      Object year = result.value();
      assertTrue(year instanceof Year);
      assertEquals(input, (Year) year);
    }

    @Test
    @DisplayName("should convert value to YearMonth")
    void dateTimeTest13() {
      DefaultConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::yearMonth, externalizedProperties(converter));

      YearMonth input = YearMonth.of(2022, Month.AUGUST);
      String yearMonthString = input.toString();

      ConversionResult<?> result = converter.convert(context, yearMonthString);
      assertNotNull(result);

      Object yearMonth = result.value();
      assertTrue(yearMonth instanceof YearMonth);
      assertEquals(input, (YearMonth) yearMonth);
    }
  }

  static DefaultConverter converterToTest() {
    return new DefaultConverter();
  }

  static ExternalizedProperties externalizedProperties(DefaultConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  static interface ProxyInterface {
    @ExternalizedProperty("property.int.primitive")
    int intPrimitiveProperty();

    @ExternalizedProperty("property.int.wrapper")
    Integer intWrapperProperty();

    @ExternalizedProperty("property.long.primitive")
    long longPrimitiveProperty();

    @ExternalizedProperty("property.long.wrapper")
    Long longWrapperProperty();

    @ExternalizedProperty("property.float.primitive")
    float floatPrimitiveProperty();

    @ExternalizedProperty("property.float.wrapper")
    Float floatWrapperProperty();

    @ExternalizedProperty("property.double.primitive")
    double doublePrimitiveProperty();

    @ExternalizedProperty("property.double.wrapper")
    Double doubleWrapperProperty();

    @ExternalizedProperty("property.boolean.primitive")
    boolean booleanPrimitiveProperty();

    @ExternalizedProperty("property.boolean.wrapper")
    Boolean booleanWrapperProperty();

    @ExternalizedProperty("property.short.primitive")
    short shortPrimitiveProperty();

    @ExternalizedProperty("property.short.wrapper")
    Short shortWrapperProperty();

    @ExternalizedProperty("property.byte.primitive")
    byte bytePrimitiveProperty();

    @ExternalizedProperty("property.byte.wrapper")
    Byte byteWrapperProperty();

    @ExternalizedProperty("property.array")
    String[] arrayProperty();

    @ExternalizedProperty("property.list")
    Collection<String> listProperty();

    @ExternalizedProperty("property.collection")
    Collection<String> collectionProperty();

    @ExternalizedProperty("property.set")
    Set<String> setProperty();

    @ExternalizedProperty("property.enum")
    TestEnum enumProperty();

    @ExternalizedProperty("property.localdatetime")
    LocalDateTime localDateTime();

    @ExternalizedProperty("localdate.property")
    LocalDate localDate();

    @ExternalizedProperty("property.localtime")
    LocalTime localTime();

    @ExternalizedProperty("property.offsetdatetime")
    OffsetDateTime offsetDateTime();

    @ExternalizedProperty("property.offsettime")
    OffsetTime offsetTime();

    @ExternalizedProperty("property.zoneddatetime")
    ZonedDateTime zonedDateTime();

    @ExternalizedProperty("property.instant")
    Instant instant();

    @ExternalizedProperty("property.dayofweek")
    DayOfWeek dayOfWeek();

    @ExternalizedProperty("property.month")
    Month month();

    @ExternalizedProperty("property.monthday")
    MonthDay monthDay();

    @ExternalizedProperty("property.year")
    Year year();

    @ExternalizedProperty("property.yearmonth.customformat")
    YearMonth yearMonth();

    @ExternalizedProperty("property.custom.type")
    CustomType customType();
  }

  static class CustomType {}

  static enum TestEnum {
    NONE,
    ONE,
    TWO,
    THREE
  }
}
