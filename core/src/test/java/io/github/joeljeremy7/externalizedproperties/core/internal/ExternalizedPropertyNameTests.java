package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.TestProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExternalizedPropertyNameTests {
    private static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
        ExternalizedProperties.builder().build();
    private static final TestProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new TestProxyMethodFactory<>(ProxyInterface.class);
    
    @Nested
    class FromProxyMethodInvocationMethods {
        @Test
        @DisplayName("should return the proxy method @ExternalizedProperty value")
        void proxyMethodOverloadTest1() {
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property, 
                EXTERNALIZED_PROPERTIES
            );
            
            Optional<String> name = ExternalizedPropertyName.fromProxyMethodInvocation(
                proxyMethod, 
                new Object[0]
            );

            assertTrue(name.isPresent());
            assertEquals(
                proxyMethod.findAnnotation(ExternalizedProperty.class)
                    .map(ExternalizedProperty::value)
                    .orElse(null),
                name.get()
            );
        }

        @Test
        @DisplayName(
            "should derive property name from proxy method invocation args value " + 
            "when no @ExternalizedProperty value is specified"
        )
        void proxyMethodOverloadTest2() {
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::resolve, 
                EXTERNALIZED_PROPERTIES
            );
            
            String propertyNameArg = "property";
            Optional<String> name = ExternalizedPropertyName.fromProxyMethodInvocation(
                proxyMethod, 
                new Object[] { propertyNameArg }
            );

            assertTrue(name.isPresent());
            assertEquals(
                propertyNameArg,
                name.get()
            );
        }

        @Test
        @DisplayName(
            "should throw when no @ExternalizedProperty value is specified and there " + 
            "is no property name provided via proxy method invocation args"
        )
        void proxyMethodOverloadTest3() {
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::resolve, 
                EXTERNALIZED_PROPERTIES
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> ExternalizedPropertyName.fromProxyMethodInvocation(
                    proxyMethod, 
                    new Object[0] // No property name invocation arguments
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when no @ExternalizedProperty value is specified and the " + 
            "property name provided via proxy method invocation args is null"
        )
        void proxyMethodOverloadTest4() {
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::resolve, 
                EXTERNALIZED_PROPERTIES
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> ExternalizedPropertyName.fromProxyMethodInvocation(
                    proxyMethod, 
                    new Object[] { null } // Null property name in invocation arguments
                )
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> ExternalizedPropertyName.fromProxyMethodInvocation(
                    proxyMethod, 
                    new Object[] { "" } // Empty property name in invocation arguments
                )
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional when proxy method is not annotated with " +
            "@ExternalizedProperty annotation"
        )
        void proxyMethodOverloadTest5() {
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::noAnnotation, 
                EXTERNALIZED_PROPERTIES
            );
            
            Optional<String> name = ExternalizedPropertyName.fromProxyMethodInvocation(
                proxyMethod, 
                new Object[0]
            );
            
            assertFalse(name.isPresent());
        }

        @Test
        @DisplayName("should return the proxy method @ExternalizedProperty value")
        void methodOverloadTest1() {
            Method proxyMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class,
                ProxyInterface::property
            );
            
            Optional<String> name = ExternalizedPropertyName.fromProxyMethodInvocation(
                proxyMethod, 
                new Object[0]
            );

            assertTrue(name.isPresent());
            assertEquals(
                proxyMethod.getAnnotation(ExternalizedProperty.class).value(),
                name.get()
            );
        }

        @Test
        @DisplayName(
            "should derive property name from proxy method invocation args value " + 
            "when no @ExternalizedProperty value is specified"
        )
        void methodOverloadTest2() {
            Method proxyMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class,
                ProxyInterface::resolve
            );
            
            String propertyNameArg = "property";
            Optional<String> name = ExternalizedPropertyName.fromProxyMethodInvocation(
                proxyMethod, 
                new Object[] { propertyNameArg }
            );

            assertTrue(name.isPresent());
            assertEquals(
                propertyNameArg,
                name.get()
            );
        }

        @Test
        @DisplayName(
            "should throw when no @ExternalizedProperty value is specified and there " + 
            "is no property name provided via proxy method invocation args"
        )
        void methodOverloadTest3() {
            Method proxyMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class,
                ProxyInterface::resolve
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> ExternalizedPropertyName.fromProxyMethodInvocation(
                    proxyMethod, 
                    new Object[0] // No property name invocation arguments
                )
            );
        }

        @Test
        @DisplayName(
            "should throw when no @ExternalizedProperty value is specified and the " + 
            "property name provided via proxy method invocation args is null"
        )
        void methodOverloadTest4() {
            Method proxyMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class,
                ProxyInterface::resolve
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> ExternalizedPropertyName.fromProxyMethodInvocation(
                    proxyMethod, 
                    new Object[] { null } // Null property name in invocation arguments
                )
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> ExternalizedPropertyName.fromProxyMethodInvocation(
                    proxyMethod, 
                    new Object[] { "" } // Empty property name in invocation arguments
                )
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional when proxy method is not annotated with " +
            "@ExternalizedProperty annotation"
        )
        void methodOverloadTest5() {
            Method proxyMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class,
                ProxyInterface::noAnnotation
            );
            
            Optional<String> name = ExternalizedPropertyName.fromProxyMethodInvocation(
                proxyMethod, 
                new Object[0]
            );

            assertFalse(name.isPresent());
        }
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();

        @ExternalizedProperty
        String resolve(String propertyName);

        String noAnnotation();
    }
}
