package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConversionContextTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when converter argument is null")
        public void test1() {
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );

            assertThrows(
                IllegalArgumentException.class, 
                () -> new ConversionContext(
                    null,
                    proxyMethod, 
                    "value"
                )
            );
        }

        @Test
        @DisplayName("should throw when proxy method info argument is null")
        public void test2() {
            Converter<?> converter = new RootConverter();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new ConversionContext(
                    converter,
                    (ProxyMethod)null, 
                    "value"
                )
            );
        }

        @Test
        @DisplayName("should throw when value argument is null")
        public void test3() {
            Converter<?> converter = new RootConverter();

            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> new ConversionContext(
                    converter,
                    proxyMethod, 
                    null
                )
            );
        }

        @Test
        @DisplayName("should throw when target type argument is null")
        public void test4() {
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );
            
            Converter<?> converter = new RootConverter();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new ConversionContext(
                    converter,
                    proxyMethod,
                    "value",
                    null
                )
            );
        }

        @Test
        @DisplayName(
            "should set target type to proxy method's generic return type"
        )
        public void test5() {
            Converter<?> converter = new RootConverter();
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );
            
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod, 
                "value"
            );

            assertEquals(proxyMethod.genericReturnType(), context.targetType());
            assertArrayEquals(
                proxyMethod.returnTypeGenericTypeParameters(), 
                context.targetTypeGenericTypeParameters()
            );
        }

        @Test
        @DisplayName(
            "should set target type to the target type argument"
        )
        public void test6() {
            Converter<?> converter = new RootConverter();
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );
            
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethod, 
                "value",
                Integer.class
            );

            // Proxy method return type is Optional but target type is Integer.
            assertNotEquals(proxyMethod.genericReturnType(), context.targetType());
        }
    }
}
