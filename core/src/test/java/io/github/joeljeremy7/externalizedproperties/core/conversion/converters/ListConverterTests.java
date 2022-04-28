package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListConverterTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when list factory argument is null.")
        void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> new ListConverter(null)
            );
        }
    }

    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            ConverterProvider<ListConverter> provider = 
                ListConverter.provider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            ConverterProvider<ListConverter> provider = 
                ListConverter.provider();
            
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
    class ProviderMethodWithListFactoryOverload {
        @Test
        @DisplayName("should throw when list factory argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ListConverter.provider(null)
            );
        }

        @Test
        @DisplayName("should not return null.")
        public void test2() {
            ConverterProvider<ListConverter> provider = 
                ListConverter.provider(ArrayList::new);

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test3() {
            ConverterProvider<ListConverter> provider = 
                ListConverter.provider(ArrayList::new);
            
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
        @DisplayName("should return false when target type is null.")
        void test1() {
            ListConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a List class.")
        void test2() {
            ListConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(List.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Collection class.")
        void test3() {
            ListConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Collection.class);
            assertTrue(canConvert);
        }



        @Test
        @DisplayName("should return false when target type is not a List/Collection class.")
        void test4() {
            ListConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should convert value to a List.")
        void test1() {
            ListConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );

            ConversionResult<? extends List<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );
        }

        @Test
        @DisplayName(
            "should convert to List<String> when target type has no " + 
            "type parameters i.e. List.class"
        )
        void test2() {
            ListConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listInteger"
                );
                
            ConversionResult<? extends List<?>> result = converter.convert(
                proxyMethod,
                "1,2,3",
                // Override proxy method return type with a raw List
                // No generic type parameter
                List.class
            );

            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            // Strings and not Integers.
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("1", "2", "3"), 
                list
            );
        }

        @Test
        @DisplayName("should convert value to a List using custom delimiter.")
        void test3() {
            ListConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listCustomDelimiter"
                );

            ConversionResult<? extends List<?>> result = converter.convert(
                proxyMethod,
                "value1#value2#value3"
            );

            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should convert value according to the List's generic type parameter.")
        void test4() {
            ListConverter converter = converterToTest(
                PrimitiveConverter.provider()
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listInteger"
                );
            
            ConversionResult<? extends List<?>> result = converter.convert(
                proxyMethod,
                "1,2,3"
            );
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof Integer));
            assertIterableEquals(
                Arrays.asList(1, 2, 3), 
                list
            );

        }

        @Test
        @DisplayName(
            "should return String values when List's generic type parameter is a wildcard."
        )
        void test5() {
            ListConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyWildcard"
                );

            ConversionResult<? extends List<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should return String values when List's generic type parameter is Object.")
        void test6() {
            ListConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyObject"
                );

            ConversionResult<? extends List<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );
        
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should return empty List when property value is empty.")
        void test7() {
            ListConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );

            ConversionResult<? extends List<?>> result = converter.convert(
                proxyMethod,
                "" // Empty value.
            );
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("should retain empty values from property value.")
        void test8() {
            ListConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );

            ConversionResult<? extends List<?>> result = converter.convert(
                proxyMethod,
                "value1,,value3,,value5" // Has empty values.
            );
        
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(5, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "", "value3", "", "value5"), 
                list
            );

        }

        @Test
        @DisplayName("should strip empty values when annotated with @StripEmptyValues.")
        void test9() {
            ListConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyStripEmpty"
                );

            ConversionResult<? extends List<?>> result = converter.convert(
                proxyMethod,
                "value1,,value3,,value5" // Has empty values.
            );
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value3", "value5"), 
                list
            );
        }

        @Test
        @DisplayName(
            "should throw when no rootConverter is registered that can handle " + 
            "the List's generic type parameter."
        )
        void test10() {
            ListConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listInteger"
                );
            
            // No registered rootConverter for Integer.
            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> converter.convert(proxyMethod, "1,2,3,4,5")
            );
        }

        @Test
        @DisplayName(
            "should convert value according to the List's generic type parameter. " + 
            "Generic type parameter is also a parameterized type e.g. List<Optional<String>>."
        )
        void test11() {
            ListConverter converter = converterToTest(
                OptionalConverter.provider()
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyNestedGenerics" // Returns a List<Optional<String>>.
                );

            ConversionResult<? extends List<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof Optional<?>));
            assertIterableEquals(
                Arrays.asList(
                    Optional.of("value1"), 
                    Optional.of("value2"), 
                    Optional.of("value3")
                ), 
                list
            );
        }

        @Test
        @DisplayName(
            "should convert value according to the List's generic type parameter. " + 
            "Generic type parameter is generic array e.g. List<Optional<String>[]>."
        )
        void test12() {
            ListConverter converter = converterToTest(
                OptionalConverter.provider(),
                ArrayConverter.provider()
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyNestedGenericsArray" // Returns a List<Optional<String>[]>.
                );

            ConversionResult<? extends List<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3"
            );
            
            assertNotNull(result);
            List<?> list = result.value();
            
            assertNotNull(list);
            assertEquals(3, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof Optional<?>[]));

            // List is a list of arrays (List<Optional<String>[]>).
            
            Optional<?>[] item1 = (Optional<?>[])list.get(0);
            Optional<?>[] item2 = (Optional<?>[])list.get(1);
            Optional<?>[] item3 = (Optional<?>[])list.get(2);

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
        @DisplayName("should throw when target type has a type variable e.g. List<T>.")
        void test13() {
            ListConverter converter = converterToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyT"
                );
                
            assertThrows(
                ConversionException.class, 
                () -> converter.convert(proxyMethod, "value")
            );
        }

        /**
         * List factory tests.
         */
        
        @Test
        @DisplayName(
            "should use provided list factory to construct lists/collections."
        )
        void listFactoryTest1() {
            ListConverter converter = converterToTest(
                // Uses linked list.
                length -> new LinkedList<>()
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );

            ConversionResult<? extends List<?>> result = converter.convert(
                proxyMethod,
                "value1,value2,value3,value4,value5"
            );
            
            assertNotNull(result);
            List<?> list = result.value();
            
            // Default: Should use ',' as delimiter and will not strip empty values.
            // This will strip trailing empty values though.
            assertNotNull(list);
            assertTrue(list instanceof LinkedList);
            assertEquals(5, list.size());
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3", "value4", "value5"), 
                list
            );
        }
        
        @Test
        @DisplayName(
            "should throw when provided list factory returns null."
        )
        void listFactoryTest2() {
            ListConverter converter = converterToTest(
                // Returns null.
                length -> null
            );

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            // Throws IllegalStateException if list factory returned null.
            assertThrows(
                IllegalStateException.class, 
                () -> converter.convert(
                    proxyMethod,
                    "value1,value2,value3,value4,value5"
                )
            );
        }
    }

    private ListConverter converterToTest(ConverterProvider<?>... additionalConverters) {
        return converterToTest(ListConverter.provider(), additionalConverters);
    }

    private ListConverter converterToTest(
            IntFunction<List<?>> listFactory,
            ConverterProvider<?>... additionalConverters
    ) {
        return converterToTest(
            ListConverter.provider(listFactory), 
            additionalConverters
        );
    }

    private ListConverter converterToTest(
            ConverterProvider<ListConverter> converterToTestProvider,
            ConverterProvider<?>... additionalConverters
    ) { 
        List<ConverterProvider<?>> allProviders = new ArrayList<>(
            Arrays.asList(additionalConverters)
        );
        allProviders.add(converterToTestProvider);
        
        ExternalizedProperties externalizedProperties = 
            ExternalizedProperties.builder()
                .withDefaultResolvers()
                .converters(allProviders)
                .build();

        RootConverter rootConverter = new RootConverter(
            externalizedProperties, 
            allProviders
        );
        return converterToTestProvider.get(externalizedProperties, rootConverter);
    }
}
