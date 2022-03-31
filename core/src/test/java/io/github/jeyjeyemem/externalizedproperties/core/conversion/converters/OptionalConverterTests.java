package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
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

public class OptionalConverterTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        public void test1() {
            OptionalConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is an Optional class.")
        public void test2() {
            OptionalConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Optional.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not an Optional class.")
        public void test3() {
            OptionalConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(String.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            OptionalConverter handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert value to an Optional.")
        public void test2() {
            OptionalConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalProperty"
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "value"
            );

            ConversionResult<? extends Optional<?>> result = handler.convert(context);

            assertNotNull(result);
            Optional<?> optional = result.value();
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof String);
            assertEquals("value", optional.get());
        }

        @Test
        @DisplayName(
            "should convert value according to the Optional's generic type parameter."
        )
        public void test3() {
            OptionalConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class,
                    "nonStringOptionalProperty"
                );
            
            Converter<?> converter = new RootConverter(
                handler,
                new PrimitiveConverter()
            );

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "1"
            );

            ConversionResult<? extends Optional<?>> result = handler.convert(context);

            assertNotNull(result);
            Optional<?> optional = result.value();
            
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof Integer);
            assertEquals(1, optional.get());
        }

        @Test
        @DisplayName(
            "should return String value when target type has no " + 
            "type parameters i.e. Optional.class"
        )
        public void test4() {
            OptionalConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class,
                    "nonStringOptionalProperty"
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "1",
                // Override proxy method return type with a raw Optional
                // No generic type parameter
                Optional.class
            );

            ConversionResult<? extends Optional<?>> result = handler.convert(context);
            
            assertNotNull(result);
            Optional<?> optional = result.value();

            assertNotNull(optional);
            assertTrue(optional.isPresent());
            // String and not Integer.
            assertTrue(optional.get() instanceof String);
            assertEquals("1", optional.get());
        }

        @Test
        @DisplayName("should return String value when Optional's generic type parameter is Object.")
        public void test5() {
            OptionalConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalPropertyObject"
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "value"
            );

            ConversionResult<? extends Optional<?>> result = handler.convert(context);
            
            assertNotNull(result);
            Optional<?> optional = result.value();

            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof String);
            assertEquals("value", optional.get());
        }

        @Test
        @DisplayName(
            "should return String value when Optional's generic type parameter is a wildcard."
        )
        public void test6() {
            OptionalConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalPropertyWildcard"
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "value"
            );

            ConversionResult<? extends Optional<?>> result = handler.convert(context);
            
            assertNotNull(result);
            Optional<?> optional = result.value();

            assertNotNull(optional);
            assertTrue(optional.isPresent());
            assertTrue(optional.get() instanceof String);
            assertEquals("value", optional.get());
        }

        @Test
        @DisplayName("should throw when target type has a type variable e.g. Optional<T>.")
        public void test7() {
            OptionalConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalPropertyT"
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "value"
            );
                
            assertThrows(
                ConversionException.class, 
                () -> handler.convert(context)
            );
        }

        @Test
        @DisplayName(
            "should convert value according to the Optional's generic type parameter. " + 
            "Generic type parameter is also a parameterized type e.g. Optional<List<String>>."
        )
        public void test8() {
            OptionalConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalPropertyNestedGenerics"
                );
            
            Converter<?> converter = new RootConverter(
                handler,
                new ListConverter() // Register additional List handler.
            );

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "value1,value2,value3"
            );

            ConversionResult<? extends Optional<?>> result = handler.convert(context);
            
            assertNotNull(result);
            Optional<?> optional = result.value();

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
            "should convert value according to the Optional's generic type parameter. " + 
            "Generic type parameter is a generic array e.g. Optional<Optional<String>[]>."
        )
        public void test9() {
            OptionalConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalPropertyNestedGenericsArray"
                );
            
            Converter<?> converter = new RootConverter(
                handler,
                new ArrayConverter() // Register additional array handler.
            );

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "value1,value2,value3"
            );

            ConversionResult<? extends Optional<?>> result = handler.convert(context);
            
            assertNotNull(result);
            Optional<?> optional = result.value();

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

        @Test
        @DisplayName(
            "should convert value to an empty Optional when property value is empty."
        )
        public void test10() {
            OptionalConverter handler = handlerToTest();
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class,
                    "optionalProperty"
                );

            Converter<?> converter = new RootConverter(handler);

            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod,
                "" // Empty.
            );

            ConversionResult<? extends Optional<?>> result = handler.convert(context);
            
            assertNotNull(result);
            Optional<?> optional = result.value();
            
            assertNotNull(optional);
            assertFalse(optional.isPresent());
        }

        /**
         * Non-proxy tests.
         */

        // @Test
        // @DisplayName("should convert value to an Optional.")
        // public void nonProxyTest1() {
        //     OptionalConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(handler);

        //     ConversionContext context = new ConversionContext(
        //         converter,
        //         Optional.class,
        //         "value"
        //     );

        //     ConversionResult<? extends Optional<?>> result = handler.convert(context);

        //     assertNotNull(result);
        //     Optional<?> optional = result.value();
            
        //     assertNotNull(optional);
        //     assertTrue(optional.isPresent());
        //     assertTrue(optional.get() instanceof String);
        //     assertEquals("value", optional.get());
        // }

        // @Test
        // @DisplayName(
        //     "should convert value according to the Optional's generic type parameter."
        // )
        // public void nonProxyTest2() {
        //     OptionalConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(
        //         handler,
        //         new PrimitiveConverter()
        //     );

        //     ConversionContext context = new ConversionContext(
        //         converter,
        //         new TypeReference<Optional<Integer>>(){}.type(),
        //         "1"
        //     );

        //     ConversionResult<? extends Optional<?>> result = handler.convert(context);

        //     assertNotNull(result);
        //     Optional<?> optional = result.value();
            
        //     assertNotNull(optional);
        //     assertTrue(optional.isPresent());
        //     assertTrue(optional.get() instanceof Integer);
        //     assertEquals(1, optional.get());
        // }

        // @Test
        // @DisplayName("should return String value when Optional's generic type parameter is Object.")
        // public void nonProxyTest3() {
        //     OptionalConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(handler);

        //     ConversionContext context = new ConversionContext(
        //         converter,
        //         new TypeReference<Optional<Object>>(){}.type(),
        //         "value"
        //     );

        //     ConversionResult<? extends Optional<?>> result = handler.convert(context);
            
        //     assertNotNull(result);
        //     Optional<?> optional = result.value();

        //     assertNotNull(optional);
        //     assertTrue(optional.isPresent());
        //     assertTrue(optional.get() instanceof String);
        //     assertEquals("value", optional.get());
        // }

        // @Test
        // @DisplayName(
        //     "should return String value when Optional's generic type parameter is a wildcard."
        // )
        // public void nonProxyTest4() {
        //     OptionalConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(handler);

        //     ConversionContext context = new ConversionContext(
        //         converter,
        //         new TypeReference<Optional<?>>(){}.type(),
        //         "value"
        //     );

        //     ConversionResult<? extends Optional<?>> result = handler.convert(context);
            
        //     assertNotNull(result);
        //     Optional<?> optional = result.value();

        //     assertNotNull(optional);
        //     assertTrue(optional.isPresent());
        //     assertTrue(optional.get() instanceof String);
        //     assertEquals("value", optional.get());
        // }

        // @Test
        // @DisplayName("should throw when target type has a type variable e.g. Optional<T>.")
        // public <T> void nonProxyTest5() {
        //     OptionalConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(handler);

        //     ConversionContext context = new ConversionContext(
        //         converter,
        //         new TypeReference<Optional<T>>(){}.type(),
        //         "value"
        //     );
                
        //     assertThrows(
        //         ConversionException.class, 
        //         () -> handler.convert(context)
        //     );
        // }

        // @Test
        // @DisplayName(
        //     "should convert value according to the Optional's generic type parameter. " + 
        //     "Generic type parameter is also a parameterized type e.g. Optional<List<String>>."
        // )
        // public void nonProxyTest6() {
        //     OptionalConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(
        //         handler,
        //         new ListConverter() // Register additional List handler.
        //     );

        //     ConversionContext context = new ConversionContext(
        //         converter,
        //         new TypeReference<Optional<List<String>>>(){}.type(),
        //         "value1,value2,value3"
        //     );

        //     ConversionResult<? extends Optional<?>> result = handler.convert(context);
            
        //     assertNotNull(result);
        //     Optional<?> optional = result.value();

        //     assertNotNull(optional);
        //     assertTrue(optional.isPresent());
        //     assertTrue(optional.get() instanceof List<?>);
        //     assertIterableEquals(
        //         Arrays.asList("value1", "value2", "value3"), 
        //         (List<?>)optional.get()
        //     );
        // }
        
        // @Test
        // @DisplayName(
        //     "should convert value according to the Optional's generic type parameter. " + 
        //     "Generic type parameter is a generic array e.g. Optional<Optional<String>[]>."
        // )
        // public void nonProxyTest7() {
        //     OptionalConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(
        //         handler,
        //         new ArrayConverter() // Register additional array handler.
        //     );

        //     ConversionContext context = new ConversionContext(
        //         converter,
        //         new TypeReference<Optional<Optional<String>[]>>(){}.type(),
        //         "value1,value2,value3"
        //     );

        //     ConversionResult<? extends Optional<?>> result = handler.convert(context);
            
        //     assertNotNull(result);
        //     Optional<?> optional = result.value();

        //     assertNotNull(optional);
        //     assertTrue(optional.isPresent());
        //     assertTrue(optional.get() instanceof Optional<?>[]);
        //     // Optional returns an array (Optional<?>[])
        //     assertArrayEquals(
        //         new Optional[] { 
        //             Optional.of("value1"), 
        //             Optional.of("value2"), 
        //             Optional.of("value3") 
        //         }, 
        //         (Optional<?>[])optional.get()
        //     );
        // }

        // @Test
        // @DisplayName(
        //     "should convert value to an empty Optional when property value is empty."
        // )
        // public void nonProxyTest8() {
        //     OptionalConverter handler = handlerToTest();

        //     Converter<?> converter = new RootConverter(handler);

        //     ConversionContext context = new ConversionContext(
        //         converter,
        //         Optional.class,
        //         "" // Empty.
        //     );

        //     ConversionResult<? extends Optional<?>> result = handler.convert(context);
            
        //     assertNotNull(result);
        //     Optional<?> optional = result.value();
            
        //     assertNotNull(optional);
        //     assertFalse(optional.isPresent());
        // }
    }

    private OptionalConverter handlerToTest() {
        return new OptionalConverter();
    }
}
