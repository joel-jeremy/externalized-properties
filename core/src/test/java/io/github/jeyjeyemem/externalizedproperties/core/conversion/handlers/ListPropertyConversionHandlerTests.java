package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListPropertyConversionHandlerTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return true when expected type is null.")
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

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list", "value1,value2,value3")
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertEquals("value1", list.get(0));
            assertEquals("value2", list.get(1));
            assertEquals("value3", list.get(2));
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
            
            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list", "value1#value2#value3")
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertEquals("value1", list.get(0));
            assertEquals("value2", list.get(1));
            assertEquals("value3", list.get(2));
        }

        @Test
        @DisplayName("should convert resolved property according to the List's generic parameter type.")
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

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list.integer", "1,2,3")
                );
            
            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof Integer));
            assertEquals(1, list.get(0));
            assertEquals(2, list.get(1));
            assertEquals(3, list.get(2));
        }

        @Test
        @DisplayName("should return String values when List's generic parameter type is a wildcard.")
        public void test5() {
            ListPropertyConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    ListProxyInterface.class,
                    "listPropertyWildcard"
                );
            
            ResolvedPropertyConverter converter = 
                new InternalResolvedPropertyConverter(handler);

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list.wildcard", "value1,value2,value3")
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 3);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertEquals("value1", list.get(0));
            assertEquals("value2", list.get(1));
            assertEquals("value3", list.get(2));
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

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
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
            assertEquals("value1", list.get(0));
            assertEquals("value2", list.get(1));
            assertEquals("value3", list.get(2));
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

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
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

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
                    converter,
                    propertyMethodInfo,
                    ResolvedProperty.with("property.list", "value1,,value3,,value5") // Has empty values.
                );

            List<?> list = handler.convert(context);
            
            assertNotNull(list);
            assertTrue(list.size() == 5);
            assertTrue(list.stream().allMatch(v -> v instanceof String));
            assertEquals("value1", list.get(0));
            assertEquals("", list.get(1));
            assertEquals("value3", list.get(2));
            assertEquals("", list.get(3));
            assertEquals("value5", list.get(4));
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

            ResolvedPropertyConversionHandlerContext context = 
                new ResolvedPropertyConversionHandlerContext(
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
            assertEquals("value1", list.get(0));
            assertEquals("value3", list.get(1));
            assertEquals("value5", list.get(2));
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
            
            assertThrows(ExternalizedPropertiesException.class, () -> {
                handler.convert(
                    new ResolvedPropertyConversionHandlerContext(
                        converter,
                        propertyMethodInfo,
                        ResolvedProperty.with("property.list.integer", "1,2,3,4,5"), 
                        propertyMethodInfo.returnType(), // No registered conversion handler for Integer.
                        propertyMethodInfo.genericReturnTypeParameters()
                    )
                );
            });
        }
    }

    private ListPropertyConversionHandler handlerToTest() {
        return new ListPropertyConversionHandler();
    }
}
