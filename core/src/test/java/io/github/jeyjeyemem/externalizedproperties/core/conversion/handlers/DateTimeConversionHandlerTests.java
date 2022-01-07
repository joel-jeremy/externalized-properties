package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubProxyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.DateTimeProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateTimeConversionHandlerTests {
    @Nested
    class CanConvertMethod {
        @Test
        @DisplayName("should return true when target type is null.")
        public void test1() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is LocalDateTime.")
        public void test2() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(LocalDateTime.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is LocalDate.")
        public void test3() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(LocalDate.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is LocalTime.")
        public void test4() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(LocalTime.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is OffsetDateTime.")
        public void test5() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(OffsetDateTime.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is OffsetTime.")
        public void test6() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(OffsetTime.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is ZonedDateTime.")
        public void test7() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(ZonedDateTime.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is Instant.")
        public void test8() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Instant.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is DayOfWeek.")
        public void test9() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(DayOfWeek.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is Month.")
        public void test10() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Month.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is MonthDay.")
        public void test11() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(MonthDay.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is Year.")
        public void test12() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Year.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is YearMonth.")
        public void test13() {
            DateTimeConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(YearMonth.class);
            assertTrue(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            DateTimeConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert value to LocalDateTime.")
        public void test2() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "localDateTime"
                );
            
            Converter converter = new InternalConverter(handler);
            LocalDateTime input = LocalDateTime.of(2022, 12, 19, 12, 30, 0);
            String localDateTimeString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                localDateTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object localDateTime = result.value();
            assertTrue(localDateTime instanceof LocalDateTime);
            assertEquals(input, (LocalDateTime)localDateTime);
        }

        @Test
        @DisplayName("should convert value to LocalDate.")
        public void test3() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "localDate"
                );
            
            Converter converter = new InternalConverter(handler);
            LocalDate input = LocalDate.of(2022, 12, 19);
            String localDateString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                localDateString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object localDate = result.value();
            assertTrue(localDate instanceof LocalDate);
            assertEquals(input, (LocalDate)localDate);
        }

        @Test
        @DisplayName("should convert value to LocalTime.")
        public void test4() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "localTime"
                );
            
            Converter converter = new InternalConverter(handler);
            LocalTime input = LocalTime.of(12, 30, 0);
            String localTimeString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                localTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object localDateTime = result.value();
            assertTrue(localDateTime instanceof LocalTime);
            assertEquals(input, (LocalTime)localDateTime);
        }

        @Test
        @DisplayName("should convert value to OffsetDateTime.")
        public void test5() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "offsetDateTime"
                );
            
            Converter converter = new InternalConverter(handler);
            OffsetDateTime input = OffsetDateTime.of(
                LocalDate.of(2022, 12, 19), 
                LocalTime.of(12, 30), 
                ZoneOffset.ofHours(8)
            );
            String offsetDateTimeString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                offsetDateTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object offsetDateTime = result.value();
            assertTrue(offsetDateTime instanceof OffsetDateTime);
            assertEquals(input, (OffsetDateTime)offsetDateTime);
        }

        @Test
        @DisplayName("should convert value to OffsetTime.")
        public void test6() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "offsetTime"
                );
            
            Converter converter = new InternalConverter(handler);
            OffsetTime input = OffsetTime.of( 
                LocalTime.of(12, 30), 
                ZoneOffset.ofHours(8)
            );
            String offsetTimeString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                offsetTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object offsetTime = result.value();
            assertTrue(offsetTime instanceof OffsetTime);
            assertEquals(input, (OffsetTime)offsetTime);
        }

        @Test
        @DisplayName("should convert value to ZonedDateTime.")
        public void test7() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "zonedDateTime"
                );
            
            Converter converter = new InternalConverter(handler);
            ZonedDateTime input = ZonedDateTime.of(
                LocalDate.of(2022, 12, 19),
                LocalTime.of(12, 30), 
                ZoneId.of("Asia/Manila")
            );
            String zonedDateTimeString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                zonedDateTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object zonedDateTime = result.value();
            assertTrue(zonedDateTime instanceof ZonedDateTime);
            assertEquals(input, (ZonedDateTime)zonedDateTime);
        }

        @Test
        @DisplayName("should convert value to Instant.")
        public void test8() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "instant"
                );
            
            Converter converter = new InternalConverter(handler);
            Instant input = Instant.now();
            String instantString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                instantString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object instant = result.value();
            assertTrue(instant instanceof Instant);
            assertEquals(input, (Instant)instant);
        }

        @Test
        @DisplayName("should convert value to DayOfWeek.")
        public void test9() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "dayOfWeek"
                );
            
            Converter converter = new InternalConverter(handler);
            DayOfWeek input = DayOfWeek.SUNDAY;
            String dayOfWeekString = input.name();
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                dayOfWeekString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object dayOfWeek = result.value();
            assertTrue(dayOfWeek instanceof DayOfWeek);
            assertEquals(input, (DayOfWeek)dayOfWeek);
        }

        @Test
        @DisplayName("should convert value to Month.")
        public void test10() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "month"
                );
            
            Converter converter = new InternalConverter(handler);
            Month input = Month.AUGUST;
            String monthString = input.name();
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                monthString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object month = result.value();
            assertTrue(month instanceof Month);
            assertEquals(input, (Month)month);
        }

        @Test
        @DisplayName("should convert value to MonthDay.")
        public void test11() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "monthDay"
                );
            
            Converter converter = new InternalConverter(handler);
            MonthDay input = MonthDay.of(Month.DECEMBER, 19);
            String monthDayString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                monthDayString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object monthDay = result.value();
            assertTrue(monthDay instanceof MonthDay);
            assertEquals(input, (MonthDay)monthDay);
        }

        @Test
        @DisplayName("should convert value to Year.")
        public void test12() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "year"
                );
            
            Converter converter = new InternalConverter(handler);
            Year input = Year.of(2022);
            String yearString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                yearString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object year = result.value();
            assertTrue(year instanceof Year);
            assertEquals(input, (Year)year);
        }

        @Test
        @DisplayName("should convert value to YearMonth.")
        public void test13() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "yearMonth"
                );
            
            Converter converter = new InternalConverter(handler);
            YearMonth input = YearMonth.of(2022, Month.AUGUST);
            String yearMonthString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                yearMonthString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object yearMonth = result.value();
            assertTrue(yearMonth instanceof YearMonth);
            assertEquals(input, (YearMonth)yearMonth);
        }

        @Test
        @DisplayName("should return skip return when target type is not supported.")
        public void test14() {
            DateTimeConversionHandler handler = handlerToTest();
            
            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );

            Converter converter = new InternalConverter(handler);
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                "1"
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            assertSame(ConversionResult.skip(), result);
        }

        /**
         * Custom Date/Time Format Tests.
         */

        @Test
        @DisplayName("should convert value to LocalDateTime using custom date time format.")
        public void customFormatTest1() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "localDateTimeCustomFormat"
                );
            
            Converter converter = new InternalConverter(handler);
            LocalDateTime input = LocalDateTime.of(2022, 12, 19, 12, 30, 0);
            String localDateTimeString = 
                DateTimeFormatter.ofPattern("MMM.dd.yyyy h:mm:ss a").format(input);
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                localDateTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object localDateTime = result.value();
            assertTrue(localDateTime instanceof LocalDateTime);
            assertEquals(input, (LocalDateTime)localDateTime);
        }

        @Test
        @DisplayName("should convert value to LocalDate using custom date time format.")
        public void customFormatTest2() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "localDateCustomFormat"
                );
            
            Converter converter = new InternalConverter(handler);
            LocalDate input = LocalDate.of(2022, 12, 19);
            String localDateString = 
                DateTimeFormatter.ofPattern("MMM.dd.yyyy").format(input);
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                localDateString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object localDate = result.value();
            assertTrue(localDate instanceof LocalDate);
            assertEquals(input, (LocalDate)localDate);
        }

        @Test
        @DisplayName("should convert value to LocalTime using custom date time format.")
        public void customFormatTest3() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "localTimeCustomFormat"
                );
            
            Converter converter = new InternalConverter(handler);
            LocalTime input = LocalTime.of(12, 30, 0);
            String localTimeString = 
                DateTimeFormatter.ofPattern("h:mm:ss a").format(input);
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                localTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object localDateTime = result.value();
            assertTrue(localDateTime instanceof LocalTime);
            assertEquals(input, (LocalTime)localDateTime);
        }

        @Test
        @DisplayName("should convert value to OffsetDateTime using custom date time format.")
        public void customFormatTest4() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "offsetDateTimeCustomFormat"
                );
            
            Converter converter = new InternalConverter(handler);
            OffsetDateTime input = OffsetDateTime.of(
                LocalDate.of(2022, 12, 19), 
                LocalTime.of(12, 30), 
                ZoneOffset.ofHours(8)
            );
            String offsetDateTimeString = 
                DateTimeFormatter.ofPattern("MMM.dd.yyyy h:mm:ss a (ZZZZ)").format(input);
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                offsetDateTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object offsetDateTime = result.value();
            assertTrue(offsetDateTime instanceof OffsetDateTime);
            assertEquals(input, (OffsetDateTime)offsetDateTime);
        }

        @Test
        @DisplayName("should convert value to OffsetTime using custom date time format.")
        public void customFormatTest5() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "offsetTimeCustomFormat"
                );
            
            Converter converter = new InternalConverter(handler);
            OffsetTime input = OffsetTime.of( 
                LocalTime.of(12, 30), 
                ZoneOffset.ofHours(8)
            );
            String offsetTimeString = 
                DateTimeFormatter.ofPattern("h:mm:ss a (ZZZZ)").format(input);
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                offsetTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object offsetTime = result.value();
            assertTrue(offsetTime instanceof OffsetTime);
            assertEquals(input, (OffsetTime)offsetTime);
        }

        @Test
        @DisplayName("should convert value to ZonedDateTime using custom date time format.")
        public void customFormatTest6() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "zonedDateTimeCustomFormat"
                );
            
            Converter converter = new InternalConverter(handler);
            ZonedDateTime input = ZonedDateTime.of(
                LocalDate.of(2022, 12, 19),
                LocalTime.of(12, 30), 
                ZoneId.of("Asia/Manila")
            );
            String zonedDateTimeString = 
                DateTimeFormatter.ofPattern("MMM.dd.yyyy h:mm:ss a (VV) (ZZZZ)")
                    .format(input);
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                zonedDateTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object zonedDateTime = result.value();
            assertTrue(zonedDateTime instanceof ZonedDateTime);
            assertEquals(input, (ZonedDateTime)zonedDateTime);
        }

        @Test
        @DisplayName("should convert value to MonthDay using custom date time format.")
        public void customFormatTest7() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "monthDayCustomFormat"
                );
            
            Converter converter = new InternalConverter(handler);
            MonthDay input = MonthDay.of(Month.DECEMBER, 19);
            String monthDayString = 
                DateTimeFormatter.ofPattern("MMMM.dd").format(input);
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                monthDayString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object monthDay = result.value();
            assertTrue(monthDay instanceof MonthDay);
            assertEquals(input, (MonthDay)monthDay);
        }

        @Test
        @DisplayName("should convert value to Year using custom date time format.")
        public void customFormatTest8() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "yearCustomFormat"
                );
            
            Converter converter = new InternalConverter(handler);
            Year input = Year.of(2022);
            String yearString = DateTimeFormatter.ofPattern("yy").format(input);
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                yearString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object year = result.value();
            assertTrue(year instanceof Year);
            assertEquals(input, (Year)year);
        }

        @Test
        @DisplayName("should convert value to YearMonth using custom date time format.")
        public void customFormatTest9() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    DateTimeProxyInterface.class,
                    "yearMonthCustomFormat"
                );
            
            Converter converter = new InternalConverter(handler);
            YearMonth input = YearMonth.of(2022, Month.AUGUST);
            String yearMonthString = 
                DateTimeFormatter.ofPattern("yyyy MMMM").format(input);
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                yearMonthString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object yearMonth = result.value();
            assertTrue(yearMonth instanceof YearMonth);
            assertEquals(input, (YearMonth)yearMonth);
        }

        @Test
        @DisplayName("should return skip return when target type is not supported.")
        public void customFormatTest10() {
            DateTimeConversionHandler handler = handlerToTest();

            ProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );
            
            Converter converter = new InternalConverter(handler);
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo,
                "1"
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            assertSame(ConversionResult.skip(), result);
        }

        /**
         * Non-proxy conversion.
         */

        @Test
        @DisplayName("should convert value to LocalDateTime.")
        public void nonProxyTest1() {
            DateTimeConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            LocalDateTime input = LocalDateTime.of(2022, 12, 19, 12, 30, 0);
            String localDateTimeString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                LocalDateTime.class,
                localDateTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object localDateTime = result.value();
            assertTrue(localDateTime instanceof LocalDateTime);
            assertEquals(input, (LocalDateTime)localDateTime);
        }

        @Test
        @DisplayName("should convert value to LocalDate.")
        public void nonProxyTest2() {
            DateTimeConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            LocalDate input = LocalDate.of(2022, 12, 19);
            String localDateString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                LocalDate.class,
                localDateString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object localDate = result.value();
            assertTrue(localDate instanceof LocalDate);
            assertEquals(input, (LocalDate)localDate);
        }

        @Test
        @DisplayName("should convert value to LocalTime.")
        public void nonProxyTest3() {
            DateTimeConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            LocalTime input = LocalTime.of(12, 30, 0);
            String localTimeString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                LocalTime.class,
                localTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object localDateTime = result.value();
            assertTrue(localDateTime instanceof LocalTime);
            assertEquals(input, (LocalTime)localDateTime);
        }

        @Test
        @DisplayName("should convert value to OffsetDateTime.")
        public void nonProxyTest4() {
            DateTimeConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            OffsetDateTime input = OffsetDateTime.of(
                LocalDate.of(2022, 12, 19), 
                LocalTime.of(12, 30), 
                ZoneOffset.ofHours(8)
            );
            String offsetDateTimeString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                OffsetDateTime.class,
                offsetDateTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object offsetDateTime = result.value();
            assertTrue(offsetDateTime instanceof OffsetDateTime);
            assertEquals(input, (OffsetDateTime)offsetDateTime);
        }

        @Test
        @DisplayName("should convert value to OffsetTime.")
        public void nonProxyTest5() {
            DateTimeConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            OffsetTime input = OffsetTime.of( 
                LocalTime.of(12, 30), 
                ZoneOffset.ofHours(8)
            );
            String offsetTimeString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                OffsetTime.class,
                offsetTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object offsetTime = result.value();
            assertTrue(offsetTime instanceof OffsetTime);
            assertEquals(input, (OffsetTime)offsetTime);
        }

        @Test
        @DisplayName("should convert value to ZonedDateTime.")
        public void nonProxyTest6() {
            DateTimeConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            ZonedDateTime input = ZonedDateTime.of(
                LocalDate.of(2022, 12, 19),
                LocalTime.of(12, 30), 
                ZoneId.of("Asia/Manila")
            );
            String zonedDateTimeString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                ZonedDateTime.class,
                zonedDateTimeString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object zonedDateTime = result.value();
            assertTrue(zonedDateTime instanceof ZonedDateTime);
            assertEquals(input, (ZonedDateTime)zonedDateTime);
        }

        @Test
        @DisplayName("should convert value to Instant.")
        public void nonProxyTest7() {
            DateTimeConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            Instant input = Instant.now();
            String instantString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                Instant.class,
                instantString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object instant = result.value();
            assertTrue(instant instanceof Instant);
            assertEquals(input, (Instant)instant);
        }

        @Test
        @DisplayName("should convert value to DayOfWeek.")
        public void nonProxyTest8() {
            DateTimeConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            DayOfWeek input = DayOfWeek.SUNDAY;
            String dayOfWeekString = input.name();
            ConversionContext context = new ConversionContext(
                converter,
                DayOfWeek.class,
                dayOfWeekString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object dayOfWeek = result.value();
            assertTrue(dayOfWeek instanceof DayOfWeek);
            assertEquals(input, (DayOfWeek)dayOfWeek);
        }

        @Test
        @DisplayName("should convert value to Month.")
        public void nonProxyTest9() {
            DateTimeConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            Month input = Month.AUGUST;
            String monthString = input.name();
            ConversionContext context = new ConversionContext(
                converter,
                Month.class,
                monthString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object month = result.value();
            assertTrue(month instanceof Month);
            assertEquals(input, (Month)month);
        }

        @Test
        @DisplayName("should convert value to MonthDay.")
        public void nonProxyTest10() {
            DateTimeConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            MonthDay input = MonthDay.of(Month.DECEMBER, 19);
            String monthDayString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                MonthDay.class,
                monthDayString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object monthDay = result.value();
            assertTrue(monthDay instanceof MonthDay);
            assertEquals(input, (MonthDay)monthDay);
        }

        @Test
        @DisplayName("should convert value to Year.")
        public void nonProxyTest11() {
            DateTimeConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            Year input = Year.of(2022);
            String yearString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                Year.class,
                yearString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object year = result.value();
            assertTrue(year instanceof Year);
            assertEquals(input, (Year)year);
        }

        @Test
        @DisplayName("should convert value to YearMonth.")
        public void nonProxyTest12() {
            DateTimeConversionHandler handler = handlerToTest();
            
            Converter converter = new InternalConverter(handler);
            YearMonth input = YearMonth.of(2022, Month.AUGUST);
            String yearMonthString = input.toString();
            ConversionContext context = new ConversionContext(
                converter,
                YearMonth.class,
                yearMonthString
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            
            Object yearMonth = result.value();
            assertTrue(yearMonth instanceof YearMonth);
            assertEquals(input, (YearMonth)yearMonth);
        }

        @Test
        @DisplayName("should return skip return when target type is not supported.")
        public void nonProxyTest13() {
            DateTimeConversionHandler handler = handlerToTest();

            Converter converter = new InternalConverter(handler);
            ConversionContext context = new ConversionContext(
                converter,
                Integer.class,
                "1"
            );

            ConversionResult<?> result = handler.convert(context);
            assertNotNull(result);
            assertSame(ConversionResult.skip(), result);
        }
    }

    private DateTimeConversionHandler handlerToTest() {
        return new DateTimeConversionHandler();
    }
}
