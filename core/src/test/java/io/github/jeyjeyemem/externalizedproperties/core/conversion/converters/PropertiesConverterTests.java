package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PropertiesProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertiesConverterTests {

    private static final String PROPERTIES_TEXT = 
        "test.property=test.property.value" + System.lineSeparator() +
        "test.property.2=test.property.2.value" + System.lineSeparator() +
        "test.property.3=test.property.3.value";

    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should return false when target type is null.")
        public void test1() {
            PropertiesConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(null);
            assertFalse(canConvert);
        }

        @Test
        @DisplayName("should return true when target type is a Properties class.")
        public void test2() {
            PropertiesConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Properties.class);
            assertTrue(canConvert);
        }

        @Test
        @DisplayName("should return false when target type is not a Properties class.")
        public void test3() {
            PropertiesConverter handler = handlerToTest();
            boolean canConvert = handler.canConvertTo(Integer.class);
            assertFalse(canConvert);
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should throw when context is null.")
        public void test1() {
            PropertiesConverter handler = handlerToTest();
            assertThrows(IllegalArgumentException.class, () -> handler.convert(null));
        }

        @Test
        @DisplayName("should convert value to Properties.")
        public void test2() {
            PropertiesConverter handler = handlerToTest();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PropertiesProxyInterface.class,
                    "properties"
                );
            
            Converter<?> converter = new RootConverter(handler);

            ConversionResult<? extends Properties> result = 
                handler.convert(new ConversionContext(
                    converter,
                    proxyMethod,
                    PROPERTIES_TEXT
                ));

            assertNotNull(result);
            Properties properties = result.value();

            assertNotNull(properties);
            assertEquals(3, properties.size());
            assertEquals("test.property.value", properties.getProperty("test.property"));
            assertEquals("test.property.2.value", properties.getProperty("test.property.2"));
            assertEquals("test.property.3.value", properties.getProperty("test.property.3"));
        }

        @Test
        @DisplayName("should return empty Properties when value is empty.")
        public void test3() {
            PropertiesConverter handler = handlerToTest();
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PropertiesProxyInterface.class,
                    "properties"
                );
            
            Converter<?> converter = new RootConverter(handler);
            
            ConversionResult<? extends Properties> result = handler.convert(
                new ConversionContext(
                    converter,
                    proxyMethod,
                    "" // Empty value.
                )
            );
            assertNotNull(result);
            Properties properties = result.value();
            
            assertNotNull(properties);
            assertTrue(properties.isEmpty());
        }

        @Test
        @DisplayName("should throw when properties cannot be loaded.")
        public void test4() {
            PropertiesConverter handler = handlerToTest();
            
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    PropertiesProxyInterface.class,
                    "properties"
                );
            
            Converter<?> converter = new RootConverter(handler);
            
            assertThrows(
                ConversionException.class, 
                () -> handler.convert(new ConversionContext(
                    converter,
                    proxyMethod,
                    "\\uxxxx" // Invalid character encoding.
                ))
            );
        }

        /**
         * Non-proxy tests.
         */

        // @Test
        // @DisplayName("should convert value to Properties.")
        // public void nonProxyTest2() {
        //     PropertiesConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(handler);
            
        //     ConversionResult<? extends Properties> result = handler.convert(
        //         new ConversionContext(
        //             converter,
        //             Properties.class,
        //             PROPERTIES_TEXT
        //         )
        //     );

        //     assertNotNull(result);
        //     Properties properties = result.value();
            
        //     assertNotNull(properties);
        //     assertEquals(3, properties.size());
        //     assertEquals("test.property.value", properties.getProperty("test.property"));
        //     assertEquals("test.property.2.value", properties.getProperty("test.property.2"));
        //     assertEquals("test.property.3.value", properties.getProperty("test.property.3"));
        // }

        // @Test
        // @DisplayName("should return empty Properties when value is empty.")
        // public void nonProxyTest3() {
        //     PropertiesConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(handler);
            
        //     ConversionResult<? extends Properties> result = handler.convert(
        //         new ConversionContext(
        //             converter,
        //             Properties.class,
        //             "" // Empty value.
        //         )
        //     );
        //     assertNotNull(result);
        //     Properties properties = result.value();
            
        //     assertNotNull(properties);
        //     assertTrue(properties.isEmpty());
        // }
    
        // @Test
        // @DisplayName("should throw when properties cannot be loaded.")
        // public void test4() {
        //     PropertiesConverter handler = handlerToTest();
            
        //     Converter<?> converter = new RootConverter(handler);
            
        //     assertThrows(
        //         ConversionException.class, 
        //         () -> handler.convert(new ConversionContext(
        //             converter,
        //             Properties.class,
        //             "\\uxxxx" // Invalid character encoding.
        //         ))
        //     );
        // }
    }

    private PropertiesConverter handlerToTest() {
        return new PropertiesConverter();
    }
}
