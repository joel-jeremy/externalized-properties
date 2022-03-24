package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.processing.Base64DecodeProcessor;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ProcessorProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProcessingContextTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when proxy method argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new ProcessingContext(null, "value")
            );
        }

        @Test
        @DisplayName("should throw when value argument is null")
        void test2() {
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
        void test1() {
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
        void test1() {
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
        void test1() {
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
        void test2() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            
            ProcessingContext context = new ProcessingContext(
                proxyMethod, 
                base64Encode("decoded")
            )
            .with("decoded", Base64DecodeProcessor.class);
            
            assertNotNull(context.appliedProcessors());
            assertTrue(context.appliedProcessors().contains(Base64DecodeProcessor.class));
        }
    }

    @Nested
    class WithMethod {
        @Test
        @DisplayName(
            "should throw when value argument is null"
        )
        void test1() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            ProcessingContext context = new ProcessingContext(
                proxyMethod, 
                base64Encode("decoded")
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> context.with(null, Base64DecodeProcessor.class)
            );
        }

        @Test
        @DisplayName(
            "should throw when applied processor argument is null"
        )
        void test2() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            ProcessingContext context = new ProcessingContext(
                proxyMethod, 
                base64Encode("decoded")
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> context.with("value", null)
            );
        }

        @Test
        @DisplayName(
            "should create a new processing context instance"
        )
        void test3() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            ProcessingContext context = new ProcessingContext(
                proxyMethod, 
                base64Encode("decoded")
            );

            ProcessingContext newContext = context.with(
                base64Encode("decoded-new-value"), 
                Base64DecodeProcessor.class
            );

            assertNotSame(context, newContext);
        }

        @Test
        @DisplayName(
            "should create a new processing context with the given value"
        )
        void test4() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            ProcessingContext context = new ProcessingContext(
                proxyMethod, 
                base64Encode("decoded")
            );

            String newValue = base64Encode("decoded-new-value");
            ProcessingContext newContext = context.with(
                newValue, 
                Base64DecodeProcessor.class
            );

            assertEquals(newValue, newContext.value());
        }

        @Test
        @DisplayName(
            "should create a new processing context with the given applied processor"
        )
        void test5() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            ProcessingContext context = new ProcessingContext(
                proxyMethod, 
                base64Encode("decoded")
            );

            ProcessingContext newContext = context.with(
                base64Encode("decoded-new-value"), 
                Base64DecodeProcessor.class
            );

            assertTrue(newContext.appliedProcessors().contains(Base64DecodeProcessor.class));
        }

        @Test
        @DisplayName(
            "should append applied processor argument to previously added list"
        )
        void test6() {
            ProxyMethod proxyMethod = ProxyMethodUtils.fromMethod(
                ProcessorProxyInterface.class, 
                "base64Decode"
            );
            ProcessingContext context = new ProcessingContext(
                proxyMethod, 
                base64Encode("decoded")
            );

            String newValue = base64Encode("decoded-new-value");
            ProcessingContext newContext1 = context.with(
                newValue, 
                Base64DecodeProcessor.class
            );

            ProcessingContext newContext2 = newContext1.with(
                newValue + "-processor-1", 
                TestProcessor1.class
            );

            ProcessingContext newContext3 = newContext2.with(
                newValue + "-processor-2", 
                TestProcessor2.class
            );

            assertTrue(newContext1.appliedProcessors().contains(Base64DecodeProcessor.class));
            assertFalse(newContext1.appliedProcessors().contains(TestProcessor1.class));
            assertFalse(newContext1.appliedProcessors().contains(TestProcessor2.class));

            assertTrue(newContext2.appliedProcessors().contains(Base64DecodeProcessor.class));
            assertTrue(newContext2.appliedProcessors().contains(TestProcessor1.class));
            assertFalse(newContext2.appliedProcessors().contains(TestProcessor2.class));

            assertTrue(newContext3.appliedProcessors().contains(Base64DecodeProcessor.class));
            assertTrue(newContext3.appliedProcessors().contains(TestProcessor1.class));
            assertTrue(newContext3.appliedProcessors().contains(TestProcessor2.class));
        }
    }

    static String base64Encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    static class TestProcessor1 implements Processor {
        @Override
        public String process(ProcessingContext context) {
            return context.value() + "-processor-1";
        }
    }

    static class TestProcessor2 implements Processor {
        @Override
        public String process(ProcessingContext context) {
            return context.value() + "-processor-2";
        }
    }
}
