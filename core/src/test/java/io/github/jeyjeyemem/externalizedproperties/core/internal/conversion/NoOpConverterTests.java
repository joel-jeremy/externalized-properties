package io.github.jeyjeyemem.externalizedproperties.core.internal.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethods;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class NoOpConverterTests {
    @Nested
    class CanConvertToMethod {
        @Test
        @DisplayName("should always return false")
        public void test1() {
            assertFalse(
                NoOpConverter.INSTANCE.canConvertTo(int.class)
            );
            assertFalse(
                NoOpConverter.INSTANCE.canConvertTo(List.class)
            );
            assertFalse(
                NoOpConverter.INSTANCE.canConvertTo(Set.class)
            );
            assertFalse(
                NoOpConverter.INSTANCE.canConvertTo(Optional.class)
            );
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName("should always return skip result")
        public void test1() {
            ProxyMethod intProxyMethod = ProxyMethods.intProperty();
            ProxyMethod booleanProxyMethod = ProxyMethods.booleanProperty();
            ProxyMethod doubleProxyMethod = ProxyMethods.doubleProperty();
            
            ConversionResult<?> intResult = 
                NoOpConverter.INSTANCE.convert(
                    intProxyMethod, 
                    "1", 
                    int.class
                );
            
            ConversionResult<?> booleanResult = 
                NoOpConverter.INSTANCE.convert(
                    booleanProxyMethod, 
                    "true", 
                    boolean.class
                );
            
            ConversionResult<?> doubleResult = 
                NoOpConverter.INSTANCE.convert(
                    doubleProxyMethod, 
                    "1.0", 
                    double.class
                );

            ConversionResult<?> skipResult = ConversionResult.skip();

            assertEquals(skipResult, intResult);
            assertEquals(skipResult, booleanResult);
            assertEquals(skipResult, doubleResult);
        }
    }
}
