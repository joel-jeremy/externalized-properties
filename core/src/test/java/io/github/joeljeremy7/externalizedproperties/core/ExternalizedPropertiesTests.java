package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.MapResolver;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubConverter;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubConverter.ConverterResultKey;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
import java.util.Collections;
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
import static org.junit.jupiter.api.Assertions.assertSame;
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
            @DisplayName("should throw when resolvers argument is null")
            void test1() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.resolvers((Resolver[])null)
                );
            }

            @Test
            @DisplayName("should throw when resolvers argument have null elements")
            void test2() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.resolvers(
                        new StubResolver(),
                        null,
                        new StubResolver()
                    )
                );
            }
        }

        @Nested
        class ConvertersMethod {
            @Test
            @DisplayName("should throw when converters argument is null")
            void test1() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.converters((Converter[])null)
                );
            }

            @Test
            @DisplayName("should throw when converters argument have null elements")
            void test2() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.converters(
                        new StubConverter<>(),
                        null,
                        new StubConverter<>()
                    )
                );
            }
        }

        @Nested
        class ProcessorsMethod {
            @Test
            @DisplayName("should throw when processors argument is null")
            void test1() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.processors((Processor[])null)
                );
            }

            @Test
            @DisplayName("should throw when processors argument have null elements")
            void test2() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class,
                    () -> builder.processors(
                        new DecryptProcessor(),
                        null,
                        new DecryptProcessor()
                    )
                );
            }
        }

        @Nested
        class VariableExpanderMethod {
            @Test
            @DisplayName("should throw when variable expander argument is null")
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

            @Test
            @DisplayName("should sort registered resolvers based on ordinal")
            void test2() {
                StubResolver resolver1 = new StubResolver(
                    pn -> "property".equals(pn) ? "ordinal-1" : null
                );
                StubResolver resolver2 = new StubResolver(
                    pn -> "property".equals(pn) ? "ordinal-2" : null
                );
                StubResolver resolver3 = new StubResolver(pn -> "no-ordinal");

                // r1o1 = resolver1 as ordinal 1
                ExternalizedProperties r1o1 = 
                    ExternalizedProperties.builder()
                        .resolvers(resolver3)
                        .resolvers(Ordinals.ordinalResolver(2, resolver2))
                        .resolvers(Ordinals.ordinalResolver(1, resolver1))
                        .build();

                // r2o1 = resolver2 as ordinal 1
                ExternalizedProperties r2o1 = 
                    ExternalizedProperties.builder()
                        .resolvers(resolver3)
                        // Switched up ordinals.
                        .resolvers(Ordinals.ordinalResolver(1, resolver2))
                        .resolvers(Ordinals.ordinalResolver(2, resolver1))
                        .build();
                
                OrdinalProxyInterface r1o1Proxy =
                    r1o1.initialize(OrdinalProxyInterface.class);
                
                OrdinalProxyInterface r2o1Proxy =
                    r2o1.initialize(OrdinalProxyInterface.class);

                // Should resolve using resolver1
                String r1o1Result = r1o1Proxy.resolve("property");

                assertEquals(
                    resolver1.resolvedProperties().get("property"), 
                    r1o1Result
                );

                // Should resolve using resolver2
                String r2o1Result = r2o1Proxy.resolve("property");

                assertEquals(
                    resolver2.resolvedProperties().get("property"), 
                    r2o1Result
                );

                // Should resolve using resolver3
                // r3no = resolver3 no ordinal
                String r3noResult = r1o1Proxy.resolve("other-property");
                String r3noResult2 = r2o1Proxy.resolve("other-property");

                assertEquals(
                    resolver3.resolvedProperties().get("other-property"), 
                    r3noResult
                );

                assertEquals(
                    resolver3.resolvedProperties().get("other-property"), 
                    r3noResult2
                );
            }

            @Test
            @DisplayName("should sort registered converters based on ordinal")
            void test3() {
                StubConverter<Integer> converter1 = new StubConverter<>(
                    (pm, value, targetType) -> "1".equals(value) ? 
                        ConversionResult.of(Integer.parseInt(value)) : ConversionResult.skip()
                );
                StubConverter<Integer> converter2 = new StubConverter<>(
                    (pm, value, targetType) -> "1".equals(value) ? 
                    ConversionResult.of(Integer.parseInt(value)) : ConversionResult.skip()
                );
                StubConverter<?> converter3 = new StubConverter<>(
                    (pm, value, targetType) -> ConversionResult.of(Integer.parseInt(value))
                );

                // c1o1 = converter1 as ordinal 1
                ExternalizedProperties c1o1 = 
                    ExternalizedProperties.builder()
                        .enableDefaultResolvers()
                        .converters(converter3)
                        .converters(Ordinals.ordinalConverter(1, converter1))
                        .converters(Ordinals.ordinalConverter(2, converter2))
                        .build();

                // c2o1 = converter2 as ordinal 1
                ExternalizedProperties c2o1 = 
                    ExternalizedProperties.builder()
                        .enableDefaultResolvers()
                        .converters(converter3)
                        // Switched up ordinals.
                        .converters(Ordinals.ordinalConverter(2, converter1))
                        .converters(Ordinals.ordinalConverter(1, converter2))
                        .build();

                OrdinalProxyInterface c1o1Proxy =
                    c1o1.initialize(OrdinalProxyInterface.class);
                
                OrdinalProxyInterface c2o1Proxy =
                    c2o1.initialize(OrdinalProxyInterface.class);
                
                // Should convert using converter1
                int c1o1Result = c1o1Proxy.convert("1", int.class);
                
                assertEquals(
                    converter1.conversionResults().get(
                        new ConverterResultKey("1", int.class)
                    ), 
                    c1o1Result
                );

                // Should convert using converter2
                int resultMustBeFromConverter2 = 
                    c2o1Proxy.convert("1", int.class);
                
                assertEquals(
                    converter2.conversionResults().get(
                        new ConverterResultKey("1", int.class)
                    ),  
                    resultMustBeFromConverter2
                );

                // Should convert using converter3
                // c3no = converter3 no ordinal
                int c3noResult = c1o1Proxy.convert("999", int.class);
                int c3noResult2 = c2o1Proxy.convert("999", int.class);

                assertEquals(
                    converter3.conversionResults().get(
                        new ConverterResultKey("999", int.class)
                    ), 
                    c3noResult
                );

                assertEquals(
                    converter3.conversionResults().get(
                        new ConverterResultKey("999", int.class)
                    ), 
                    c3noResult2
                );
            }
        }

        @Nested
        class DefaultsMethod {
            @Test
            @DisplayName("should register default resolvers and converters")
            void test1() {
                Map<String, String> testProps = testProperties();
                testProps.forEach((k, v) -> System.setProperty(k, v));

                // Default converter includes conversion to:
                // - Primitives
                // - Lists/Collections
                // - Arrays
                // - Sets
                // - Enums
                // - Date/Time classes
                ExternalizedProperties ep = ExternalizedProperties.builder()
                    .defaults()
                    .build();
        
                testDefaultResolvers(ep);
                testDefaultConverters(ep, testProps);

                // Cleanup system properties.
                testProps.keySet().forEach(k -> System.clearProperty(k));
            }
        }

        @Nested
        class EnableDefaultResolversMethod {
            @Test
            @DisplayName("should register default resolvers")
            void test1() {
                // Default resolvers include:
                // - System property resolver
                // - Environment variable resolver
                ExternalizedProperties ep = ExternalizedProperties.builder()
                    .enableDefaultResolvers()
                    .build();

                testDefaultResolvers(ep);
            }
        }

        @Nested
        class EnableDefaultConvertersMethod {
            @Test
            @DisplayName("should register default converters")
            void test1() {
                Map<String, String> testProps = testProperties();
        
                // Default converter includes conversion to:
                // - Primitives
                // - Lists/Collections
                // - Arrays
                // - Sets
                // - Enums
                // - Date/Time classes
                ExternalizedProperties ep = ExternalizedProperties.builder()
                    .resolvers(new MapResolver(testProps))
                    .enableDefaultConverters()
                    .build();
        
                testDefaultConverters(ep, testProps);
            }
        }

        @Nested
        class CacheDurationMethod {
            @Test
            @DisplayName("should throw when cache duration argument is null")
            void test1() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> builder.cacheDuration(null)
                );
            }

            @Test
            @DisplayName("should throw when cache duration argument is null")
            void test2() {
                ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
                assertDoesNotThrow(
                    () -> builder.cacheDuration(Duration.ofMinutes(1))
                );
            }
        }

        @Nested
        class EnableEagerLoadingMethod {
            @Test
            @DisplayName("should enable proxy eager loading")
            void test1() {
                Map<String, String> testProps = testProperties();
                StubResolver mapResolver = new StubResolver(testProps::get);
                StubResolver systemProps = new StubResolver(System::getProperty);

                ExternalizedProperties externalizedProperties = 
                    ExternalizedProperties.builder()
                        .enableDefaultResolvers()
                        .resolvers(mapResolver, systemProps)
                        .enableDefaultConverters()
                        .enableEagerLoading()
                        .build();

                ProxyInterface proxy = 
                    externalizedProperties.initialize(ProxyInterface.class);

                assertNotNull(proxy);
                testProps.forEach((key, expectedValue) -> {
                    assertEquals(expectedValue, mapResolver.resolvedProperties().get(key));
                });
                systemProps.resolvedProperties().forEach((key, value) -> {
                    assertEquals(System.getProperty(key), value);
                });
            }
        }

        @Nested
        class EnableInvocationCachingMethod {
            @Test
            @DisplayName("should cache proxy invocation results")
            void test1() {
                Map<String, String> testProps = testProperties();

                ExternalizedProperties externalizedProperties = 
                    ExternalizedProperties.builder()
                        .enableInvocationCaching()
                        .enableDefaultConverters()
                        .resolvers(new MapResolver(testProps))
                        .build();

                ProxyInterface proxy = 
                    externalizedProperties.initialize(ProxyInterface.class);
                
                List<String> result1 = proxy.listProperty();
                List<String> result2 = proxy.listProperty();

                // Same instance.
                assertSame(result1, result2);
            }
        }

        @Nested
        class EnableInitializeCachingMethod {
            @Test
            @DisplayName("should cache initialized proxies")
            void test1() {
                Map<String, String> testProps = testProperties();

                ExternalizedProperties externalizedProperties = 
                    ExternalizedProperties.builder()
                        .enableInitializeCaching()
                        .resolvers(new MapResolver(testProps))
                        .build();

                ProxyInterface proxy1 = 
                    externalizedProperties.initialize(ProxyInterface.class);
                ProxyInterface proxy2 = 
                    externalizedProperties.initialize(ProxyInterface.class);

                // Same instance.
                assertSame(proxy1, proxy2);
            }
        }

        @Nested
        class OnProfilesMethod {
            @Test
            @DisplayName("should throw when profiles varargs argument is null")
            void test1() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> ExternalizedProperties.builder().onProfiles((String[])null)
                );
            }
        }
    }

    @Nested
    class ProfileConfigurationTests {
        @Nested
        class ApplyMethod {
            @Test
            @DisplayName("should throw when profile configurator argument is null")
            void test1() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> ExternalizedProperties.builder()
                        .onProfiles().apply(null)
                );
            }

            @ParameterizedTest
            @ValueSource(strings = { "test", "prod" })
            @DisplayName("should apply active profile configuration")
            void test2(String activeProfile) {
                // Set profile.
                System.setProperty("externalizedproperties.profile", activeProfile);

                ExternalizedProperties externalizedProperties = 
                    ExternalizedProperties.builder()
                        .onProfiles("test").apply((profile, builder) -> 
                            builder.resolvers(new MapResolver(
                                Collections.singletonMap("property", profile)
                            ))
                        )
                        .onProfiles("prod").apply((profile, builder) ->
                            builder.resolvers(new MapResolver(
                                Collections.singletonMap("property", profile)
                            ))
                        )
                        .build();
                
                ProxyInterface proxyInterface = 
                    externalizedProperties.initialize(ProxyInterface.class);

                assertEquals(activeProfile, proxyInterface.property());

                // Cleanup profile system property.
                System.clearProperty("externalizedproperties.profile");
            }

            @ParameterizedTest
            @ValueSource(strings = { "test", "prod" })
            @DisplayName(
                "should apply wildcard profile configuration regardless of the active profile"
            )
            void test3(String activeProfile) {
                // Set profile.
                System.setProperty("externalizedproperties.profile", activeProfile);

                ExternalizedProperties externalizedProperties = 
                    ExternalizedProperties.builder()
                        // Wildcard profile.
                        .onProfiles().apply((profile, builder) ->
                            builder.resolvers(new MapResolver(
                                Collections.singletonMap("property", "wildcard")
                            ))
                        )
                        .build();
                
                ProxyInterface proxyInterface = 
                    externalizedProperties.initialize(ProxyInterface.class);

                assertEquals("wildcard", proxyInterface.property());

                // Cleanup profile system property.
                System.clearProperty("externalizedproperties.profile");
            }
        }
    }

    private static void testDefaultResolvers(ExternalizedProperties ep) {
        ProxyInterface proxyInterface = ep.initialize(ProxyInterface.class);

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

    private static void testDefaultConverters(
            ExternalizedProperties externalizedProperties,
            Map<String, String> expectedProps
    ) {
        ProxyInterface proxy = 
            externalizedProperties.initialize(ProxyInterface.class);

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

    // This is Map<Object, Object>, but only Strings should be put here...
    private static Map<String, String> testProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("property", "property-value");
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

        @ExternalizedProperty("property")
        String property();

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

    public static interface OrdinalProxyInterface {
        @ExternalizedProperty
        String resolve(String propertyName);

        @Convert
        <T> T convert(String property, Class<T> targetType);
    }

    public static enum TestEnum {
        ONE,
        TWO,
        THREE
    }
}
