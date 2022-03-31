package io.github.jeyjeyemem.externalizedproperties.core.processing;

import io.github.jeyjeyemem.externalizedproperties.core.ProcessingContext;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethodUtils;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ProcessorProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    class ProcessPropertyMethod {
        @Test
        @DisplayName("should throw when property argument is null")
        void test1() {
            Base64DecodeProcessor base64Decode = new Base64DecodeProcessor();
            assertThrows(
                IllegalArgumentException.class, 
                () -> base64Decode.process(null)
            );
        }

        @Test
        @DisplayName("should apply base 64 decoding to property")
        void test2() {
            String property = "test";
            String base64Property = base64Encode(property, Base64.getEncoder());
            
            Base64DecodeProcessor base64Decode = new Base64DecodeProcessor();
            String decoded = base64Decode.process(
                new ProcessingContext(
                    ProxyMethodUtils.fromMethod(ProcessorProxyInterface.class, "base64Decode"), 
                    base64Property
                )
            );

            assertEquals(property, decoded);
        }

        @Test
        @DisplayName("should apply base 64 decoding to property using configured decoder")
        void test3() {
            String property = "test";
            Base64.Decoder decoder = Base64.getUrlDecoder();
            
            String base64Property = base64Encode(property, Base64.getUrlEncoder());
            
            Base64DecodeProcessor base64Decode = new Base64DecodeProcessor(decoder);
            String decoded = base64Decode.process(
                new ProcessingContext(
                    ProxyMethodUtils.fromMethod(ProcessorProxyInterface.class, "base64Decode"), 
                    base64Property
                )
            );

            assertEquals(property, decoded);
        }

        @Test
        @DisplayName("should wrap exceptions in ProcessingException and propagate.")
        void test4() {
            Base64.Decoder decoder = Base64.getDecoder();
            
            String invalidBase64 = "%%%";
            
            Base64DecodeProcessor base64Decode = new Base64DecodeProcessor(decoder);

            assertThrows(
                ProcessingException.class, 
                () -> base64Decode.process(
                    new ProcessingContext(
                        ProxyMethodUtils.fromMethod(ProcessorProxyInterface.class, "base64Decode"), 
                        invalidBase64
                    )
                )
            );
        }

        private String base64Encode(String property, Base64.Encoder encoder) {
            return new String(encoder.encode(property.getBytes()));
        }
    }
}
