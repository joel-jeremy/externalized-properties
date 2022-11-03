package io.github.joeljeremy.externalizedproperties.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties.ProfileConfigurator;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties.ProfilesBuilder;
import io.github.joeljeremy.externalizedproperties.core.processing.processors.DecryptProcessor;
import io.github.joeljeremy.externalizedproperties.core.processing.processors.DecryptProcessor.JceDecryptor;
import io.github.joeljeremy.externalizedproperties.core.resolvers.MapResolver;
import io.github.joeljeremy.externalizedproperties.core.testentities.EncryptionUtils;
import io.github.joeljeremy.externalizedproperties.core.testentities.Unsafe;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.StubConverter;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.StubConverter.ConverterResultKey;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.StubResolver;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
import javax.crypto.NoSuchPaddingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ExternalizedPropertiesTests {
  private static final String EXTERNALIZEDPROPERTIES_PROFILE_SYSTEM_PROPERTY =
      "externalizedproperties.profile";
  private static final String EXTERNALIZEDPROPERTIES_PROFILE_ENV_VAR =
      "EXTERNALIZEDPROPERTIES_PROFILE";

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
        assertThrows(IllegalArgumentException.class, () -> builder.resolvers((Resolver[]) null));
      }

      @Test
      @DisplayName("should throw when resolvers argument have null elements")
      void test2() {
        ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
        StubResolver resolver1 = new StubResolver();
        StubResolver resolver2 = new StubResolver();
        assertThrows(
            IllegalArgumentException.class, () -> builder.resolvers(resolver1, null, resolver2));
      }
    }

    @Nested
    class ConvertersMethod {
      @Test
      @DisplayName("should throw when converters argument is null")
      void test1() {
        ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.converters((Converter[]) null));
      }

      @Test
      @DisplayName("should throw when converters argument have null elements")
      void test2() {
        ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
        StubConverter<?> converter1 = new StubConverter<>();
        StubConverter<?> converter2 = new StubConverter<>();
        assertThrows(
            IllegalArgumentException.class, () -> builder.converters(converter1, null, converter2));
      }
    }

    @Nested
    class ProcessorsMethod {
      @Test
      @DisplayName("should throw when processors argument is null")
      void test1() {
        ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.processors((Processor[]) null));
      }

      @Test
      @DisplayName("should throw when processors argument have null elements")
      void test2() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
        DecryptProcessor processor1 =
            new DecryptProcessor(
                JceDecryptor.factory()
                    .symmetric(
                        EncryptionUtils.AES_ALGORITHM, EncryptionUtils.DEFAULT_AES_SECRET_KEY));
        DecryptProcessor processor2 =
            new DecryptProcessor(
                JceDecryptor.factory()
                    .asymmetric(
                        EncryptionUtils.RSA_ALGORITHM, EncryptionUtils.DEFAULT_RSA_PRIVATE_KEY));
        assertThrows(
            IllegalArgumentException.class, () -> builder.processors(processor1, null, processor2));
      }
    }

    @Nested
    class VariableExpanderMethod {
      @Test
      @DisplayName("should throw when variable expander argument is null")
      void test1() {
        ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.variableExpander(null));
      }
    }

    @Nested
    class BuildMethod {
      @ParameterizedTest
      @ValueSource(strings = {"test", "prod"})
      @DisplayName("should apply active profile configuration")
      void profilesTest1(String activeProfile) {
        MapResolver activeProfileResolver =
            new MapResolver(EXTERNALIZEDPROPERTIES_PROFILE_SYSTEM_PROPERTY, activeProfile);

        ExternalizedProperties externalizedProperties =
            ExternalizedProperties.builder()
                .resolvers(activeProfileResolver)
                .onProfiles("test")
                .apply(
                    (profile, builder) -> builder.resolvers(new MapResolver("property", profile)))
                .onProfiles("prod")
                .apply(
                    (profile, builder) -> builder.resolvers(new MapResolver("property", profile)))
                .build();

        ProxyInterface proxyInterface = externalizedProperties.initialize(ProxyInterface.class);

        assertEquals(activeProfile, proxyInterface.property());
      }

      @ParameterizedTest
      @ValueSource(strings = {"test", "prod"})
      @DisplayName("should apply wildcard profile configuration regardless of the active profile")
      void profilesTest2(String activeProfile) {
        MapResolver activeProfileResolver =
            new MapResolver(EXTERNALIZEDPROPERTIES_PROFILE_SYSTEM_PROPERTY, activeProfile);

        ExternalizedProperties externalizedProperties =
            ExternalizedProperties.builder()
                .resolvers(activeProfileResolver)
                // Wildcard profile.
                .onProfiles()
                .apply(
                    (profile, builder) ->
                        builder.resolvers(new MapResolver("property", "wildcard")))
                .build();

        ProxyInterface proxyInterface = externalizedProperties.initialize(ProxyInterface.class);

        assertEquals("wildcard", proxyInterface.property());
      }

      @Test
      @DisplayName("should not apply profile configurations when no active profile can be resolved")
      void profilesTest3() {
        // No resolver defines the active profile.

        ExternalizedProperties externalizedProperties =
            ExternalizedProperties.builder()
                // Wildcard profile.
                .onProfiles()
                .apply(
                    (profile, builder) ->
                        builder.resolvers(new MapResolver("property.optional", "wildcard")))
                .build();

        ProxyInterface proxyInterface = externalizedProperties.initialize(ProxyInterface.class);

        assertFalse(proxyInterface.optionalProperty().isPresent());
      }

      @Test
      @DisplayName("should not apply profile configurations when active profile is empty")
      void profilesTest4() {
        // Empty profile.
        MapResolver activeProfileResolver =
            new MapResolver(EXTERNALIZEDPROPERTIES_PROFILE_SYSTEM_PROPERTY, "");

        ExternalizedProperties externalizedProperties =
            ExternalizedProperties.builder()
                .resolvers(activeProfileResolver)
                // Wildcard profile.
                .onProfiles()
                .apply(
                    (profile, builder) ->
                        builder.resolvers(new MapResolver("property.optional", "wildcard")))
                .build();

        ProxyInterface proxyInterface = externalizedProperties.initialize(ProxyInterface.class);

        assertFalse(proxyInterface.optionalProperty().isPresent());
      }

      @Test
      @DisplayName("should not apply profile configurations when active profile is blank")
      void profilesTest5() {
        MapResolver activeProfileResolver =
            new MapResolver(
                EXTERNALIZEDPROPERTIES_PROFILE_SYSTEM_PROPERTY, "   " // Blank profile.
                );

        ExternalizedProperties externalizedProperties =
            ExternalizedProperties.builder()
                .resolvers(activeProfileResolver)
                // Wildcard profile.
                .onProfiles()
                .apply(
                    (profile, builder) ->
                        builder.resolvers(new MapResolver("property.optional", "wildcard")))
                .build();

        ProxyInterface proxyInterface = externalizedProperties.initialize(ProxyInterface.class);

        assertFalse(proxyInterface.optionalProperty().isPresent());
      }

      @ParameterizedTest
      @ValueSource(strings = "test")
      @DisplayName(
          "should try to look for the active profile in system properties "
              + "even if system property resolver is not explicitly registered.")
      // Environment variables too but it's not trivial to set environment variables
      void profilesTest6(String activeProfile) {
        System.setProperty(EXTERNALIZEDPROPERTIES_PROFILE_SYSTEM_PROPERTY, activeProfile);
        try {
          ExternalizedProperties externalizedProperties =
              ExternalizedProperties.builder()
                  // Wildcard profile.
                  .onProfiles("test")
                  .apply(
                      (profile, builder) -> builder.resolvers(new MapResolver("property", profile)))
                  .build();

          ProxyInterface proxyInterface = externalizedProperties.initialize(ProxyInterface.class);

          assertEquals(activeProfile, proxyInterface.property());
        } finally {
          System.clearProperty(EXTERNALIZEDPROPERTIES_PROFILE_SYSTEM_PROPERTY);
        }
      }

      @ParameterizedTest
      @ValueSource(strings = "test")
      @DisplayName(
          "should try to look for the active profile in environment variables "
              + "even if environment variable resolver is not explicitly registered.")
      void profilesTest7(String activeProfile) {
        Unsafe.setEnv(EXTERNALIZEDPROPERTIES_PROFILE_ENV_VAR, activeProfile);
        try {
          ExternalizedProperties externalizedProperties =
              ExternalizedProperties.builder()
                  // Wildcard profile.
                  .onProfiles("test")
                  .apply(
                      (profile, builder) -> builder.resolvers(new MapResolver("property", profile)))
                  .build();

          ProxyInterface proxyInterface = externalizedProperties.initialize(ProxyInterface.class);

          assertEquals(activeProfile, proxyInterface.property());
        } finally {
          Unsafe.clearEnv(EXTERNALIZEDPROPERTIES_PROFILE_ENV_VAR);
        }
      }

      @Test
      @DisplayName("should sort registered resolvers based on ordinal")
      void ordinalTest1() {
        StubResolver resolver1 = new StubResolver(pn -> "property".equals(pn) ? "ordinal-1" : null);
        StubResolver resolver2 = new StubResolver(pn -> "property".equals(pn) ? "ordinal-2" : null);
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

        OrdinalProxyInterface r1o1Proxy = r1o1.initialize(OrdinalProxyInterface.class);

        OrdinalProxyInterface r2o1Proxy = r2o1.initialize(OrdinalProxyInterface.class);

        // Should resolve using resolver1
        String r1o1Result = r1o1Proxy.resolve("property");

        assertEquals(resolver1.resolvedProperties().get("property"), r1o1Result);

        // Should resolve using resolver2
        String r2o1Result = r2o1Proxy.resolve("property");

        assertEquals(resolver2.resolvedProperties().get("property"), r2o1Result);

        // Should resolve using resolver3
        // r3no = resolver3 no ordinal
        String r3noResult = r1o1Proxy.resolve("other-property");
        String r3noResult2 = r2o1Proxy.resolve("other-property");

        assertEquals(resolver3.resolvedProperties().get("other-property"), r3noResult);

        assertEquals(resolver3.resolvedProperties().get("other-property"), r3noResult2);
      }

      @Test
      @DisplayName("should sort registered converters based on ordinal")
      void ordinalTest2() {
        StubConverter<Integer> converter1 =
            new StubConverter<>(
                (pm, value, targetType) ->
                    "1".equals(value)
                        ? ConversionResult.of(Integer.parseInt(value))
                        : ConversionResult.skip());
        StubConverter<Integer> converter2 =
            new StubConverter<>(
                (pm, value, targetType) ->
                    "1".equals(value)
                        ? ConversionResult.of(Integer.parseInt(value))
                        : ConversionResult.skip());
        StubConverter<?> converter3 =
            new StubConverter<>(
                (pm, value, targetType) -> ConversionResult.of(Integer.parseInt(value)));

        // c1o1 = converter1 as ordinal 1
        ExternalizedProperties c1o1 =
            ExternalizedProperties.builder()
                .converters(converter3)
                .converters(Ordinals.ordinalConverter(1, converter1))
                .converters(Ordinals.ordinalConverter(2, converter2))
                .build();

        // c2o1 = converter2 as ordinal 1
        ExternalizedProperties c2o1 =
            ExternalizedProperties.builder()
                .converters(converter3)
                // Switched up ordinals.
                .converters(Ordinals.ordinalConverter(2, converter1))
                .converters(Ordinals.ordinalConverter(1, converter2))
                .build();

        OrdinalProxyInterface c1o1Proxy = c1o1.initialize(OrdinalProxyInterface.class);

        OrdinalProxyInterface c2o1Proxy = c2o1.initialize(OrdinalProxyInterface.class);

        // Should convert using converter1
        int c1o1Result = c1o1Proxy.convert("1", int.class);

        assertEquals(
            converter1.conversionResults().get(new ConverterResultKey("1", int.class)), c1o1Result);

        // Should convert using converter2
        int resultMustBeFromConverter2 = c2o1Proxy.convert("1", int.class);

        assertEquals(
            converter2.conversionResults().get(new ConverterResultKey("1", int.class)),
            resultMustBeFromConverter2);

        // Should convert using converter3
        // c3no = converter3 no ordinal
        int c3noResult = c1o1Proxy.convert("999", int.class);
        int c3noResult2 = c2o1Proxy.convert("999", int.class);

        assertEquals(
            converter3.conversionResults().get(new ConverterResultKey("999", int.class)),
            c3noResult);

        assertEquals(
            converter3.conversionResults().get(new ConverterResultKey("999", int.class)),
            c3noResult2);
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
        ExternalizedProperties ep = ExternalizedProperties.builder().defaults().build();

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
        ExternalizedProperties ep =
            ExternalizedProperties.builder().enableDefaultResolvers().build();

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
        ExternalizedProperties ep =
            ExternalizedProperties.builder()
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
        assertThrows(IllegalArgumentException.class, () -> builder.cacheDuration(null));
      }

      @Test
      @DisplayName("should throw when cache duration argument is null")
      void test2() {
        ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
        assertDoesNotThrow(() -> builder.cacheDuration(Duration.ofMinutes(1)));
      }
    }

    @Nested
    class EnableEagerLoadingMethod {
      @Test
      @DisplayName("should enable proxy eager loading")
      void test1() {
        Map<String, String> testProps = testProperties();
        StubResolver mapResolver = new StubResolver(testProps::get);

        ExternalizedProperties externalizedProperties =
            ExternalizedProperties.builder()
                .resolvers(mapResolver)
                .enableDefaultConverters()
                .enableEagerLoading()
                .build();

        EagerLoadingProxyInterface proxy =
            externalizedProperties.initialize(EagerLoadingProxyInterface.class);

        assertNotNull(proxy);
        mapResolver
            .resolvedProperties()
            .forEach(
                (key, expectedValue) -> {
                  assertEquals(testProps.get(key), expectedValue);
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

        ProxyInterface proxy = externalizedProperties.initialize(ProxyInterface.class);

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
        ExternalizedProperties externalizedProperties =
            ExternalizedProperties.builder()
                .enableInitializeCaching()
                .enableDefaultResolvers()
                .build();

        ProxyInterface proxy1 = externalizedProperties.initialize(ProxyInterface.class);
        ProxyInterface proxy2 = externalizedProperties.initialize(ProxyInterface.class);

        // Same instance.
        assertSame(proxy1, proxy2);
      }
    }

    @Nested
    class EnableVariableExpansionInPropertiesMethod {
      @Test
      @DisplayName("should expand variables in resolved properties")
      void test1() {
        Map<String, String> testProps = testProperties();

        ExternalizedProperties externalizedProperties =
            ExternalizedProperties.builder()
                .enableVariableExpansionInProperties()
                .resolvers(new MapResolver(testProps))
                .build();

        ProxyInterface proxy = externalizedProperties.initialize(ProxyInterface.class);

        String resolved = proxy.propertyWithVariableValue();

        assertEquals(proxy.propertyVariable(), resolved);
      }
    }

    @Nested
    class OnProfilesMethod {
      @Test
      @DisplayName("should throw when profiles varargs argument is null")
      void test1() {
        ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
        assertThrows(IllegalArgumentException.class, () -> builder.onProfiles((String[]) null));
      }

      @Test
      @DisplayName("should throw when profiles varargs argument contains null elements")
      void test2() {
        ExternalizedProperties.Builder builder = ExternalizedProperties.builder();
        assertThrows(
            IllegalArgumentException.class, () -> builder.onProfiles("test", "staging", null));
      }
    }
  }

  @Nested
  class ProfilesBuilderTests {
    @Nested
    class ApplyMethod {
      @Test
      @DisplayName("should throw when profile configurators argument is null")
      void test1() {
        ProfilesBuilder profilesBuilder = ExternalizedProperties.builder().onProfiles();
        assertThrows(
            IllegalArgumentException.class,
            () -> profilesBuilder.apply((ProfileConfigurator[]) null));
      }

      @Test
      @DisplayName("should throw when profile configurators argument contains null elements")
      void test2() {
        ProfilesBuilder profilesBuilder = ExternalizedProperties.builder().onProfiles();
        assertThrows(
            IllegalArgumentException.class,
            () ->
                profilesBuilder.apply(
                    (activeProfile, builder) -> {}, null // Not allowed
                    ));
      }

      @Test
      @DisplayName("should throw when profile configurators argument is empty")
      void test3() {
        ProfilesBuilder profilesBuilder = ExternalizedProperties.builder().onProfiles();
        assertThrows(IllegalArgumentException.class, () -> profilesBuilder.apply());
      }
    }
  }

  private static void testDefaultResolvers(ExternalizedProperties ep) {
    ProxyInterface proxyInterface = ep.initialize(ProxyInterface.class);

    // Resolved from system properties.
    assertEquals(System.getProperty("java.version"), proxyInterface.javaVersion());

    // Resolved from environment variables.
    assertEquals(System.getenv("PATH"), proxyInterface.path());
  }

  private static void testDefaultConverters(
      ExternalizedProperties externalizedProperties, Map<String, String> expectedProps) {
    ProxyInterface proxy = externalizedProperties.initialize(ProxyInterface.class);

    // Primitive conversions
    assertEquals(Integer.parseInt(expectedProps.get("property.int")), proxy.intProperty());
    assertEquals(Long.parseLong(expectedProps.get("property.long")), proxy.longProperty());
    assertEquals(Short.parseShort(expectedProps.get("property.short")), proxy.shortProperty());
    assertEquals(Double.parseDouble(expectedProps.get("property.double")), proxy.doubleProperty());
    assertEquals(Float.parseFloat(expectedProps.get("property.float")), proxy.floatProperty());
    assertEquals(
        Boolean.parseBoolean(expectedProps.get("property.boolean")), proxy.booleanProperty());
    assertEquals(Byte.parseByte(expectedProps.get("property.byte")), proxy.byteProperty());

    // List/Collection conversion
    assertIterableEquals(
        Arrays.asList(expectedProps.get("property.list").split(",")), proxy.listProperty());
    assertIterableEquals(
        Arrays.asList(expectedProps.get("property.collection").split(",")),
        proxy.collectionProperty());

    // Set conversion
    assertIterableEquals(
        new LinkedHashSet<>(Arrays.asList(expectedProps.get("property.set").split(","))),
        proxy.setProperty());

    // Array conversion
    assertArrayEquals(expectedProps.get("property.array").split(","), proxy.arrayProperty());

    // Optional conversion
    Optional<String> prop = proxy.optionalProperty();
    assertTrue(prop.isPresent());
    assertEquals(expectedProps.get("property.optional"), prop.get());

    // Enum conversion
    assertEquals(TestEnum.valueOf(expectedProps.get("property.enum")), proxy.enumProperty());

    // Date/Time conversions
    assertEquals(
        LocalDateTime.parse(expectedProps.get("property.localdatetime")), proxy.localDateTime());

    assertEquals(LocalDate.parse(expectedProps.get("property.localdate")), proxy.localDate());

    assertEquals(LocalTime.parse(expectedProps.get("property.localtime")), proxy.localTime());

    assertEquals(
        OffsetDateTime.parse(expectedProps.get("property.offsetdatetime")), proxy.offsetDateTime());

    assertEquals(OffsetTime.parse(expectedProps.get("property.offsettime")), proxy.offsetTime());

    assertEquals(
        ZonedDateTime.parse(expectedProps.get("property.zoneddatetime")), proxy.zonedDateTime());

    assertEquals(Instant.parse(expectedProps.get("property.instant")), proxy.instant());

    assertEquals(DayOfWeek.valueOf(expectedProps.get("property.dayofweek")), proxy.dayOfWeek());

    assertEquals(Month.valueOf(expectedProps.get("property.month")), proxy.month());

    assertEquals(MonthDay.parse(expectedProps.get("property.monthday")), proxy.monthDay());

    assertEquals(Year.parse(expectedProps.get("property.year")), proxy.year());

    assertEquals(YearMonth.parse(expectedProps.get("property.yearmonth")), proxy.yearMonth());
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
    props.put("property.with.variable.value", "${property.variable}");
    props.put("property.variable", "property-variable-value");
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

    @ExternalizedProperty("property.with.variable.value")
    String propertyWithVariableValue();

    @ExternalizedProperty("property.variable")
    String propertyVariable();
  }

  public static interface EagerLoadingProxyInterface {
    @ExternalizedProperty("property")
    String property();

    @ExternalizedProperty("property.int")
    int intProperty();

    @ExternalizedProperty("property.optional")
    Optional<String> optionalProperty();
  }

  public static interface OrdinalProxyInterface {
    @ResolverFacade
    String resolve(String propertyName);

    @ConverterFacade
    <T> T convert(String property, Class<T> targetType);
  }

  public static enum TestEnum {
    ONE,
    TWO,
    THREE
  }
}
