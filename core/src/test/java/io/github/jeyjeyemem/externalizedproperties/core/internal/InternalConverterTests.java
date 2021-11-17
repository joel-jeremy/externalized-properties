package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers.PrimitiveConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnumProxyInterface.TestEnum;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InternalConverterTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName(
            "should throw when resolved property conversion handlers collection argument is null."
        )
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new InternalConverter(
                    (Collection<ConversionHandler<?>>)null
                )  
            );
        }

        @Test
        @DisplayName(
            "should throw when resolved property conversion handlers varargs argument is null."
        )
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new InternalConverter(
                    (ConversionHandler<?>[])null
                )  
            );
        }
    }

    @Nested
    class ConvertMethod {
        @Test
        @DisplayName(
            "should throw when resolved property converter context argument is null."
        )
        public void test1() {
            InternalConverter converter = converter(
                new PrimitiveConversionHandler()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> converter.convert(null)
            );
        }

        @Test
        @DisplayName(
            "should correctly convert to expected type."
        )
        public void test2() {
            InternalConverter converter = converter(
                new PrimitiveConversionHandler()
            );

            StubExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class, 
                    "intPrimitiveProperty"
                );

            Class<Integer> expectedType = Integer.class;

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethod, 
                    ResolvedProperty.with("property.integer.primitive", "1"), 
                    expectedType
                );

            Object convertedValue = converter.convert(context);

            assertNotNull(convertedValue);
            assertEquals(expectedType, convertedValue.getClass());
            assertEquals(1, convertedValue);
        }

        @Test
        @DisplayName(
            "should throw when there is no handler that can convert to expected type."
        )
        public void test3() {
            InternalConverter converter = converter(
                new PrimitiveConversionHandler()
            );

            StubExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );

            // No handler registered to convert to TestEnum.
            Class<?> expectedType = TestEnum.class;

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethod, 
                    ResolvedProperty.with("enumProperty", TestEnum.ONE.name()), 
                    expectedType
                );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(context)
            );
        }

        @Test
        @DisplayName(
            "should wrap and re-throw when handler has thrown an exception."
        )
        public void test4() {
            // Handler that can convert anything but always throws.
            ConversionHandler<?> throwingHandler = 
                new ConversionHandler<Object>() {

                    @Override
                    public boolean canConvertTo(Class<?> expectedType) {
                        return true;
                    }

                    @Override
                    public Object convert(PropertyMethodConversionContext context) {
                        throw new RuntimeException("Mr. Stark I don't feel so good...");
                    }

                    @Override
                    public Object convert(ConversionContext context) {
                        throw new RuntimeException("Mr. Stark I don't feel so good...");
                    }
                };
            
            InternalConverter converter = converter(throwingHandler);

            StubExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );

            Class<?> expectedType = Integer.class;

            PropertyMethodConversionContext context = 
                new PropertyMethodConversionContext(
                    converter,
                    propertyMethod, 
                    ResolvedProperty.with("property.integer.primitive", "1"), 
                    expectedType
                );

            assertThrows(
                ConversionException.class, 
                () -> converter.convert(context)
            );
        }

        private InternalConverter converter(
                ConversionHandler<?>... conversionHandlers) {
            return new InternalConverter(conversionHandlers);
        }
    }
}
