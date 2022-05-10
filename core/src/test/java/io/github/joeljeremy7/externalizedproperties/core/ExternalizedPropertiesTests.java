package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.resolvers.MapResolver;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
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
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExternalizedPropertiesTests {
    @Nested
    class BuilderMethod {
        @Test
        @DisplayName("should not return null")
        void test1() {
            assertNotNull(ExternalizedProperties.builder());
        }
    }

    @Nested
    class BuilderTests {
        @Nested
        class ResolversMethod {
            @Test
            @DisplayName("should throw when resolver providers collection argument is null")
            void test1() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.resolvers((Collection<ResolverProvider<?>>)null)
                );
            }

            @Test
            @DisplayName("should throw when resolver providers varargs argument is null")
            void test2() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.resolvers((ResolverProvider[])null)
                );
            }
        }

        @Nested
        class ConvertersMethod {
            @Test
            @DisplayName("should throw when converter providers collection argument is null")
            void test1() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.converters((Collection<ConverterProvider<?>>)null)
                );
            }
        
            @Test
            @DisplayName("should throw when converter providers varargs argument is null")
            void test2() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.converters((ConverterProvider[])null)
                );
            }
        }

        @Nested
        class ProcessorsMethod {
            @Test
            @DisplayName("should throw when processor providers collection argument is null")
            void test1() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.processors((Collection<ProcessorProvider<?>>)null)
                );
            }
        
            @Test
            @DisplayName("should throw when processor providers varargs argument is null")
            void test2() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.processors((ProcessorProvider[])null)
                );
            }
        }

        @Nested
        class VariableExpanderMethod {
            @Test
            @DisplayName("should throw when variable expander factory argument is null")
            void test1() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.variableExpander(null)
                );
            }
        }

        @Nested
        class BuildMethod {
            @Test
            @DisplayName("should throw on build when there are no resolvers")
            void test1() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalStateException.class,
                    () -> builder.build()
                );
            }
        }

        @Nested
        class WithDefaultResolversMethod {
            @Test
            @DisplayName("should register default resolvers")
            void test1() {
                // Default resolvers include:
                // - System property resolver
                // - Environment variable resolver
                ExternalizedProperties ep = ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .build();

                testDefaultResolvers(ep);
            }
        }

        @Nested
        class WithDefaultConvertersMethod {
            @Test
            @DisplayName("should register default converters")
            void test1() {
                Map<String, String> testProps = testProperties();
        
                // Default converter includes conversion to:
                // - Primitives
                // - Lists/Collections
                // - Sets
                // - Arrays
                // - Optionals
                // - Enums
                // - Date/Time classes
                ExternalizedProperties ep = ExternalizedProperties.builder()
                    .resolvers(MapResolver.provider(testProps))
                    .withDefaultConverters()
                    .build();
        
                testDefaultConverters(ep, testProps);
            }
        }

        @Nested
        class WithDefaultsMethod {
            @Test
            @DisplayName("should register default resolvers and converters")
            void test12() {
                // System properties.
                Map<String, String> testProps = testProperties();
                testProps.forEach((k, v) -> System.setProperty(k, v));
        
                // Default converter includes conversion to:
                // - Primitives
                // - Lists/Collections
                // - Arrays
                // - Optionals
                ExternalizedProperties ep = ExternalizedProperties.builder()
                    .withDefaults()
                    .build();
        
                testDefaultResolvers(ep);
                testDefaultConverters(ep, testProps);
            }
        }

        @Nested
        class WithCacheDurationMethod {
            @Test
            @DisplayName("should throw when cache duration argument is null")
            void test1() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.withCacheDuration(null)
                );
            }

            @Test
            @DisplayName("should throw when cache duration argument is null")
            void test2() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertDoesNotThrow(
                    () -> builder.withCacheDuration(Duration.ofMinutes(1))
                );
            }
        }

        @Nested
        class WithProxyEagerLoadingMethod {
            @Test
            @DisplayName("should enable proxy eager loading")
            void test1() {
                Map<String, String> testProps = testProperties();
                StubResolver mapResolver = new StubResolver(testProps::get);
                StubResolver systemProps = new StubResolver(System::getProperty);

                ExternalizedProperties externalizedProperties = 
                    ExternalizedProperties.builder()
                        .withDefaultResolvers()
                        .resolvers(
                            ResolverProvider.of(mapResolver),
                            ResolverProvider.of(systemProps)
                        )
                        .withDefaultConverters()
                        .withProxyEagerLoading()
                        .build();

                ProxyInterface proxy = 
                    externalizedProperties.proxy(ProxyInterface.class);

                assertNotNull(proxy);
                testProps.forEach((key, expectedValue) -> {
                    assertEquals(expectedValue, mapResolver.resolvedProperties().get(key));
                });
                systemProps.resolvedProperties().forEach((key, value) -> {
                    assertEquals(System.getProperty(key), value);
                });
            }
        }

        private void testDefaultResolvers(ExternalizedProperties ep) {
            ProxyInterface proxyInterface = ep.proxy(ProxyInterface.class);

            // Resolved from system properties.
            assertEquals(
                System.getProperty("java.version"), 
                proxyInterface.javaVersion()
            );

            // Resolved from environment variables.
            assertEquals(
                System.getenv("PATH"), 
                proxyInterface.path()
            );
        }

        private void testDefaultConverters(
                ExternalizedProperties externalizedProperties,
                Map<String, String> expectedProps
        ) {
            ProxyInterface proxy = 
                externalizedProperties.proxy(ProxyInterface.class);

            // Primitive conversions
            assertEquals(
                Integer.parseInt(expectedProps.get("property.int")), 
                proxy.intProperty()
            );
            assertEquals(
                Long.parseLong(expectedProps.get("property.long")), 
                proxy.longProperty()
            );
            assertEquals(
                Short.parseShort(expectedProps.get("property.short")), 
                proxy.shortProperty()
            );
            assertEquals(
                Double.parseDouble(expectedProps.get("property.double")), 
                proxy.doubleProperty()
            );
            assertEquals(
                Float.parseFloat(expectedProps.get("property.float")), 
                proxy.floatProperty()
            );
            assertEquals(
                Boolean.parseBoolean(expectedProps.get("property.boolean")), 
                proxy.booleanProperty()
            );
            assertEquals(
                Byte.parseByte(expectedProps.get("property.byte")), 
                proxy.byteProperty()
            );
            
            // List/Collection conversion
            assertIterableEquals(
                Arrays.asList(expectedProps.get("property.list").split(",")), 
                proxy.listProperty()
            );
            assertIterableEquals(
                Arrays.asList(expectedProps.get("property.collection").split(",")), 
                proxy.collectionProperty()
            );

            // Set conversion
            assertIterableEquals(
                new LinkedHashSet<>(
                    Arrays.asList(expectedProps.get("property.set").split(","))
                ), 
                proxy.setProperty()
            );

            // Array conversion
            assertArrayEquals(
                expectedProps.get("property.array").split(","), 
                proxy.arrayProperty()
            );

            // Optional conversion
            Optional<String> prop = proxy.optionalProperty();
            assertTrue(prop.isPresent());
            assertEquals(expectedProps.get("property.optional"), prop.get());

            // Enum conversion
            assertEquals(
                TestEnum.valueOf(expectedProps.get("property.enum")), 
                proxy.enumProperty()
            );

            // Date/Time conversions
            assertEquals(
                LocalDateTime.parse(expectedProps.get("property.localdatetime")), 
                proxy.localDateTime()
            );

            assertEquals(
                LocalDate.parse(expectedProps.get("property.localdate")), 
                proxy.localDate()
            );
            
            assertEquals(
                LocalTime.parse(expectedProps.get("property.localtime")), 
                proxy.localTime()
            );

            assertEquals(
                OffsetDateTime.parse(expectedProps.get("property.offsetdatetime")), 
                proxy.offsetDateTime()
            );

            assertEquals(
                OffsetTime.parse(expectedProps.get("property.offsettime")), 
                proxy.offsetTime()
            );

            assertEquals(
                ZonedDateTime.parse(expectedProps.get("property.zoneddatetime")), 
                proxy.zonedDateTime()
            );

            assertEquals(
                Instant.parse(expectedProps.get("property.instant")), 
                proxy.instant()
            );

            assertEquals(
                DayOfWeek.valueOf(expectedProps.get("property.dayofweek")), 
                proxy.dayOfWeek()
            );

            assertEquals(
                Month.valueOf(expectedProps.get("property.month")), 
                proxy.month()
            );

            assertEquals(
                MonthDay.parse(expectedProps.get("property.monthday")), 
                proxy.monthDay()
            );

            assertEquals(
                Year.parse(expectedProps.get("property.year")), 
                proxy.year()
            );

            assertEquals(
                YearMonth.parse(expectedProps.get("property.yearmonth")), 
                proxy.yearMonth()
            );
        }
    }

    // This is Map<Object, Object>, but only Strings should be put here...
    private static Map<String, String> testProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("property.int", "1");
        props.put("property.short", "2");
        props.put("property.long", "3");
        props.put("property.double", "4.0");
        props.put("property.float", "5.0");
        props.put("property.boolean", "true");
        props.put("property.byte", "0");
        props.put("property.list", "a,b,c");
        props.put("property.set", "a,b,c");
        props.put("property.collection", "c,b,a");
        props.put("property.array", "a,b,c");
        props.put("property.optional", "optional-value");
        props.put("property.enum", TestEnum.ONE.name());
        props.put("property.localdatetime", LocalDateTime.now().toString());
        props.put("property.localdate", LocalDate.now().toString());
        props.put("property.localtime", LocalTime.now().toString());
        props.put("property.offsetdatetime", OffsetDateTime.now().toString());
        props.put("property.offsettime", OffsetTime.now().toString());
        props.put("property.zoneddatetime", ZonedDateTime.now().toString());
        props.put("property.instant", Instant.now().toString());
        props.put("property.dayofweek", DayOfWeek.FRIDAY.name());
        props.put("property.month", Month.AUGUST.name());
        props.put("property.monthday", MonthDay.now().toString());
        props.put("property.year", Year.now().toString());
        props.put("property.yearmonth", YearMonth.now().toString());
        return props;
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("java.version")
        String javaVersion();

        @ExternalizedProperty("path")
        String path();

        @ExternalizedProperty("property.int")
        int intProperty();
    
        @ExternalizedProperty("property.long")
        long longProperty();
    
        @ExternalizedProperty("property.float")
        float floatProperty();
    
        @ExternalizedProperty("property.double")
        double doubleProperty();
    
        @ExternalizedProperty("property.boolean")
        boolean booleanProperty();
    
        @ExternalizedProperty("property.short")
        short shortProperty();
    
        @ExternalizedProperty("property.byte")
        byte byteProperty();

        @ExternalizedProperty("property.array")
        String[] arrayProperty();

        @ExternalizedProperty("property.list")
        List<String> listProperty();

        @ExternalizedProperty("property.collection")
        Collection<String> collectionProperty();

        @ExternalizedProperty("property.set")
        Set<String> setProperty();

        @ExternalizedProperty("property.optional")
        Optional<String> optionalProperty();

        @ExternalizedProperty("property.enum")
        TestEnum enumProperty();

        @ExternalizedProperty("property.localdatetime")
        LocalDateTime localDateTime();
    
        @ExternalizedProperty("property.localdate")
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
    
        @ExternalizedProperty("property.yearmonth")
        YearMonth yearMonth();
    }

    public static enum TestEnum {
        ONE,
        TWO,
        THREE
    }
}
