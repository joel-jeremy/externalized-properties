package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.ResolverProvider;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.MapResolver.UnresolvedPropertyHandler;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertiesResolverTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);
    private static final Properties EMPTY_PROPERTIES = new Properties();

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when properties argument is null.")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new PropertiesResolver((Properties)null)
            );
        }

        @Test
        @DisplayName("should throw when unresolved property handler argument is null.")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new PropertiesResolver(EMPTY_PROPERTIES, null)
            );
        }

        @Test
        @DisplayName("should ignore properties with non-String keys or values.")
        void test3() {
            Properties props = new Properties();
            props.put("property.nonstring", 123);
            props.put(123, "property.nonstring.key");
            props.put("property", "property.value");

            PropertiesResolver resolver = resolverToTest(props);
            ProxyMethod intPropertyProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::propertyNonString
            );
            ProxyMethod propertyProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );

            Optional<String> nonStringResult = 
                resolver.resolve(intPropertyProxyMethod, "property.nonstring");
            Optional<String> result = 
                resolver.resolve(propertyProxyMethod, "property");
            
            assertFalse(nonStringResult.isPresent());
            assertTrue(result.isPresent());

            assertEquals(
                props.get("property"), 
                result.get()
            );
        }
    }

    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        void test1() {
            ResolverProvider<PropertiesResolver> provider = 
                PropertiesResolver.provider(new Properties());

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        void test2() {
            ResolverProvider<PropertiesResolver> provider = 
                PropertiesResolver.provider(new Properties());

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }


    @Nested
    class ProviderMethodWithUnresolvedPropertyHandlerOverload {
        @Test
        @DisplayName("should not return null.")
        void test1() {
            ResolverProvider<PropertiesResolver> provider = 
                PropertiesResolver.provider(
                    new Properties(),
                    System::getProperty
                );

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        void test2() {
            ResolverProvider<PropertiesResolver> provider = 
                PropertiesResolver.provider(
                    new Properties(),
                    System::getProperty
                );

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve property value from the given properties.")
        void test1() {
            Properties props = new Properties();
            props.setProperty("property", "property.value");
            
            PropertiesResolver resolver = resolverToTest(props);
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
                props.getProperty("property"), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional " + 
            "when property is not found from the given properties."
        )
        void test2() {
            PropertiesResolver resolver = resolverToTest(EMPTY_PROPERTIES);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );

            Optional<String> result = resolver.resolve(
                proxyMethod, 
                "property" // Not in Properties.
            );
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName(
            "should invoke unresolved property handler " + 
            "when property is not found from the given properties."
        )
        void test3() {
            AtomicBoolean unresolvedPropertyHandlerInvoked = new AtomicBoolean(false);

            UnresolvedPropertyHandler unresolvedPropertyHandler = 
                propertyName -> {
                    unresolvedPropertyHandlerInvoked.set(true);
                    return propertyName + "-default-value";
                };

            PropertiesResolver resolver = resolverToTest(
                EMPTY_PROPERTIES,
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
                unresolvedPropertyHandler.handle("property"), 
                result.get()
            );
        }
    }

    private PropertiesResolver resolverToTest(Properties properties) {
        return new PropertiesResolver(properties);
    }

    private PropertiesResolver resolverToTest(
            Properties properties,
            UnresolvedPropertyHandler unresolverPropertyHandler
    ) {
        return new PropertiesResolver(properties, unresolverPropertyHandler);
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();

        @ExternalizedProperty("property.nonstring")
        int propertyNonString();
    }
}
