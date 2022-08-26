package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.Ordinals.Ordinal;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.IntegerConverter;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubConverter;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrdinalsTests {
    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(ProxyInterface.class);

    @Nested
    class OrdinalResolverMethod {
        @Test
        @DisplayName("should throw when decorated argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Ordinals.ordinalResolver(1, null)
            );
        }

        @Test
        @DisplayName("should return an ordinal resolver")
        void test2() {
            Resolver ordinalResolver = 
                Ordinals.ordinalResolver(1, new StubResolver());
            assertTrue(Ordinals.isOrdinal(ordinalResolver));
        }
    }

    @Nested
    class OrdinalConverterMethod {
        @Test
        @DisplayName("should throw when decorated argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Ordinals.ordinalConverter(1, null)
            );
        }

        @Test
        @DisplayName("should return an ordinal converter")
        void test2() {
            Converter<?> ordinalConverter = 
                Ordinals.ordinalConverter(-1, new StubConverter<>());
            assertTrue(Ordinals.isOrdinal(ordinalConverter));
        }
    }

    @Nested
    class SortResolversMethod {
        @Test
        @DisplayName("should throw when resolvers list is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Ordinals.sortResolvers(null)
            );
        }

        @Test
        @DisplayName(
            "should sort resolvers based on ordinal " + 
            "(the lower the ordinal, the earlier in the resolver sequence)"
        )
        void test2() {
            StubResolver resolver1 = new StubResolver();
            StubResolver resolver2 = new StubResolver();
            StubResolver resolver3 = new StubResolver();
            
            List<Resolver> sorted = Ordinals.sortResolvers(Arrays.asList(
                Ordinals.ordinalResolver(3, resolver3),
                Ordinals.ordinalResolver(2, resolver2),
                Ordinals.ordinalResolver(1, resolver1)
            ));

            assertEquals(3, sorted.size());
            assertEquals(sorted.get(0), resolver1);
            assertEquals(sorted.get(1), resolver2);
            assertEquals(sorted.get(2), resolver3);
        }

        @Test
        @DisplayName(
            "should put ordinal resolvers earlier in the resolver sequence than " + 
            "resolvers that have no ordinal"
        )
        void test3() {
            StubResolver resolver1 = new StubResolver();
            StubResolver resolver2 = new StubResolver();
            StubResolver resolver3 = new StubResolver();
            
            List<Resolver> sorted = Ordinals.sortResolvers(Arrays.asList(
                resolver3,
                Ordinals.ordinalResolver(2, resolver2),
                Ordinals.ordinalResolver(1, resolver1)
            ));

            assertEquals(3, sorted.size());
            assertEquals(sorted.get(0), resolver1);
            assertEquals(sorted.get(1), resolver2);
            assertEquals(sorted.get(2), resolver3);
        }

        @Test
        @DisplayName("should retain insertion order when ordinal is the same")
        void test4() {
            StubResolver resolver1 = new StubResolver();
            StubResolver resolver2 = new StubResolver();
            StubResolver resolver3 = new StubResolver();
            
            List<Resolver> sorted = Ordinals.sortResolvers(Arrays.asList(
                Ordinals.ordinalResolver(1, resolver1),
                Ordinals.ordinalResolver(1, resolver2),
                Ordinals.ordinalResolver(1, resolver3)
            ));

            assertEquals(3, sorted.size());
            assertEquals(sorted.get(0), resolver1);
            assertEquals(sorted.get(1), resolver2);
            assertEquals(sorted.get(2), resolver3);
        }

        @Test
        @DisplayName("should unwrap ordinal resolvers")
        void test5() {
            StubResolver resolver1 = new StubResolver();
            StubResolver resolver2 = new StubResolver();
            StubResolver resolver3 = new StubResolver();
            
            List<Resolver> sorted = Ordinals.sortResolvers(Arrays.asList(
                Ordinals.ordinalResolver(1, resolver1),
                Ordinals.ordinalResolver(3, resolver3),
                Ordinals.ordinalResolver(2, resolver2)
            ));

            sorted.forEach(r -> assertTrue(r instanceof StubResolver));
        }
    }

    @Nested
    class SortConvertersMethod {
        @Test
        @DisplayName("should throw when converters list is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Ordinals.sortConverters(null)
            );
        }
        
        @Test
        @DisplayName(
            "should sort converters based on ordinal " + 
            "(the lower the ordinal, the earlier in the converter sequence)"
        )
        void test2() {
            StubConverter<?> converter1 = new StubConverter<>();
            StubConverter<?> converter2 = new StubConverter<>();
            StubConverter<?> converter3 = new StubConverter<>();
            
            List<Converter<?>> sorted = Ordinals.sortConverters(Arrays.asList(
                Ordinals.ordinalConverter(3, converter3),
                Ordinals.ordinalConverter(2, converter2),
                Ordinals.ordinalConverter(1, converter1)
            ));

            assertEquals(3, sorted.size());
            assertEquals(sorted.get(0), converter1);
            assertEquals(sorted.get(1), converter2);
            assertEquals(sorted.get(2), converter3);
        }
        
        @Test
        @DisplayName(
            "should put ordinal converters earlier in the converter sequence than " + 
            "converters that have no ordinal"
        )
        void test3() {
            StubConverter<?> converter1 = new StubConverter<>();
            StubConverter<?> converter2 = new StubConverter<>();
            StubConverter<?> converter3 = new StubConverter<>();
            
            List<Converter<?>> sorted = Ordinals.sortConverters(Arrays.asList(
                converter3,
                Ordinals.ordinalConverter(2, converter2),
                Ordinals.ordinalConverter(1, converter1)
            ));

            assertEquals(3, sorted.size());
            assertEquals(sorted.get(0), converter1);
            assertEquals(sorted.get(1), converter2);
            assertEquals(sorted.get(2), converter3);
        }
        
        @Test
        @DisplayName("should retain insertion order when ordinal is the same")
        void test4() {
            StubConverter<?> converter1 = new StubConverter<>();
            StubConverter<?> converter2 = new StubConverter<>();
            StubConverter<?> converter3 = new StubConverter<>();
            
            List<Converter<?>> sorted = Ordinals.sortConverters(Arrays.asList(
                Ordinals.ordinalConverter(1, converter1),
                Ordinals.ordinalConverter(1, converter2),
                Ordinals.ordinalConverter(1, converter3)
            ));

            assertEquals(3, sorted.size());
            assertEquals(sorted.get(0), converter1);
            assertEquals(sorted.get(1), converter2);
            assertEquals(sorted.get(2), converter3);
        }

        @Test
        @DisplayName("should unwrap ordinal converters")
        void test5() {
            StubConverter<?> converter1 = new StubConverter<>();
            StubConverter<?> converter2 = new StubConverter<>();
            StubConverter<?> converter3 = new StubConverter<>();
            
            List<Converter<?>> sorted = Ordinals.sortConverters(Arrays.asList(
                Ordinals.ordinalConverter(1, converter1),
                Ordinals.ordinalConverter(3, converter2),
                Ordinals.ordinalConverter(2, converter3)
            ));

            sorted.forEach(r -> assertTrue(r instanceof StubConverter));
        }
    }

    @Nested
    class CompareOrdinalMethod {
        @Test
        @DisplayName("should throw when first argument is null")
        void test1() {
            StubResolver resolver = new StubResolver();
            assertThrows(
                IllegalArgumentException.class, 
                () -> Ordinals.compareOrdinal(null, resolver)
            );
        }

        @Test
        @DisplayName("should throw when second argument is null")
        void test2() {
            StubConverter<?> converter = new StubConverter<>();
            assertThrows(
                IllegalArgumentException.class, 
                () -> Ordinals.compareOrdinal(converter, null)
            );
        }

        @Test
        @DisplayName(
            "should return value less than 0 when " + 
            "first ordinal has lower value than second ordinal"
        )
        void test3() {
            Ordinal ordinal1 = new Ordinal() {
                @Override
                public int ordinal() { return 1; }
            };
            Ordinal ordinal2 = new Ordinal() {
                @Override
                public int ordinal() { return 2; }
            };
            
            int result = Ordinals.compareOrdinal(ordinal1, ordinal2);

            assertTrue(result < 0);
        }

        @Test
        @DisplayName(
            "should return value greater than 0 when " + 
            "first ordinal has higher value than second ordinal"
        )
        void test4() {
            Ordinal ordinal1 = new Ordinal() {
                @Override
                public int ordinal() { return 1; }
            };
            Ordinal ordinal2 = new Ordinal() {
                @Override
                public int ordinal() { return 2; }
            };
            
            int result = Ordinals.compareOrdinal(ordinal2, ordinal1);

            assertTrue(result > 0);
        }

        @Test
        @DisplayName("should return 0 when both ordinal values are equal")
        void test5() {
            Ordinal ordinal1 = new Ordinal() {
                @Override
                public int ordinal() { return 1; }
            };
            Ordinal ordinal2 = new Ordinal() {
                @Override
                public int ordinal() { return 1; }
            };
            
            int result = Ordinals.compareOrdinal(ordinal1, ordinal2);

            assertEquals(0, result);
        }

        @Test
        @DisplayName(
            "should return value less than 0 when " + 
            "first argument is ordinal but second argument is not"
        )
        void test6() {
            Resolver ordinal = Ordinals.ordinalResolver(1, new StubResolver());
            Resolver nonOrdinal = new StubResolver();
            
            int result = Ordinals.compareOrdinal(ordinal, nonOrdinal);

            assertTrue(result < 0);
        }

        @Test
        @DisplayName(
            "should return value greater than 0 when " + 
            "second argument is ordinal but first argument is not"
        )
        void test7() {
            Resolver ordinal = Ordinals.ordinalResolver(1, new StubResolver());
            Resolver nonOrdinal = new StubResolver();
            
            int result = Ordinals.compareOrdinal(nonOrdinal, ordinal);

            assertTrue(result > 0);
        }

        @Test
        @DisplayName("should return 0 when both arguments are not ordinal")
        void test8() {
            Resolver nonOrdinal1 = new StubResolver();
            Resolver nonOrdinal2 = new StubResolver();
            
            int result = Ordinals.compareOrdinal(nonOrdinal1, nonOrdinal2);

            assertEquals(0, result);
        }
    }

    @Nested
    class IsOrdinalMethod {
        @Test
        @DisplayName("should return true when object is ordinal")
        void test1() {
            assertTrue(Ordinals.isOrdinal(new Ordinal() {
                @Override
                public int ordinal() {
                    return 0;
                }
            }));
        }
        @Test
        @DisplayName("should return false when object is not ordinal")
        void test2() {
            assertFalse(Ordinals.isOrdinal(new StubResolver()));
        }
    }

    @Nested
    class OrdinalResolverTests {
        @Nested
        class ResolveMethod {
            @Test
            @DisplayName("should invoke decorated resolver")
            void test1() {
                StubResolver resolver = new StubResolver();
                Resolver ordinalResolver = 
                    Ordinals.ordinalResolver(1, resolver);

                InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                    ProxyInterface::property,
                    externalizedProperties(ordinalResolver)
                );

                String propertyName = "property";
                
                Optional<String> result = 
                    ordinalResolver.resolve(context, propertyName);

                assertTrue(result.isPresent());
                assertEquals(
                    resolver.resolvedProperties().get(propertyName), 
                    result.get()
                );
            }
        }

        @Nested
        class OrdinalMethod {
            @Test
            @DisplayName("should return ordinal defined in factory method")
            void test1() {
                int ordinal = 1;

                Resolver ordinalResolver =
                    Ordinals.ordinalResolver(ordinal, new StubResolver());

                assertEquals(
                    ordinal, 
                    ((Ordinal)ordinalResolver).ordinal()
                );
            }
        }
    }


    @Nested
    class OrdinalConverterTests {
        @Nested
        class CanConvertMethod {
            @Test
            @DisplayName("should invoke decorated converter")
            void test1() {
                IntegerConverter converter = new IntegerConverter();
                Converter<?> ordinalConverter = 
                    Ordinals.ordinalConverter(1, converter);
                
                assertEquals(
                    converter.canConvertTo(Integer.class),
                    ordinalConverter.canConvertTo(Integer.class)
                );
            }
        }

        @Nested
        class ConvertMethod {
            @Test
            @DisplayName("should invoke decorated converter")
            void test1() {
                IntegerConverter converter = new IntegerConverter();
                Converter<?> ordinalConverter = 
                    Ordinals.ordinalConverter(1, converter);

                InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                    ProxyInterface::intProperty,
                    externalizedProperties(ordinalConverter)
                );
                
                ConversionResult<?> converterResult = ordinalConverter.convert(
                    context, 
                    "1"
                );

                ConversionResult<?> ordinalConverterResult = ordinalConverter.convert(
                    context, 
                    "1"
                );

                assertEquals(converterResult, ordinalConverterResult);
            }
        }

        @Nested
        class OrdinalMethod {
            @Test
            @DisplayName("should return ordinal defined in factory method")
            void test1() {
                int ordinal = 1;

                Converter<?> ordinalConverter =
                    Ordinals.ordinalConverter(ordinal, new StubConverter<>());

                assertEquals(
                    ordinal, 
                    ((Ordinal)ordinalConverter).ordinal()
                );
            }
        }
    }

    private static ExternalizedProperties externalizedProperties(
            Resolver... resolvers
    ) {
        return ExternalizedProperties.builder().resolvers(resolvers).build();
    }

    private static ExternalizedProperties externalizedProperties(
            Converter<?>... converters
    ) {
        return ExternalizedProperties.builder()
            .converters(converters)
            .build();
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();

        @ExternalizedProperty("property.int")
        int intProperty();
    }
}
