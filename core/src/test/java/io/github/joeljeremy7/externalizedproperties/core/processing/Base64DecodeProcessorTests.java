package io.github.joeljeremy7.externalizedproperties.core.processing;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ProcessorProvider;
import io.github.joeljeremy7.externalizedproperties.core.testentities.proxy.ProcessorProxyInterface;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Base64DecodeProcessorTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when decoder argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> new Base64DecodeProcessor((Base64.Decoder)null)
            );
        }
    }

    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null")
        void test1() {
            ProcessorProvider<Base64DecodeProcessor> provider = 
                Base64DecodeProcessor.provider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should not return null on get")
        void test2() {
            ProcessorProvider<Base64DecodeProcessor> provider = 
                Base64DecodeProcessor.provider();

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ProcessPropertyMethod {
        @Test
        @DisplayName("should apply base 64 decoding to property")
        void test1() {
            String property = "test";
            String base64Property = base64Encode(property, Base64.getEncoder());
            
            Base64DecodeProcessor base64Decode = new Base64DecodeProcessor();
            String decoded = base64Decode.process(
                ProxyMethodUtils.fromMethod(
                    ProcessorProxyInterface.class, 
                    "base64Decode"
                ), 
                base64Property
            );

            assertEquals(property, decoded);
        }

        @Test
        @DisplayName("should apply base 64 decoding to property using configured default decoder")
        void test2() {
            String property = "test";
            Base64.Decoder decoder = Base64.getUrlDecoder();
            
            String base64Property = base64Encode(property, Base64.getUrlEncoder());
            
            Base64DecodeProcessor base64Decode = new Base64DecodeProcessor(decoder);
            String decoded = base64Decode.process(
                ProxyMethodUtils.fromMethod(
                    ProcessorProxyInterface.class, 
                    "base64Decode"
                ), 
                base64Property
            );

            assertEquals(property, decoded);
        }

        @Test
        @DisplayName("should wrap exceptions in ProcessingException and propagate.")
        void test3() {
            Base64.Decoder decoder = Base64.getDecoder();
            
            String invalidBase64 = "%%%";
            
            Base64DecodeProcessor base64Decode = new Base64DecodeProcessor(decoder);

            assertThrows(
                ProcessingException.class, 
                () -> base64Decode.process(
                    ProxyMethodUtils.fromMethod(
                        ProcessorProxyInterface.class, 
                        "base64Decode"
                    ), 
                    invalidBase64
                )
            );
        }

        @Test
        @DisplayName("should use URL base 64 decoder when @Base64Decode encoding is url")
        void encodingTest1() {
            String property = "test";
            
            String base64Property = base64Encode(property, Base64.getUrlEncoder());
            
            Base64DecodeProcessor base64Decode = new Base64DecodeProcessor();
            String decoded = base64Decode.process(
                ProxyMethodUtils.fromMethod(
                    ProcessorProxyInterface.class, 
                    "base64DecodeUrl"
                ), 
                base64Property
            );

            assertEquals(property, decoded);
        }

        @Test
        @DisplayName("should use MIME base 64 decoder when @Base64Decode encoding is mime")
        void encodingTest2() {
            String property = "test";
            
            String base64Property = base64Encode(property, Base64.getMimeEncoder());
            
            Base64DecodeProcessor base64Decode = new Base64DecodeProcessor();
            String decoded = base64Decode.process(
                ProxyMethodUtils.fromMethod(
                    ProcessorProxyInterface.class, 
                    "base64DecodeMime"
                ), 
                base64Property
            );

            assertEquals(property, decoded);
        }

        @Test
        @DisplayName("should use basic base 64 decoder when @Base64Decode encoding is basic")
        void encodingTest3() {
            String property = "test";
            
            String base64Property = base64Encode(property, Base64.getEncoder());
            
            Base64DecodeProcessor base64Decode = new Base64DecodeProcessor();
            String decoded = base64Decode.process(
                ProxyMethodUtils.fromMethod(
                    ProcessorProxyInterface.class, 
                    "base64DecodeBasic"
                ), 
                base64Property
            );

            assertEquals(property, decoded);
        }

        @Test
        @DisplayName(
            "should use configured default base 64 decoder when a @Base64Decode encoding " +
            "is not specified"
        )
        void encodingTest4() {
            String property = "test";
            
            String base64Property = base64Encode(property, Base64.getEncoder());
            
            Base64DecodeProcessor base64Decode = new Base64DecodeProcessor(Base64.getDecoder());
            String decoded = base64Decode.process(
                ProxyMethodUtils.fromMethod(
                    ProcessorProxyInterface.class, 
                    "base64Decode"
                ), 
                base64Property
            );

            assertEquals(property, decoded);
        }

        @Test
        @DisplayName(
            "should use charset when a @Base64Decode charset is specified"
        )
        void charsetTest1() {
            String property = "test";
            
            String base64Property = base64Encode(property, Base64.getEncoder());
            
            Base64DecodeProcessor base64Decode = new Base64DecodeProcessor(Base64.getDecoder());
            String decoded = base64Decode.process(
                ProxyMethodUtils.fromMethod(
                    ProcessorProxyInterface.class, 
                    "base64DecodeUtf16"
                ), 
                base64Property
            );

            // Convert string to UTF-16.
            String utf16String = new String(property.getBytes(), StandardCharsets.UTF_16);
            assertEquals(utf16String, decoded);
        }

        private String base64Encode(String property, Base64.Encoder encoder) {
            return new String(encoder.encode(property.getBytes()));
        }
    }
}
