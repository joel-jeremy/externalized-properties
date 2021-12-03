package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.TypeReference;
import io.github.jeyjeyemem.externalizedproperties.core.internal.InternalConverter;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PropertyMethodConversionContextTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when converter argument is null")
        public void test1() {
            StubExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );

            assertThrows(
                IllegalArgumentException.class, 
                () -> new PropertyMethodConversionContext(
                    null,
                    propertyMethod, 
                    "value", 
                    propertyMethod.genericReturnType()
                )
            );
        }

        @Test
        @DisplayName("should throw when externalized property method info argument is null")
        public void test2() {
            Converter converter = new InternalConverter();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new PropertyMethodConversionContext(
                    converter,
                    null, 
                    "value", 
                    Optional.class
                )
            );
        }

        @Test
        @DisplayName("should throw when resolved property argument is null")
        public void test3() {
            Converter converter = new InternalConverter();

            StubExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> new PropertyMethodConversionContext(
                    converter,
                    propertyMethod, 
                    null, 
                    propertyMethod.genericReturnType()
                )
            );
        }

        @Test
        @DisplayName("should throw when expected type argument is null")
        public void test5() {
            Converter converter = new InternalConverter();
            StubExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> new PropertyMethodConversionContext(
                    converter,
                    propertyMethod, 
                    "value", 
                    null
                )
            );
        }

        @Test
        @DisplayName(
            "should use property method's generic return type " + 
            "when expected type argument is not set."
        )
        public void test6() {
            Converter converter = new InternalConverter();
            StubExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );
            
            PropertyMethodConversionContext context =
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethod, 
                    "value"
                );

            assertEquals(propertyMethod.genericReturnType(), context.expectedType());
            assertArrayEquals(
                propertyMethod.returnTypeGenericTypeParameters(), 
                context.expectedTypeGenericTypeParameters()
            );
        }

        @Test
        @DisplayName(
            "should allow expected type to be different from property method's return type."
        )
        public void test7() {
            Converter converter = new InternalConverter();
            StubExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );
            
            PropertyMethodConversionContext context =
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethod, 
                    "value",
                    new TypeReference<List<String>>(){}.type()
                );

            assertNotEquals(propertyMethod.returnType(), context.expectedType());
        }

        @Test
        @DisplayName(
            "should allow generic type parameters of expected type to be different " + 
            "from property method's generic return type generic type parameters."
        )
        public void test10() {
            Converter converter = new InternalConverter();
            StubExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    OptionalProxyInterface.class, 
                    "optionalProperty"
                );
            
            PropertyMethodConversionContext context =
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethod, 
                    "value",
                    new TypeReference<List<Integer>>(){}.type()
                );

            assertArrayEquals(
                new Class<?>[] { Integer.class }, 
                context.expectedTypeGenericTypeParameters()
            );
        }
    }
}
