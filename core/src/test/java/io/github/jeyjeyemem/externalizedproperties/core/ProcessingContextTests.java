package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.processing.Base64Decode;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.JavaPropertiesProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ProcessorProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProcessingContextTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when proxy method argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new ProcessingContext(null, "value")
            );
        }

        @Test
        @DisplayName("should throw when value argument is null")
        public void test2() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> new ProcessingContext(
                    proxyMethod,
                    null
                )
            );
        }
    }

    @Nested
    class ValueMethod {
        @Test
        @DisplayName("should return value")
        public void test1() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            
            ProcessingContext context = 
                new ProcessingContext(proxyMethod, "value");

            assertEquals("value", context.value());
        }
    }

    @Nested
    class ProxyMethodMethod {
        @Test
        @DisplayName("should return proxy method")
        public void test1() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            
            ProcessingContext context = 
                new ProcessingContext(proxyMethod, "value");
            
            assertEquals(proxyMethod, context.proxyMethod());
        }
    }

    @Nested
    class AppliedProcessorsMethod {
        @Test
        @DisplayName(
            "should return empty list when there are no applied processors"
        )
        public void test1() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            
            ProcessingContext context = 
                new ProcessingContext(proxyMethod, "value");

            assertNotNull(context.appliedProcessors());
            assertTrue(context.appliedProcessors().isEmpty());
        }

        @Test
        @DisplayName("should return applied processors")
        public void test2() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            
            ProcessingContext context = new ProcessingContext(
                proxyMethod, 
                Base64.getEncoder().encodeToString("decoded".getBytes())
            )
            .with("decoded", Base64Decode.class);
            
            assertNotNull(context.appliedProcessors());
            assertTrue(context.appliedProcessors().contains(Base64Decode.class));
        }
    }

    @Nested
    class ProcessorClassesMethod {
        @Test
        @DisplayName(
            "should return empty Optional when proxy method " + 
            "is not annotated with @ProcessorClasses"
        )
        public void test1() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                JavaPropertiesProxyInterface.class, 
                "javaVersion"
            );
            
            ProcessingContext context = 
                new ProcessingContext(proxyMethod, "value");

            assertNotNull(context.processorClasses());
            assertFalse(context.processorClasses().isPresent());
        }

        @Test
        @DisplayName("should return processor classes")
        public void test2() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            
            ProcessingContext context = 
                new ProcessingContext(proxyMethod, "value");
            
            assertNotNull(context.processorClasses());
            assertTrue(context.processorClasses().isPresent());
        }
    }

    @Nested
    class GetAttributeForMethod {
        @Test
        @DisplayName(
            "should throw when processor class argument is null"
        )
        public void test1() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64DecodeWithAttribute"
            );
            
            ProcessingContext context = 
                new ProcessingContext(proxyMethod, "value");

            assertThrows(
                IllegalArgumentException.class, 
                () -> context.getAttributesFor(null)
            );
        }

        @Test
        @DisplayName(
            "should return map containing the attribute specific for the processor"
        )
        public void test2() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64DecodeWithAttribute"
            );
            
            ProcessingContext context = 
                new ProcessingContext(proxyMethod, "value");

            Map<String, String> attributes = 
                context.getAttributesFor(Base64Decode.class);
            assertNotNull(attributes);
            assertFalse(attributes.isEmpty());
            assertEquals(
                "Base64Decode.testAttributeValue", 
                attributes.get("Base64Decode.testAttribute")
            );
        }

        @Test
        @DisplayName(
            "should return empty map when @ProcessorClasses " +
            "does not have any attributes" 
        )
        public void test3() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            
            ProcessingContext context = 
                new ProcessingContext(proxyMethod, "value");
            
            Map<String, String> attributes = 
                context.getAttributesFor(Base64Decode.class);
            assertNotNull(attributes);
            assertTrue(attributes.isEmpty());
        }

        @Test
        @DisplayName(
            "should return empty map when proxy method " +
            "is not annotated with @ProcessorClasses" 
        )
        public void test4() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                BasicProxyInterface.class, 
                "property"
            );
            
            ProcessingContext context = 
                new ProcessingContext(proxyMethod, "value");
            
            Map<String, String> attributes = 
                context.getAttributesFor(Base64Decode.class);
            assertNotNull(attributes);
            assertTrue(attributes.isEmpty());
        }

        @Test
        @DisplayName(
            "should not include attribute to the map when attribute " +
            "is configured for a different processor" 
        )
        public void test5() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "specificAttributes"
            );
            
            ProcessingContext context = 
                new ProcessingContext(proxyMethod, "value");
            
            Map<String, String> attributes = 
                context.getAttributesFor(Base64Decode.class);
            assertNotNull(attributes);
            // TestProcessor.testAttribute is only for TestProcessor processor.
            assertFalse(attributes.containsKey("TestProcessor.testAttribute"));
        }
        
        @Test
        @DisplayName(
            "should include attribute to the map when attribute " +
            "is not configured for any processors" 
        )
        public void test6() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "specificAttributes"
            );
            
            ProcessingContext context = 
                new ProcessingContext(proxyMethod, "value");
            
            Map<String, String> attributes = 
                context.getAttributesFor(Base64Decode.class);
            assertNotNull(attributes);
            // Shared.testAttribute is not configured for specific processors
            // i.e. ProcessorAttribute.forProcessors was not populated.
            assertEquals(
                "Shared.testAttributeValue",
                attributes.get("Shared.testAttribute")
            );
        }
    }

    @Nested
    class WithMethod {

    }
}
