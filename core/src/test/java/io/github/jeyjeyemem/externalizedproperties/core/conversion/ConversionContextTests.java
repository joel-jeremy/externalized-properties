package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubProxyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConversionContextTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when converter argument is null")
        public void test1() {
            StubProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );

            assertThrows(
                IllegalArgumentException.class, 
                () -> new ConversionContext(
                    null,
                    proxyMethodInfo, 
                    "value"
                )
            );
        }

        @Test
        @DisplayName("should throw when proxy method info argument is null")
        public void test2() {
            Converter converter = new InternalConverter();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new ConversionContext(
                    converter,
                    (ProxyMethodInfo)null, 
                    "value"
                )
            );
        }

        @Test
        @DisplayName("should throw when value argument is null")
        public void test3() {
            Converter converter = new InternalConverter();

            StubProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> new ConversionContext(
                    converter,
                    proxyMethodInfo, 
                    null
                )
            );
        }

        @Test
        @DisplayName(
            "should set target type to proxy method's generic return type"
        )
        public void test6() {
            Converter converter = new InternalConverter();
            StubProxyMethodInfo proxyMethodInfo = 
                StubProxyMethodInfo.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );
            
            ConversionContext context = new ConversionContext(
                converter,
                proxyMethodInfo, 
                "value"
            );

            assertEquals(proxyMethodInfo.genericReturnType(), context.targetType());
            assertArrayEquals(
                proxyMethodInfo.returnTypeGenericTypeParameters(), 
                context.targetTypeGenericTypeParameters()
            );
        }
    }
}
