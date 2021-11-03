package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverterContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers.IntegerPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;
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

public class InternalResolvedPropertyConverterTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName(
            "should throw when resolved property conversion handlers collection argument is null."
        )
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new InternalResolvedPropertyConverter(
                    (Collection<ResolvedPropertyConversionHandler<?>>)null
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
                () -> new InternalResolvedPropertyConverter(
                    (ResolvedPropertyConversionHandler<?>[])null
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
            InternalResolvedPropertyConverter converter = converter(
                new IntegerPropertyConversionHandler()
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
            InternalResolvedPropertyConverter converter = converter(
                new IntegerPropertyConversionHandler()
            );

            StubExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class, 
                    "intPrimitiveProperty"
                );

            Class<Integer> expectedType = Integer.class;

            ResolvedPropertyConverterContext context = new ResolvedPropertyConverterContext(
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
            InternalResolvedPropertyConverter converter = converter(
                new IntegerPropertyConversionHandler()
            );

            StubExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    EnumProxyInterface.class,
                    "enumProperty"
                );

            // No handler registered to convert to TestEnum.
            Class<?> expectedType = TestEnum.class;

            ResolvedPropertyConverterContext context = new ResolvedPropertyConverterContext(
                propertyMethod, 
                ResolvedProperty.with("enumProperty", TestEnum.ONE.name()), 
                expectedType
            );

            assertThrows(
                ResolvedPropertyConversionException.class, 
                () -> converter.convert(context)
            );
        }

        @Test
        @DisplayName(
            "should wrap and re-throw when handler has thrown an exception."
        )
        public void test4() {
            // Handler that can convert anything but always throws.
            ResolvedPropertyConversionHandler<?> throwingHandler = 
                new ResolvedPropertyConversionHandler<Object>() {

                    @Override
                    public boolean canConvertTo(Class<?> expectedType) {
                        return true;
                    }

                    @Override
                    public Object convert(ResolvedPropertyConversionHandlerContext context) {
                        throw new RuntimeException("Mr. Stark I don't feel so good...");
                    }
                    
                };
            
            InternalResolvedPropertyConverter converter = converter(throwingHandler);

            StubExternalizedPropertyMethodInfo propertyMethod = 
                StubExternalizedPropertyMethodInfo.fromMethod(
                    PrimitiveProxyInterface.class,
                    "intPrimitiveProperty"
                );

            Class<?> expectedType = Integer.class;

            ResolvedPropertyConverterContext context = new ResolvedPropertyConverterContext(
                propertyMethod, 
                ResolvedProperty.with("property.integer.primitive", "1"), 
                expectedType
            );

            assertThrows(
                ResolvedPropertyConversionException.class, 
                () -> converter.convert(context)
            );
        }

        private InternalResolvedPropertyConverter converter(
                ResolvedPropertyConversionHandler<?>... conversionHandlers) {
            return new InternalResolvedPropertyConverter(conversionHandlers);
        }
    }
}
