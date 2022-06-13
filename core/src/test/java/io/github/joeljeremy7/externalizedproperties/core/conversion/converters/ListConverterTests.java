package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.conversion.Delimiter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.StripEmptyValues;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.ListConverter.ListFactory;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListConverterTests {
    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(ProxyInterface.class);

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
    class CanConvertToMethod {
        @Test
        @DisplayName("should return true when target type is a List class.")
        void test1() {
            ListConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(List.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Collection class.")
        void test2() {
            ListConverter converter = converterToTest();
            boolean canConvert = converter.canConvertTo(Collection.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not a List/Collection class.")
        void test3() {
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

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listProperty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends List<?>> result = converter.convert(
                context,
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

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listInteger,
                externalizedProperties(converter)
            );
                
            ConversionResult<? extends List<?>> result = converter.convert(
                context,
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

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listCustomDelimiter,
                externalizedProperties(converter)
            );

            ConversionResult<? extends List<?>> result = converter.convert(
                context,
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
            ListConverter converter = converterToTest();

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listInteger,
                externalizedProperties(converter, new IntegerConverter())
            );
            
            ConversionResult<? extends List<?>> result = converter.convert(
                context,
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

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listPropertyWildcard,
                externalizedProperties(converter)
            );

            ConversionResult<? extends List<?>> result = converter.convert(
                context,
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

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listPropertyObject,
                externalizedProperties(converter)
            );

            ConversionResult<? extends List<?>> result = converter.convert(
                context,
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

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listProperty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends List<?>> result = converter.convert(
                context,
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

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listProperty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends List<?>> result = converter.convert(
                context,
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

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listPropertyStripEmpty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends List<?>> result = converter.convert(
                context,
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

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listInteger,
                externalizedProperties(converter)
            );
            
            // No registered rootConverter for Integer.
            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> converter.convert(context, "1,2,3,4,5")
            );
        }

        @Test
        @DisplayName(
            "should convert value according to the List's generic type parameter. " + 
            "Generic type parameter is also a parameterized type e.g. List<Optional<String>>."
        )
        void test11() {
            ListConverter converter = converterToTest();

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listPropertyNestedGenerics, // Returns a List<Optional<String>>.
                externalizedProperties(converter)
            );

            ConversionResult<? extends List<?>> result = converter.convert(
                context,
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
            ListConverter converter = converterToTest();

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listPropertyNestedGenericsArray, // Returns a List<Optional<String>[]>.
                externalizedProperties(converter, new ArrayConverter())
            );

            ConversionResult<? extends List<?>> result = converter.convert(
                context,
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

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listPropertyT,
                externalizedProperties(converter)
            );
                
            assertThrows(
                ConversionException.class, 
                () -> converter.convert(context, "value")
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
                // Uses CopyOnWriteArrayList.
                capacity -> new CopyOnWriteArrayList<>()
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listProperty,
                externalizedProperties(converter)
            );

            ConversionResult<? extends List<?>> result = converter.convert(
                context,
                "value1,value2,value3,value4,value5"
            );
            
            assertNotNull(result);
            List<?> list = result.value();
            
            // Default: Should use ',' as delimiter and will not strip empty values.
            // This will strip trailing empty values though.
            assertNotNull(list);
            assertTrue(list instanceof CopyOnWriteArrayList);
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
                capacity -> null
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listProperty,
                externalizedProperties(converter)
            );
            
            // Throws IllegalStateException if list factory returned null.
            assertThrows(
                IllegalStateException.class, 
                () -> converter.convert(
                    context,
                    "value1,value2,value3"
                )
            );
        }
        
        @Test
        @DisplayName(
            "should throw when provided list factory returns a populated list."
        )
        void listFactoryTest3() {
            ListConverter converter = converterToTest(
                // Returns a populated list.
                capacity -> Arrays.asList("this", "should", "not", "be", "populated")
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::listProperty,
                externalizedProperties(converter)
            );
            
            // Throws IllegalStateException if list factory returned a populated list.
            assertThrows(
                IllegalStateException.class, 
                () -> converter.convert(
                    context,
                    "value1,value2,value3"
                )
            );
        }
    }

    private static ListConverter converterToTest(ListFactory listFactory) {
        return new ListConverter(listFactory);
    }

    private static ListConverter converterToTest() { 
        return new ListConverter();
    }

    private static ExternalizedProperties externalizedProperties(
            ListConverter converterToTest,
            Converter<?>... additionalConverters
    ) {
        return ExternalizedProperties.builder()
            .converters(converterToTest)
            .converters(additionalConverters)
            .build();
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property.list")
        List<String> listProperty();

        @ExternalizedProperty("property.list.object")
        List<Object> listPropertyObject();

        @ExternalizedProperty("property.list.custom.delimiter")
        @Delimiter("#")
        List<String> listCustomDelimiter();

        @ExternalizedProperty("property.list.integer")
        List<Integer> listInteger();

        @ExternalizedProperty("property.list.wildcard")
        List<?> listPropertyWildcard();

        @ExternalizedProperty("property.collection")
        Collection<String> collectionProperty();

        @ExternalizedProperty("property.collection.custom.delimiter")
        @Delimiter("#")
        Collection<String> collectionCustomDelimiter();

        @ExternalizedProperty("property.collection.integer")
        Collection<Integer> collectionInteger();

        @ExternalizedProperty("property.collection.wildcard")
        Collection<?> collectionPropertyWildcard();

        @ExternalizedProperty("property.list.stripempty")
        @StripEmptyValues
        List<String> listPropertyStripEmpty();

        @ExternalizedProperty("property.list.nested.generics")
        List<Optional<String>> listPropertyNestedGenerics();

        @ExternalizedProperty("property.list.nested.generics.array")
        List<Optional<String>[]> listPropertyNestedGenericsArray();

        @ExternalizedProperty("property.list.T")
        <T> List<T> listPropertyT();
    }
}
