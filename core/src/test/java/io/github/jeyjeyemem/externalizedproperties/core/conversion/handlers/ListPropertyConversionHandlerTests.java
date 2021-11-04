package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListPropertyConversionHandlerTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when expected type is null.")
        public void test1() {
            ListPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a List class.")
        public void test2() {
            ListPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(List.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is a Collection class.")
        public void test3() {
            ListPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Collection.class);
            assertTrue(canConvert);
        }



        @Test
        @DisplayName("should return false when expected type is not a List/Collection class.")
        public void test4() {
            ListPropertyConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            ListPropertyConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert resolved property to a List.")
        public void test2() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list", "value1,value2,value3")
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );
        }

        @Test
        @DisplayName("should convert resolved property to a List using custom delimiter.")
        public void test3() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listCustomDelimiter"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);
            
            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list", "value1#value2#value3")
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should convert resolved property according to the List's generic type parameter.")
        public void test4() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listInteger"
                );
            
            ResolvedPropertyConverter converter = new InternalResolvedPropertyConverter(
                handler,
                new IntegerPropertyConversionHandler()
            );

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list.integer", "1,2,3")
                );
            
            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof Integer));
            assertIterableEquals(
                Arrays.asList(1, 2, 3), 
                list
            );

        }

        @Test
        @DisplayName("should return String values when List's generic type parameter is a wildcard.")
        public void test5() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyWildcard"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list.wildcard", "value1,value2,value3")
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should return String values when List's generic type parameter is Object.")
        public void test6() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyObject"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with(
                        "property.list.object", 
                        "value1,value2,value3"
                    )
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                list
            );

        }

        @Test
        @DisplayName("should return empty List when property value is empty.")
        public void test7() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list", "") // Empty value.
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.isEmpty());
        }

        @Test
        @DisplayName("should retain empty values from property value.")
        public void test8() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list", "value1,,value3,,value5") // Has empty values.
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 5);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "", "value3", "", "value5"), 
                list
            );

        }

        @Test
        @DisplayName("should strip empty values when annotated with @StripEmptyValues.")
        public void test9() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyStripEmpty"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with(
                        "property.list.stripempty", 
                        "value1,,value3,,value5" // Has empty values.
                    )
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertIterableEquals(
                Arrays.asList("value1", "value3", "value5"), 
                list
            );
        }

        @Test
        @DisplayName(
            "should throw when no conversion handler is registered that can handle " + 
            "the List's generic type parameter."
        )
        public void test10() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listInteger"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);
            
            // No registered conversion handler for Integer.
            assertThrows(ExternalizedPropertiesException.class, () -> {
                handler.convert(
                    new ResolvedPropertyConversionContext(
                        converter,
                        propertyMethodInfo,
                        ResolvedProperty.with("property.list.integer", "1,2,3,4,5")
                    )
                );
            });
        }

        @Test
        @DisplayName(
            "should convert resolved property according to the List's generic type parameter. " + 
            "Generic type parameter is also a parameterized type e.g. List<Optional<String>>."
        )
        public void test11() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyNestedGenerics" // Returns a List<Optional<String>>.
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(
                    handler,
                    new OptionalPropertyConversionHandler() // Register additional Optional handler.
                );

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with(
                        "property.list.nested.generics", 
                        "value1,value2,value3"
                    )
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
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
            "should convert resolved property according to the List's generic type parameter. " + 
            "Generic type parameter is generic array e.g. List<Optional<String>[]>."
        )
        public void test12() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyNestedGenericsArray" // Returns a List<Optional<String>[]>.
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(
                    handler,
                    new ArrayPropertyConversionHandler(), // Register additional array handler.
                    new OptionalPropertyConversionHandler() // Register additional Optional handler.
                );

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with(
                        "property.list.nested.generics.array", 
                        "value1,value2,value3"
                    )
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
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
        @DisplayName("should throw when expected type has a type variable e.g. List<T>.")
        public void test13() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyT"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list.T", "value")
                );
                
            assertThrows(
                ResolvedPropertyConversionException.class, 
                () -> handler.convert(context)
            );
        }

        @Test
        @DisplayName(
            "should throw when there is no expected type generic type parameters in context."
        )
        public void test14() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listProperty"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionContext context = 
                new ResolvedPropertyConversionContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list", "value1,value2,value3"),
                    List.class
                );
                
            assertThrows(
                ResolvedPropertyConversionException.class, 
                () -> handler.convert(context)
            );
        }
    }

    private ListPropertyConversionHandler handlerToTest() {
        return new ListPropertyConversionHandler();
    }
}
