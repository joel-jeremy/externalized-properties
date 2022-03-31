package io.github.jeyjeyemem.externalizedproperties.core.internal.processing;

import io.github.jeyjeyemem.externalizedproperties.core.ProcessingContext;
import io.github.jeyjeyemem.externalizedproperties.core.Processor;
import io.github.jeyjeyemem.externalizedproperties.core.processing.Base64DecodeProcessor;
import io.github.jeyjeyemem.externalizedproperties.core.processing.ProcessingException;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ProcessorProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RootProcessorTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when processors varargs argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootProcessor((Processor[])null)
            );
        }

        @Test
        @DisplayName("should throw when processors collection argument is null")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootProcessor((Collection<Processor>)null)
            );
        }
    }

    @Nested
    class ProcessMethod {
        @Test
        @DisplayName("should throw when context argument is null")
        void test1() {
            RootProcessor processor = new RootProcessor(
                new Base64DecodeProcessor()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> processor.process(null)
            );
        }

        @Test
        @DisplayName(
            "should process property using configured processor classes"
        )
        void test2() {
            RootProcessor processor = new RootProcessor(
                new Base64DecodeProcessor()
            );

            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    ProcessorProxyInterface.class, 
                    "base64Decode"
                );

            String plainText = "plain-text-value";
            String base64Encoded = 
                Base64.getEncoder().encodeToString(plainText.getBytes());

            ProcessingContext context = new ProcessingContext(
                proxyMethod, 
                base64Encoded
            );

            String result = processor.process(context);

            assertEquals(plainText, result);
        }

        @Test
        @DisplayName(
            "should when required processor class is not configured"
        )
        void test3() {
            Processor stubProcessor = new Processor() {
                @Override
                public String process(ProcessingContext context) {
                    return context.value();
                }
            };
            RootProcessor processor = new RootProcessor(
                stubProcessor
                // Base64Decode processor not configured.
            );

            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    ProcessorProxyInterface.class, 
                    "base64Decode"
                );

            String plainText = "plain-text-value";
            String base64Encoded = 
                Base64.getEncoder().encodeToString(plainText.getBytes());

            ProcessingContext context = new ProcessingContext(
                proxyMethod, 
                base64Encoded
            );

            assertThrows(
                ProcessingException.class, 
                () -> processor.process(context))
            ;
        }
    }
}
