package io.github.joeljeremy.externalizedproperties.core.conversion.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.conversion.DateTimeFormat;
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
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class DateTimeConverterTests {
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class CanConvertMethod {
    @Test
    @DisplayName("should return true when target type is LocalDateTime")
    void test1() {
      DateTimeConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(LocalDateTime.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is LocalDate")
    void dateTimeTest2() {
      DateTimeConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(LocalDate.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is LocalTime")
    void test3() {
      DateTimeConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(LocalTime.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is OffsetDateTime")
    void test4() {
      DateTimeConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(OffsetDateTime.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is OffsetTime")
    void test5() {
      DateTimeConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(OffsetTime.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is ZonedDateTime")
    void test6() {
      DateTimeConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(ZonedDateTime.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is Instant")
    void test7() {
      DateTimeConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Instant.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is DayOfWeek")
    void test8() {
      DateTimeConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(DayOfWeek.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is Month")
    void test9() {
      DateTimeConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Month.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is MonthDay")
    void test10() {
      DateTimeConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(MonthDay.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is Year")
    void test11() {
      DateTimeConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(Year.class);
      assertTrue(canConvert);
    }

    @Test
    @DisplayName("should return true when target type is YearMonth")
    void test12() {
      DateTimeConverter converter = converterToTest();
      boolean canConvert = converter.canConvertTo(YearMonth.class);
      assertTrue(canConvert);
    }
  }

  @Nested
  class ConvertMethod {
    @Test
    @DisplayName("should convert value to LocalDateTime")
    void test1() {
      DateTimeConverter converter = converterToTest();

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
    void test2() {
      DateTimeConverter converter = converterToTest();

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
    void test3() {
      DateTimeConverter converter = converterToTest();

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
    void test4() {
      DateTimeConverter converter = converterToTest();

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
    void test5() {
      DateTimeConverter converter = converterToTest();

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
    void test7() {
      DateTimeConverter converter = converterToTest();

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
    void test8() {
      DateTimeConverter converter = converterToTest();

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
    void test9() {
      DateTimeConverter converter = converterToTest();

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
    void test10() {
      DateTimeConverter converter = converterToTest();

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
    void test11() {
      DateTimeConverter converter = converterToTest();

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
    void test12() {
      DateTimeConverter converter = converterToTest();

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
    void test13() {
      DateTimeConverter converter = converterToTest();

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

    @Test
    @DisplayName("should return skip return when target type is not supported")
    void test14() {
      DateTimeConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::notSupportedNotADateTimeClass, externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "1");
      assertNotNull(result);
      assertSame(ConversionResult.skip(), result);
    }

    /** Custom Date/Time Format Tests. */
    @Test
    @DisplayName("should convert value to LocalDateTime using custom date time format")
    void customFormatTest1() {
      DateTimeConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::localDateTimeCustomFormat, externalizedProperties(converter));

      LocalDateTime input = LocalDateTime.of(2022, 12, 19, 12, 30, 0);
      String localDateTimeString =
          DateTimeFormatter.ofPattern("MMM.dd.yyyy h:mm:ss a").format(input);

      ConversionResult<?> result = converter.convert(context, localDateTimeString);
      assertNotNull(result);

      Object localDateTime = result.value();
      assertTrue(localDateTime instanceof LocalDateTime);
      assertEquals(input, (LocalDateTime) localDateTime);
    }

    @Test
    @DisplayName("should convert value to LocalDate using custom date time format")
    void customFormatTest2() {
      DateTimeConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::localDateCustomFormat, externalizedProperties(converter));

      LocalDate input = LocalDate.of(2022, 12, 19);
      String localDateString = DateTimeFormatter.ofPattern("MMM.dd.yyyy").format(input);

      ConversionResult<?> result = converter.convert(context, localDateString);
      assertNotNull(result);

      Object localDate = result.value();
      assertTrue(localDate instanceof LocalDate);
      assertEquals(input, (LocalDate) localDate);
    }

    @Test
    @DisplayName("should convert value to LocalTime using custom date time format")
    void customFormatTest3() {
      DateTimeConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::localTimeCustomFormat, externalizedProperties(converter));

      LocalTime input = LocalTime.of(12, 30, 0);
      String localTimeString = DateTimeFormatter.ofPattern("h:mm:ss a").format(input);

      ConversionResult<?> result = converter.convert(context, localTimeString);
      assertNotNull(result);

      Object localDateTime = result.value();
      assertTrue(localDateTime instanceof LocalTime);
      assertEquals(input, (LocalTime) localDateTime);
    }

    @Test
    @DisplayName("should convert value to OffsetDateTime using custom date time format")
    void customFormatTest4() {
      DateTimeConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::offsetDateTimeCustomFormat, externalizedProperties(converter));

      OffsetDateTime input =
          OffsetDateTime.of(
              LocalDate.of(2022, 12, 19), LocalTime.of(12, 30), ZoneOffset.ofHours(8));
      String offsetDateTimeString =
          DateTimeFormatter.ofPattern("MMM.dd.yyyy h:mm:ss a (ZZZZ)").format(input);

      ConversionResult<?> result = converter.convert(context, offsetDateTimeString);
      assertNotNull(result);

      Object offsetDateTime = result.value();
      assertTrue(offsetDateTime instanceof OffsetDateTime);
      assertEquals(input, (OffsetDateTime) offsetDateTime);
    }

    @Test
    @DisplayName("should convert value to OffsetTime using custom date time format")
    void customFormatTest5() {
      DateTimeConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::offsetTimeCustomFormat, externalizedProperties(converter));

      OffsetTime input = OffsetTime.of(LocalTime.of(12, 30), ZoneOffset.ofHours(8));
      String offsetTimeString = DateTimeFormatter.ofPattern("h:mm:ss a (ZZZZ)").format(input);

      ConversionResult<?> result = converter.convert(context, offsetTimeString);
      assertNotNull(result);

      Object offsetTime = result.value();
      assertTrue(offsetTime instanceof OffsetTime);
      assertEquals(input, (OffsetTime) offsetTime);
    }

    @Test
    @DisplayName("should convert value to ZonedDateTime using custom date time format")
    void customFormatTest6() {
      DateTimeConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::zonedDateTimeCustomFormat, externalizedProperties(converter));

      ZonedDateTime input =
          ZonedDateTime.of(
              LocalDate.of(2022, 12, 19), LocalTime.of(12, 30), ZoneId.of("Asia/Manila"));
      String zonedDateTimeString =
          DateTimeFormatter.ofPattern("MMM.dd.yyyy h:mm:ss a (VV) (ZZZZ)").format(input);

      ConversionResult<?> result = converter.convert(context, zonedDateTimeString);
      assertNotNull(result);

      Object zonedDateTime = result.value();
      assertTrue(zonedDateTime instanceof ZonedDateTime);
      assertEquals(input, (ZonedDateTime) zonedDateTime);
    }

    @Test
    @DisplayName("should convert value to MonthDay using custom date time format")
    void customFormatTest7() {
      DateTimeConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::monthDayCustomFormat, externalizedProperties(converter));

      MonthDay input = MonthDay.of(Month.DECEMBER, 19);
      String monthDayString = DateTimeFormatter.ofPattern("MMMM.dd").format(input);

      ConversionResult<?> result = converter.convert(context, monthDayString);
      assertNotNull(result);

      Object monthDay = result.value();
      assertTrue(monthDay instanceof MonthDay);
      assertEquals(input, (MonthDay) monthDay);
    }

    @Test
    @DisplayName("should convert value to Year using custom date time format")
    void customFormatTest8() {
      DateTimeConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::yearCustomFormat, externalizedProperties(converter));

      Year input = Year.of(2022);
      String yearString = DateTimeFormatter.ofPattern("yy").format(input);

      ConversionResult<?> result = converter.convert(context, yearString);
      assertNotNull(result);

      Object year = result.value();
      assertTrue(year instanceof Year);
      assertEquals(input, (Year) year);
    }

    @Test
    @DisplayName("should convert value to YearMonth using custom date time format")
    void customFormatTest9() {
      DateTimeConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::yearMonthCustomFormat, externalizedProperties(converter));

      YearMonth input = YearMonth.of(2022, Month.AUGUST);
      String yearMonthString = DateTimeFormatter.ofPattern("yyyy MMMM").format(input);

      ConversionResult<?> result = converter.convert(context, yearMonthString);
      assertNotNull(result);

      Object yearMonth = result.value();
      assertTrue(yearMonth instanceof YearMonth);
      assertEquals(input, (YearMonth) yearMonth);
    }

    @Test
    @DisplayName("should return skip return when target type is not supported")
    void customFormatTest10() {
      DateTimeConverter converter = converterToTest();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::notSupportedNotADateTimeClass, externalizedProperties(converter));

      ConversionResult<?> result = converter.convert(context, "1");
      assertNotNull(result);
      assertSame(ConversionResult.skip(), result);
    }
  }

  static DateTimeConverter converterToTest() {
    return new DateTimeConverter();
  }

  static ExternalizedProperties externalizedProperties(DateTimeConverter converterToTest) {
    return ExternalizedProperties.builder().converters(converterToTest).build();
  }

  static interface ProxyInterface {
    @ExternalizedProperty("property.localdatetime")
    LocalDateTime localDateTime();

    @ExternalizedProperty("property.localdatetime.customformat")
    @DateTimeFormat("MMM.dd.yyyy h:mm:ss a")
    LocalDateTime localDateTimeCustomFormat();

    @ExternalizedProperty("localdate.property")
    LocalDate localDate();

    @ExternalizedProperty("property.localdate.customformat")
    @DateTimeFormat("MMM.dd.yyyy")
    LocalDate localDateCustomFormat();

    @ExternalizedProperty("property.localtime")
    LocalTime localTime();

    @ExternalizedProperty("property.localtime.customformat")
    @DateTimeFormat("h:mm:ss a")
    LocalTime localTimeCustomFormat();

    @ExternalizedProperty("property.offsetdatetime")
    OffsetDateTime offsetDateTime();

    @ExternalizedProperty("property.offsetdatetime.customformat")
    @DateTimeFormat("MMM.dd.yyyy h:mm:ss a (ZZZZ)")
    OffsetDateTime offsetDateTimeCustomFormat();

    @ExternalizedProperty("property.offsettime")
    OffsetTime offsetTime();

    @ExternalizedProperty("property.offsettime.customformat")
    @DateTimeFormat("h:mm:ss a (ZZZZ)")
    OffsetTime offsetTimeCustomFormat();

    @ExternalizedProperty("property.zoneddatetime")
    ZonedDateTime zonedDateTime();

    @ExternalizedProperty("property.zoneddatetime.customformat")
    @DateTimeFormat("MMM.dd.yyyy h:mm:ss a (VV) (ZZZZ)")
    ZonedDateTime zonedDateTimeCustomFormat();

    @ExternalizedProperty("property.instant")
    Instant instant();

    @ExternalizedProperty("property.dayofweek")
    DayOfWeek dayOfWeek();

    @ExternalizedProperty("property.month")
    Month month();

    @ExternalizedProperty("property.monthday")
    MonthDay monthDay();

    @ExternalizedProperty("property.monthday.customformat")
    @DateTimeFormat("MMMM.dd")
    MonthDay monthDayCustomFormat();

    @ExternalizedProperty("property.year")
    Year year();

    @ExternalizedProperty("property.year.customformat")
    @DateTimeFormat("yy")
    Year yearCustomFormat();

    @ExternalizedProperty("property.yearmonth")
    YearMonth yearMonth();

    @ExternalizedProperty("property.yearmonth.customformat")
    @DateTimeFormat("yyyy MMMM")
    YearMonth yearMonthCustomFormat();

    @ExternalizedProperty("property.not.supported")
    int notSupportedNotADateTimeClass();
  }
}
