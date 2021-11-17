package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.MapPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.VoidReturnTypeProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExternalizedPropertiesTests {
    @Nested
    class ProxyMethod {
        @Test
        @DisplayName("should not return null")
        public void validationTest1() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface = 
                externalizedProperties.proxy(BasicProxyInterface.class);

            assertNotNull(proxyInterface);
        }

        @Test
        @DisplayName("should throw when proxy interface is null")
        public void validationTest2() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(null)
            );
        }

        @Test
        @DisplayName("should throw when proxy interface is not an interface")
        public void validationTest3() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(ExternalizedPropertiesTests.class)
            );
        }

        @Test
        @DisplayName("should not allow proxy interface methods with void return type")
        public void validationTest4() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(VoidReturnTypeProxyInterface.class)
            );
        }
    }

    @Nested
    class ResolvePropertyMethod {
        @Test
        @DisplayName("should resolve property from resolvers")
        public void test1() {
            Map<String, String> map = new HashMap<>();
            map.put("property", "value");

            ExternalizedProperties externalizedProperties = 
                externalizedProperties(map);

            Optional<String> propertyValue = 
                externalizedProperties.resolveProperty("property");

            assertNotNull(propertyValue);
            assertTrue(propertyValue.isPresent());
            assertEquals("value", propertyValue.get());
        }

        @Test
        @DisplayName(
            "should return empty Optional when property cannot be resolved from resolvers"
        )
        public void test2() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            Optional<String> propertyValue = 
                externalizedProperties.resolveProperty("nonexistent.property");

            assertNotNull(propertyValue);
            assertFalse(propertyValue.isPresent());
        }
    }

    private ExternalizedProperties externalizedProperties(
            Map<String, String> propertySource,
            ConversionHandler<?>... conversionHandlers
    ) {
        return externalizedProperties(
            Arrays.asList(new MapPropertyResolver(propertySource)),
            Arrays.asList(conversionHandlers)
        );
    }

    private ExternalizedProperties externalizedProperties(
            Collection<ExternalizedPropertyResolver> resolvers,
            Collection<ConversionHandler<?>> conversionHandlers
    ) {
        ExternalizedPropertiesBuilder builder = 
            ExternalizedPropertiesBuilder.newBuilder()
                .resolvers(resolvers)
                .conversionHandlers(conversionHandlers)
                .withCaching(Duration.ofMinutes(5));
        
        if (conversionHandlers.size() == 0) {
            builder.withDefaultConversionHandlers();
        }

        return builder.build();
    }
}
