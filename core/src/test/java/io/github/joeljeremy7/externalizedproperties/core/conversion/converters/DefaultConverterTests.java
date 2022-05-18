package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.EnumConverterTests.TestEnum;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultConverterTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);
    
    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        void test1() {
            ConverterProvider<DefaultConverter> provider = 
                DefaultConverter.provider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        void test2() {
            ConverterProvider<DefaultConverter> provider = 
                DefaultConverter.provider();
            
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .converters(provider)
                    .build();
            
            assertNotNull(
                provider.get(
                    externalizedProperties,
                    new RootConverter(externalizedProperties, provider)
                )
            );
        }
    }
    
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return true when target type is an Integer.")
        void primitiveTest1() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Integer.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive int.")
        void primitiveTest2() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Integer.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Long.")
        void primitiveTest3() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Long.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive long.")
        void primitiveTest4() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Long.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Float.")
        void primitiveTest5() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Float.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive float.")
        void primitiveTest6() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Float.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Double.")
        void primitiveTest7() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Double.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a primitive double.")
        void primitiveTest8() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Double.TYPE);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a List.")
        void listTest1() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(List.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Collection.")
        void listTest2() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Collection.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Set.")
        void setTest1() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Set.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an Array.")
        void arrayTest1() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(String[].class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an enum.")
        void enumTest1() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(TestEnum.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is LocalDateTime.")
        void dateTimeTest1() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(LocalDateTime.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is LocalDate.")
        void dateTimeTest2() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(LocalDate.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is LocalTime.")
        void dateTimeTest3() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(LocalTime.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is OffsetDateTime.")
        void dateTimeTest4() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(OffsetDateTime.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is OffsetTime.")
        void dateTimeTest5() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(OffsetTime.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is ZonedDateTime.")
        void dateTimeTest6() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(ZonedDateTime.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is Instant.")
        void dateTimeTest7() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Instant.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is DayOfWeek.")
        void dateTimeTest8() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(DayOfWeek.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is Month.")
        void dateTimeTest9() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Month.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is MonthDay.")
        void dateTimeTest10() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(MonthDay.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is Year.")
        void dateTimeTest11() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Year.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is YearMonth.")
        void dateTimeTest12() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(YearMonth.class);
            assertTrue(canConvert);
        }
        
        @Test
        @DisplayName("should return false when target type is null.")
        void unsupportedTest1() {
            DefaultConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not supported.")
        void unsupportedTest2() {
            DefaultConverter converter = converterToTest();
            // Unsupported.
            boolean canConvert = converter.canConvertTo(Properties.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should return skip result when target type is not supported.")
        void unsupportedTest1() {
            DefaultConverter converter = converterToTest();

            // Not primitive, List/Collection, array or Optional.
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::customType // This method returns a CustomType.
            );

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                ""
            );
            assertEquals(ConversionResult.skip(), result);
        }

        @Test
        @DisplayName("should convert resolved property to an Integer or primitive int.")
        void primitiveTest1() {
            DefaultConverter converter = converterToTest();

            ProxyMethod wrapperProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::intWrapperProperty // This method returns a Integer wrapper class
            );

            ProxyMethod primitiveProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::intPrimitiveProperty // This method returns an int primitive
            );

            ConversionResult<?> wrapperResult = converter.convert(
                wrapperProxyMethod,
                "1"
            );

            ConversionResult<?> primitiveResult = converter.convert(
                primitiveProxyMethod,
                "2"
            );

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
        @DisplayName("should convert resolved property to a Long or primitive long.")
        void primitiveTest2() {
            DefaultConverter converter = converterToTest();

            ProxyMethod wrapperProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::longWrapperProperty // This method returns a Long wrapper class
            );

            ProxyMethod primitiveProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::longPrimitiveProperty // This method returns an long primitive
            );

            ConversionResult<?> wrapperResult = converter.convert(
                wrapperProxyMethod,
                "1"
            );

            ConversionResult<?> primitiveResult = converter.convert(
                primitiveProxyMethod,
                "2"
            );
            
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
        @DisplayName("should convert resolved property to a Float or primitive float.")
        void primitiveTest3() {
            DefaultConverter converter = converterToTest();

            ProxyMethod wrapperProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::floatWrapperProperty // This method returns a Float wrapper class
            );

            ProxyMethod primitiveProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::floatPrimitiveProperty // This method returns an float primitive
            );

            ConversionResult<?> wrapperResult = converter.convert(
                wrapperProxyMethod,
                "1.0"
            );

            ConversionResult<?> primitiveResult = converter.convert(
                primitiveProxyMethod,
                "2.0"
            );
            
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
        @DisplayName("should convert resolved property to a Double or primitive double.")
        void primitiveTest4() {
            DefaultConverter converter = converterToTest();

            ProxyMethod wrapperProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::doubleWrapperProperty // This method returns a Double wrapper class
            );

            ProxyMethod primitiveProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::doublePrimitiveProperty // This method returns an double primitive
            );

            ConversionResult<?> wrapperResult = converter.convert(
                wrapperProxyMethod,
                "1.0"
            );

            ConversionResult<?> primitiveResult = converter.convert(
                primitiveProxyMethod,
                "2.0"
            );
            
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
        @DisplayName("should convert resolved property to a Short or primitive short.")
        void primitiveTest5() {
            DefaultConverter converter = converterToTest();

            ProxyMethod wrapperProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::shortWrapperProperty // This method returns a Short wrapper class
            );

            ProxyMethod primitiveProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::shortPrimitiveProperty // This method returns a short primitive
            );

            ConversionResult<?> wrapperResult = converter.convert(
                wrapperProxyMethod,
                "1"
            );

            ConversionResult<?> primitiveResult = converter.convert(
                primitiveProxyMethod,
                "2"
            );
            
            assertNotNull(wrapperResult);
            assertNotNull(primitiveResult);
            Object wrapperValue = wrapperResult.value();
            Object primitiveValue = primitiveResult.value();

            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Short);
            assertTrue(primitiveValue instanceof Short);

            assertEquals((short)1, wrapperValue);
            assertEquals((short)2, primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Boolean or primitive boolean.")
        void primitiveTest6() {
            DefaultConverter converter = converterToTest();

            ProxyMethod wrapperProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::booleanWrapperProperty // This method returns a Boolean wrapper class
            );

            ProxyMethod primitiveProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::booleanPrimitiveProperty // This method returns a boolean primitive
            );

            ConversionResult<?> wrapperResult = converter.convert(
                wrapperProxyMethod,
                "true"
            );

            ConversionResult<?> primitiveResult = converter.convert(
                primitiveProxyMethod,
                "false"
            );
            
            assertNotNull(wrapperResult);
            assertNotNull(primitiveResult);
            Object wrapperValue = wrapperResult.value();
            Object primitiveValue = primitiveResult.value();

            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Boolean);
            assertTrue(primitiveValue instanceof Boolean);

            assertEquals(true, (Boolean)wrapperValue);
            assertEquals(false, (boolean)primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a Byte or primitive byte.")
        void primitiveTest7() {
            DefaultConverter converter = converterToTest();

            ProxyMethod wrapperProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::byteWrapperProperty // This method returns a Byte wrapper class
            );

            ProxyMethod primitiveProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::bytePrimitiveProperty // This method returns a byte primitive
            );

            ConversionResult<?> wrapperResult = converter.convert(
                wrapperProxyMethod,
                "1"
            );

            ConversionResult<?> primitiveResult = converter.convert(
                primitiveProxyMethod,
                "2"
            );
            
            assertNotNull(wrapperResult);
            assertNotNull(primitiveResult);
            Object wrapperValue = wrapperResult.value();
            Object primitiveValue = primitiveResult.value();

            assertNotNull(wrapperValue);
            assertNotNull(primitiveValue);

            assertTrue(wrapperValue instanceof Byte);
            assertTrue(primitiveValue instanceof Byte);

            assertEquals((byte)1, (Byte)wrapperValue);
            assertEquals((byte)2, (byte)primitiveValue);
        }

        @Test
        @DisplayName("should convert resolved property to a List or Collection.")
        void listTest1() {
            DefaultConverter converter = converterToTest();

            ProxyMethod listProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::listProperty // This method returns a List.
            );

            ProxyMethod collectionProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::collectionProperty // This method returns a Collection.
            );

            ConversionResult<?> listResult = converter.convert(
                listProxyMethod,
                "a,b,c"
            );

            ConversionResult<?> collectionResult = converter.convert(
                collectionProxyMethod,
                "c,b,a"
            );
            
            assertNotNull(listResult);
            assertNotNull(collectionResult);
            Object listValue = listResult.value();
            Object collectionValue = collectionResult.value();

            assertNotNull(listValue);
            assertNotNull(collectionValue);

            assertTrue(listValue instanceof List<?>);
            assertTrue(collectionValue instanceof Collection<?>);

            assertIterableEquals(
                Arrays.asList("a", "b", "c"), 
                (List<?>)listValue
            );
            assertIterableEquals(
                Arrays.asList("c", "b", "a"), 
                (Collection<?>)collectionValue
            );
        }

        @Test
        @DisplayName("should convert resolved property to a Set.")
        void setTest1() {
            DefaultConverter converter = converterToTest();

            ProxyMethod setProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::setProperty // This method returns a Set.
            );

            ConversionResult<?> setResult = converter.convert(
                setProxyMethod,
                "a,b,c"
            );
            
            assertNotNull(setResult);
            Object setValue = setResult.value();

            assertNotNull(setValue);

            Set<?> expected = new HashSet<>(Arrays.asList("a", "b", "c"));
            assertEquals(expected, (Set<?>)setValue);
        }

        @Test
        @DisplayName("should convert resolved property to an array.")
        void arrayTest1() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::arrayProperty // This method returns a String[].
            );
            
            ConversionResult<?> arrayResult = converter.convert(
                proxyMethod,
                "a,b,c"
            );

            assertNotNull(arrayResult);
            Object arrayValue = arrayResult.value();
            
            assertNotNull(arrayValue);
            assertTrue(arrayValue.getClass().isArray());
            assertArrayEquals(
                new String[] { "a", "b", "c" }, 
                (String[])arrayValue
            );
        }

        @Test
        @DisplayName("should convert resolved property to enum.")
        void enumTest1() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::enumProperty
            );

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                TestEnum.ONE.name()
            );
            assertNotNull(result);

            Object testEnum = result.value();
            assertEquals(TestEnum.ONE, testEnum);
        }

        @Test
        @DisplayName("should convert value to LocalDateTime.")
        void dateTimeTest1() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::localDateTime
            );
            
            LocalDateTime input = LocalDateTime.of(2022, 12, 19, 12, 30, 0);
            String localDateTimeString = input.toString();

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                localDateTimeString
            );
            assertNotNull(result);
            
            Object localDateTime = result.value();
            assertTrue(localDateTime instanceof LocalDateTime);
            assertEquals(input, (LocalDateTime)localDateTime);
        }

        @Test
        @DisplayName("should convert value to LocalDate.")
        void dateTimeTest2() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::localDate
            );
            LocalDate input = LocalDate.of(2022, 12, 19);
            String localDateString = input.toString();

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                localDateString
            );
            
            assertNotNull(result);
            
            Object localDate = result.value();
            assertTrue(localDate instanceof LocalDate);
            assertEquals(input, (LocalDate)localDate);
        }

        @Test
        @DisplayName("should convert value to LocalTime.")
        void dateTimeTest3() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::localTime
            );
            
            LocalTime input = LocalTime.of(12, 30, 0);
            String localTimeString = input.toString();

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                localTimeString
            );
            assertNotNull(result);
            
            Object localDateTime = result.value();
            assertTrue(localDateTime instanceof LocalTime);
            assertEquals(input, (LocalTime)localDateTime);
        }

        @Test
        @DisplayName("should convert value to OffsetDateTime.")
        void dateTimeTest4() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::offsetDateTime
            );
            
            OffsetDateTime input = OffsetDateTime.of(
                LocalDate.of(2022, 12, 19), 
                LocalTime.of(12, 30), 
                ZoneOffset.ofHours(8)
            );
            String offsetDateTimeString = input.toString();

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                offsetDateTimeString
            );
            assertNotNull(result);
            
            Object offsetDateTime = result.value();
            assertTrue(offsetDateTime instanceof OffsetDateTime);
            assertEquals(input, (OffsetDateTime)offsetDateTime);
        }

        @Test
        @DisplayName("should convert value to OffsetTime.")
        void dateTimeTest5() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::offsetTime
            );
            
            OffsetTime input = OffsetTime.of( 
                LocalTime.of(12, 30), 
                ZoneOffset.ofHours(8)
            );
            String offsetTimeString = input.toString();

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                offsetTimeString
            );
            assertNotNull(result);
            
            Object offsetTime = result.value();
            assertTrue(offsetTime instanceof OffsetTime);
            assertEquals(input, (OffsetTime)offsetTime);
        }

        @Test
        @DisplayName("should convert value to ZonedDateTime.")
        void dateTimeTest7() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::zonedDateTime
            );
            
            ZonedDateTime input = ZonedDateTime.of(
                LocalDate.of(2022, 12, 19),
                LocalTime.of(12, 30), 
                ZoneId.of("Asia/Manila")
            );
            String zonedDateTimeString = input.toString();

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                zonedDateTimeString
            );
            assertNotNull(result);
            
            Object zonedDateTime = result.value();
            assertTrue(zonedDateTime instanceof ZonedDateTime);
            assertEquals(input, (ZonedDateTime)zonedDateTime);
        }

        @Test
        @DisplayName("should convert value to Instant.")
        void dateTimeTest8() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::instant
            );
            
            Instant input = Instant.now();
            String instantString = input.toString();

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                instantString
            );
            assertNotNull(result);
            
            Object instant = result.value();
            assertTrue(instant instanceof Instant);
            assertEquals(input, (Instant)instant);
        }

        @Test
        @DisplayName("should convert value to DayOfWeek.")
        void dateTimeTest9() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::dayOfWeek
            );
            
            DayOfWeek input = DayOfWeek.SUNDAY;
            String dayOfWeekString = input.name();

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                dayOfWeekString
            );
            assertNotNull(result);
            
            Object dayOfWeek = result.value();
            assertTrue(dayOfWeek instanceof DayOfWeek);
            assertEquals(input, (DayOfWeek)dayOfWeek);
        }

        @Test
        @DisplayName("should convert value to Month.")
        void dateTimeTest10() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::month
            );
            
            Month input = Month.AUGUST;
            String monthString = input.name();

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                monthString
            );
            assertNotNull(result);
            
            Object month = result.value();
            assertTrue(month instanceof Month);
            assertEquals(input, (Month)month);
        }

        @Test
        @DisplayName("should convert value to MonthDay.")
        void dateTimeTest11() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::monthDay
            );
            
            MonthDay input = MonthDay.of(Month.DECEMBER, 19);
            String monthDayString = input.toString();

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                monthDayString
            );
            assertNotNull(result);
            
            Object monthDay = result.value();
            assertTrue(monthDay instanceof MonthDay);
            assertEquals(input, (MonthDay)monthDay);
        }

        @Test
        @DisplayName("should convert value to Year.")
        void dateTimeTest12() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::year
            );
            
            Year input = Year.of(2022);
            String yearString = input.toString();

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                yearString
            );
            assertNotNull(result);
            
            Object year = result.value();
            assertTrue(year instanceof Year);
            assertEquals(input, (Year)year);
        }

        @Test
        @DisplayName("should convert value to YearMonth.")
        void dateTimeTest13() {
            DefaultConverter converter = converterToTest();

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::yearMonth
            );
            
            YearMonth input = YearMonth.of(2022, Month.AUGUST);
            String yearMonthString = input.toString();

            ConversionResult<?> result = converter.convert(
                proxyMethod,
                yearMonthString
            );
            assertNotNull(result);
            
            Object yearMonth = result.value();
            assertTrue(yearMonth instanceof YearMonth);
            assertEquals(input, (YearMonth)yearMonth);
        }
    }

    private DefaultConverter converterToTest() {
        ConverterProvider<DefaultConverter> provider = DefaultConverter.provider();

        ExternalizedProperties externalizedProperties = 
            ExternalizedProperties.builder()
                .withDefaultResolvers()
                .converters(provider)
                .build();
        RootConverter rootConverter = new RootConverter(
            externalizedProperties, 
            provider
        );
        
        return provider.get(externalizedProperties, rootConverter);
    }

    public static interface ProxyInterface extends PrimitiveConverterTests.ProxyInterface, 
            ArrayConverterTests.ProxyInterface,
            ListConverterTests.ProxyInterface, 
            SetConverterTests.ProxyInterface,
            OptionalConverterTests.ProxyInterface,
            EnumConverterTests.ProxyInterface, 
            DateTimeConverterTests.ProxyInterface {
        
        @ExternalizedProperty("property.custom.type")
        CustomType customType();
    }

    public static class CustomType {}
}
