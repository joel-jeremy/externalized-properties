package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.SetProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SetConverterTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when set factory argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> new SetConverter(null)
            );
        }
    }

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        public void test1() {
            SetConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Set class.")
        public void test2() {
            SetConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Set.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not a Set class.")
        public void test4() {
            SetConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            SetConverter converter = converterToTest();
            assertThrows(IllegalArgumentException.class, () -> converter.convert(null));
        }

        @Test
        @DisplayName("should convert value to a Set.")
        public void test2() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,value2,value3"
            );

            ConversionResult<? extends Set<?>> result = converter.convert(context);
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new HashSet<>(Arrays.asList("value1", "value2", "value3")), 
                set
            );
        }

        @Test
        @DisplayName(
            "should convert to Set<String> when target type has no " + 
            "type parameters i.e. Set.class"
        )
        public void test3() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setInteger"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "1,2,3",
                // Override proxy method return type with a raw Set
                // No generic type parameter
                Set.class
            );
                
            ConversionResult<? extends Set<?>> result = converter.convert(context);

            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            // Strings and not Integers.
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("1", "2", "3"), 
                set
            );
        }

        @Test
        @DisplayName("should convert value to a Set using custom delimiter.")
        public void test4() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setCustomDelimiter"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1#value2#value3"
            );

            ConversionResult<? extends Set<?>> result = converter.convert(context);

            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new HashSet<>(Arrays.asList("value1", "value2", "value3")), 
                set
            );

        }

        @Test
        @DisplayName("should convert value according to the Set's generic type parameter.")
        public void test5() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setInteger"
                );
            
            Converter<?> rootConverter = new RootConverter(
                converter,
                new PrimitiveConverter()
            );

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "1,2,3"
            );
            
            ConversionResult<? extends Set<?>> result = converter.convert(context);
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof Integer));
            assertIterableEquals(
                Arrays.asList(1, 2, 3), 
                set
            );

        }

        @Test
        @DisplayName(
            "should return String values when Set's generic type parameter is a wildcard."
        )
        public void test6() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setPropertyWildcard"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,value2,value3"
            );

            ConversionResult<? extends Set<?>> result = converter.convert(context);
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new HashSet<>(Arrays.asList("value1", "value2", "value3")), 
                set
            );

        }

        @Test
        @DisplayName("should return String values when Set's generic type parameter is Object.")
        public void test7() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setPropertyObject"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = 
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1,value2,value3"
                );

            ConversionResult<? extends Set<?>> result = converter.convert(context);
        
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new HashSet<>(Arrays.asList("value1", "value2", "value3")), 
                set
            );

        }

        @Test
        @DisplayName("should return empty Set when property value is empty.")
        public void test8() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "" // Empty value.
            );

            ConversionResult<? extends Set<?>> result = converter.convert(context);
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertTrue(set.isEmpty());
        }

        @Test
        @DisplayName("should retain empty values from property value.")
        public void test9() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = 
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1,value2,value3,,value5" // Has empty values.
                );

            ConversionResult<? extends Set<?>> result = converter.convert(context);
        
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(5, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new HashSet<>(Arrays.asList("value1", "value2", "value3", "", "value5")), 
                set
            );

        }

        @Test
        @DisplayName("should strip empty values when annotated with @StripEmptyValues.")
        public void test10() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setPropertyStripEmpty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,,value3,,value5" // Has empty values.
            );

            ConversionResult<? extends Set<?>> result = converter.convert(context);
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new HashSet<>(Arrays.asList("value1", "value3", "value5")), 
                set
            );
        }

        @Test
        @DisplayName(
            "should throw when no rootConverter is registered that can handle " + 
            "the Set's generic type parameter."
        )
        public void test11() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setInteger"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            // No registered rootConverter for Integer.
            assertThrows(ExternalizedPropertiesException.class, () -> {
                converter.convert(
                    new ConversionContext(
                        rootConverter,
                        proxyMethod,
                        "1,2,3,4,5"
                    )
                );
            });
        }

        @Test
        @DisplayName(
            "should convert value according to the Set's generic type parameter. " + 
            "Generic type parameter is also a parameterized type e.g. Set<Optional<String>>."
        )
        public void test12() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setPropertyNestedGenerics" // Returns a Set<Optional<String>>.
                );
            
            Converter<?> rootConverter = new RootConverter(
                converter,
                new OptionalConverter() // Register additional Optional converter.
            );

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,value2,value3"
            );

            ConversionResult<? extends Set<?>> result = converter.convert(context);
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof Optional<?>));
            assertIterableEquals(
                new HashSet<>(Arrays.asList(
                    Optional.of("value1"), 
                    Optional.of("value2"), 
                    Optional.of("value3")
                )), 
                set
            );
        }

        @Test
        @DisplayName(
            "should convert value according to the Set's generic type parameter. " + 
            "Generic type parameter is generic array e.g. Set<Optional<String>[]>."
        )
        public void test13() {
            // Use LinkedHashSet for easy assertion later on this test case.
            SetConverter converter = converterToTest(LinkedHashSet::new);

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setPropertyNestedGenericsArray" // Returns a Set<Optional<String>[]>.
                );
            
            Converter<?> rootConverter = new RootConverter(
                converter,
                new ArrayConverter(), // Register additional array converter.
                new OptionalConverter() // Register additional Optional converter.
            );

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,value2,value3"
            );

            ConversionResult<? extends Set<?>> result = converter.convert(context);
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(3, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof Optional<?>[]));

            // Set is a set of arrays (Set<Optional<String>[]>).
            // Convert to List for set content assertion.
            List<?> setAsList = new ArrayList<>(set);
            Optional<?>[] item1 = (Optional<?>[])setAsList.get(0);
            Optional<?>[] item2 = (Optional<?>[])setAsList.get(1);
            Optional<?>[] item3 = (Optional<?>[])setAsList.get(2);

            assertArrayEquals(
                new Optional<?>[] { Optional.of("value1") }, 
                item1
            );

            assertArrayEquals(
                new Optional<?>[] { Optional.of("value2") }, 
                item2
            );
            
            assertArrayEquals(
                new Optional<?>[] { Optional.of("value3") }, 
                item3
            );
        }

        @Test
        @DisplayName("should throw when target type has a type variable e.g. Set<T>.")
        public void test14() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setPropertyT"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value"
            );
                
            assertThrows(
                ConversionException.class, 
                () -> converter.convert(context)
            );
        }



        @Test
        @DisplayName("should discard duplicate values.")
        public void test15() {
            SetConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);

            ConversionContext context = 
                new ConversionContext(
                    rootConverter,
                    proxyMethod,
                    "value1,value1,value1,value1,value5" // There are 4 value1
                );

            ConversionResult<? extends Set<?>> result = converter.convert(context);
        
            assertNotNull(result);
            Set<?> set = result.value();
            
            assertNotNull(set);
            assertEquals(2, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new HashSet<>(Arrays.asList("value1", "value5")), 
                set
            );

        }

        /**
         * Set factory tests.
         */
        
        @Test
        @DisplayName(
            "should use provided set factory to construct sets."
        )
        public void setFactoryTest1() {
            SetConverter converter = converterToTest(
                // Uses linked set.
                length -> new LinkedHashSet<>()
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,value2,value3,value4,value5"
            );

            ConversionResult<? extends Set<?>> result = converter.convert(context);
            
            assertNotNull(result);
            Set<?> set = result.value();
            
            // Default: Should use ',' as delimiter and will not strip empty values.
            // This will strip trailing empty values though.
            assertNotNull(set);
            assertTrue(set instanceof LinkedHashSet);
            assertEquals(5, set.size());
            assertTrue(set.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                new LinkedHashSet<>(Arrays.asList("value1", "value2", "value3", "value4", "value5")), 
                set
            );
        }
        
        @Test
        @DisplayName(
            "should throw when provided set factory returns null."
        )
        public void setFactoryTest2() {
            SetConverter converter = converterToTest(
                // Returns null.
                length -> null
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    SetProxyInterface.class,
                    "setProperty"
                );
            
            Converter<?> rootConverter = new RootConverter(converter);
            
            ConversionContext context = new ConversionContext(
                rootConverter,
                proxyMethod,
                "value1,,value3,,value5"
            );

            // Throws IllegalStateException if set factory returned null.
            assertThrows(
                IllegalStateException.class, 
                () -> converter.convert(context)
            );
        }
    }

    private SetConverter converterToTest() {
        return new SetConverter();
    }

    private SetConverter converterToTest(IntFunction<Set<?>> setFactory) {
        return new SetConverter(setFactory);
    }
}
