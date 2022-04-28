package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.ResolverProvider;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapResolverTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);
    
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when property source map argument is null.")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new MapResolver((Map<String, String>)null)
            );
        }

        @Test
        @DisplayName("should throw when unresolved property handler argument is null.")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new MapResolver(new HashMap<>(), null)
            );
        }
    }

    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            ResolverProvider<MapResolver> provider = 
                MapResolver.provider(Collections.emptyMap());

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            ResolverProvider<MapResolver> provider = 
                MapResolver.provider(Collections.emptyMap());

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ProviderMethodWithUnresolvedPropertyHandlerOverload {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            ResolverProvider<MapResolver> provider = 
                MapResolver.provider(
                    Collections.emptyMap(),
                    System::getProperty
                );

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            ResolverProvider<MapResolver> provider = 
                MapResolver.provider(
                    Collections.emptyMap(),
                    System::getProperty
                );

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ResolveMethod {
        // @Test
        // @DisplayName("should throw when proxy method argument is null or empty.")
        // void validationTest1() {
        //     MapResolver resolver = resolverToTest();
        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> resolver.resolve(null, "property")
        //     );
        // }

        // @Test
        // @DisplayName("should throw when property name argument is null or empty.")
        // void validationTest2() {
        //     MapResolver resolver = resolverToTest();
        //     ProxyMethod proxyMethod = proxyMethod(resolver);

        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> resolver.resolve(proxyMethod, (String)null)
        //     );
            
        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> resolver.resolve(proxyMethod, "")
        //     );
        // }

        @Test
        @DisplayName("should resolve values from the given map.")
        void test1() {
            Map<String, String> map = new HashMap<>();
            map.put("property", "property.value");
            
            MapResolver resolver = resolverToTest(map);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );

            Optional<String> result = resolver.resolve(
                proxyMethod, 
                "property"
            );

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                map.get("property"), 
                result.get()   
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional " + 
            "when property is not found from the given map."
        )
        void test2() {
            MapResolver resolver = resolverToTest(Collections.emptyMap());
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );

            Optional<String> result = resolver.resolve(
                proxyMethod, 
                "property"
            );
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName(
            "should invoke unresolved property handler " + 
            "when property is not found from the given map."
        )
        void test3() {
            AtomicBoolean unresolvedPropertyHandlerInvoked = new AtomicBoolean(false);

            Function<String, String> unresolvedPropertyHandler = 
                propertyName -> {
                    unresolvedPropertyHandlerInvoked.set(true);
                    return propertyName + "-default-value";
                };

            MapResolver resolver = resolverToTest(
                Collections.emptyMap(),
                unresolvedPropertyHandler
            );
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );

            Optional<String> result = 
                resolver.resolve(proxyMethod, "property");
            
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                unresolvedPropertyHandler.apply("property"), 
                result.get()    
            );
        }
    }

    private MapResolver resolverToTest(Map<String, String> map) {
        return new MapResolver(map);
    }

    private MapResolver resolverToTest(
            Map<String, String> map,
            Function<String, String> unresolverPropertyHandler
    ) {
        return new MapResolver(map, unresolverPropertyHandler);
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();
    }
}
