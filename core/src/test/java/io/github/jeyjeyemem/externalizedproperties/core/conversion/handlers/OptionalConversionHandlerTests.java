package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OptionalConversionHandlerTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when expected type is null.")
        public void test1() {
            OptionalConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when expected type is an Optional class.")
        public void test2() {
            OptionalConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Optional.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when expected type is not an Optional class.")
        public void test3() {
            OptionalConversionHandler handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            OptionalConversionHandler handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert resolved property to an Optional.")
        public void test2() {
            OptionalConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalProperty"
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value"
                );

            Optional<?> optional = handler.convert(context);
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof String);
            assertEquals("value", optional.get());
        }

        @Test
        @DisplayName(
            "should convert resolved property according to the Optional's generic type parameter."
        )
        public void test3() {
            OptionalConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "nonStringOptionalProperty"
                );
            
            Converter converter = new InternalConverter(
                handler,
                new PrimitiveConversionHandler()
            );

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "1"
                );

            Optional<?> optional = handler.convert(context);
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof Integer);
            assertEquals(1, optional.get());
        }

        @Test
        @DisplayName("should return String value when Optional's generic type parameter is Object.")
        public void test4() {
            OptionalConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalPropertyObject"
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value"
                );

            Optional<?> optional = handler.convert(context);
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof String);
            assertEquals("value", optional.get());
        }

        @Test
        @DisplayName(
            "should return String value when Optional's generic type parameter is a wildcard."
        )
        public void test5() {
            OptionalConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalPropertyWildcard"
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value"
                );

            Optional<?> optional = handler.convert(context);
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof String);
            assertEquals("value", optional.get());
        }

        @Test
        @DisplayName("should throw when expected type has a type variable e.g. Optional<T>.")
        public void test6() {
            OptionalConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalPropertyT"
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value"
                );
                
            assertThrows(
                ConversionException.class, 
                () -> handler.convert(context)
            );
        }

        @Test
        @DisplayName(
            "should throw when there is no expected type generic type parameters in context."
        )
        public void test7() {
            OptionalConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalProperty"
                );
            
            Converter converter = 
                new InternalConverter(handler);

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value",
                    Optional.class
                );
                
            assertThrows(
                ConversionException.class, 
                () -> handler.convert(context)
            );
        }

        @Test
        @DisplayName(
            "should convert resolved property according to the Optional's generic type parameter. " + 
            "Generic type parameter is also a parameterized type e.g. Optional<List<String>>"
        )
        public void test8() {
            OptionalConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalPropertyNestedGenerics"
                );
            
            Converter converter = new InternalConverter(
                handler,
                new CollectionConversionHandler() // Register additional List handler.
            );

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,value2,value3"
                );

            Optional<?> optional = handler.convert(context);
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof List<?>);
            assertIterableEquals(
                Arrays.asList("value1", "value2", "value3"), 
                (List<?>)optional.get()
            );
        }
        
        @Test
        @DisplayName(
            "should convert resolved property according to the Optional's generic type parameter. " + 
            "Generic type parameter is a generic array e.g. Optional<Optional<String>[]>."
        )
        public void test9() {
            OptionalConversionHandler handler = handlerToTest();

            ExternalizedPropertyMethodInfo propertyMethodInfo = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalPropertyNestedGenericsArray"
                );
            
            Converter converter = new InternalConverter(
                handler,
                new ArrayConversionHandler() // Register additional array handler.
            );

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethodInfo,
                    "value1,value2,value3"
                );

            Optional<?> optional = handler.convert(context);
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof Optional<?>[]);
            // Optional returns an array (Optional<?>[])
            assertArrayEquals(
                new Optional[] { 
                    Optional.of("value1"), 
                    Optional.of("value2"), 
                    Optional.of("value3") 
                }, 
                (Optional<?>[])optional.get()
            );
        }
    }

    private OptionalConversionHandler handlerToTest() {
        return new OptionalConversionHandler();
    }
}
