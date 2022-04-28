package io.github.jeyjeyemem.externalizedproperties.core.internal.processing;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.Processor;
import io.github.jeyjeyemem.externalizedproperties.core.ProcessorProvider;
import io.github.jeyjeyemem.externalizedproperties.core.processing.Base64DecodeProcessor;
import io.github.jeyjeyemem.externalizedproperties.core.processing.ProcessingException;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ProcessorProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RootProcessorTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when externalized properties argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootProcessor(
                    null,
                    ep -> new Base64DecodeProcessor()
                )
            );
        }

        @Test
        @DisplayName("should throw when processors varargs argument is null")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootProcessor(
                    ExternalizedProperties.builder().withDefaults().build(),
                    (ProcessorProvider[])null
                )
            );
        }

        @Test
        @DisplayName("should throw when processors collection argument is null")
        void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootProcessor(
                    ExternalizedProperties.builder().withDefaults().build(),
                    (Collection<ProcessorProvider<?>>)null
                )
            );
        }
    }

    @Nested
    class ProviderMethodWithVarArgsOverload {
        @Test
        @DisplayName("should throw when processor providers argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> RootProcessor.provider((ProcessorProvider<?>[])null)
            );
        }

        @Test
        @DisplayName("should not return null")
        void test2() {
            ProcessorProvider<RootProcessor> provider = 
                RootProcessor.provider(Base64DecodeProcessor.provider());

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should not return null on get")
        void test3() {
            ProcessorProvider<RootProcessor> provider = 
                RootProcessor.provider(Base64DecodeProcessor.provider());

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ProviderMethodCollectionOverload {
        @Test
        @DisplayName("should throw when processor providers argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> RootProcessor.provider((Collection<ProcessorProvider<?>>)null)
            );
        }

        @Test
        @DisplayName("should not return null")
        void test2() {
            ProcessorProvider<RootProcessor> provider = 
                RootProcessor.provider(
                    Arrays.asList(Base64DecodeProcessor.provider())
                );

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should not return null on get")
        void test3() {
            ProcessorProvider<RootProcessor> provider = 
                RootProcessor.provider(
                    Arrays.asList(Base64DecodeProcessor.provider())
                );

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ProcessMethod {
        @Test
        @DisplayName("should throw when proxy method argument is null")
        void test1() {
            RootProcessor processor = rootProcessor();

            assertThrows(
                IllegalArgumentException.class, 
                () -> processor.process(null, "valueToProcess")
            );
        }
        @Test
        @DisplayName("should throw when context argument is null")
        void test2() {
            RootProcessor processor = rootProcessor(
                Base64DecodeProcessor.provider()
            );

            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    ProcessorProxyInterface.class, 
                    "base64Decode"
                );

            assertThrows(
                IllegalArgumentException.class, 
                () -> processor.process(proxyMethod, null)
            );
        }

        @Test
        @DisplayName(
            "should process property using configured processor classes"
        )
        void test3() {
            RootProcessor processor = rootProcessor(
                Base64DecodeProcessor.provider()
            );

            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    ProcessorProxyInterface.class, 
                    "base64Decode"
                );

            String plainText = "plain-text-value";
            String base64Encoded = 
                Base64.getEncoder().encodeToString(plainText.getBytes());

            String result = processor.process(
                proxyMethod, 
                base64Encoded
            );

            assertEquals(plainText, result);
        }

        @Test
        @DisplayName(
            "should when required processor class is not configured"
        )
        void test4() {
            Processor stubProcessor = new Processor() {
                @Override
                public String process(ProxyMethod proxyMethod, String valueToProcess) {
                    return valueToProcess;
                }
            };

            RootProcessor processor = rootProcessor(
                ep -> stubProcessor
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

            assertThrows(
                ProcessingException.class, 
                () -> processor.process(proxyMethod, base64Encoded))
            ;
        }
    }
    private RootProcessor rootProcessor(
            ProcessorProvider<?>... processorProviders
    ) {
        return new RootProcessor(
            ExternalizedProperties.builder()
                .withDefaultResolvers()
                .processors(processorProviders)
                .build(),
            processorProviders
        );
    }
}
