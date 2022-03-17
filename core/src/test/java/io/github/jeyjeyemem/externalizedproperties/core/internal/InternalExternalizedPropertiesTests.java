package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.Processor;
import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.jeyjeyemem.externalizedproperties.core.internal.proxy.ExternalizedPropertyInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.VoidReturnTypeProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.variableexpansion.BasicVariableExpander;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InternalExternalizedPropertiesTests {
    // @Nested
    // class ResolvePropertyMethod {
    //     @Test
    //     @DisplayName("should throw when property name argument is null or empty.")
    //     public void test1() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolver((String)null)
    //         );

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolver("")
    //         );
    //     }
        
    //     @Test
    //     @DisplayName("should return resolved property value.")
    //     public void test2() {
    //         // Just return property name.
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         Optional<String> property = externalizedProperties.resolver("test.property");

    //         assertTrue(property.isPresent());
    //         assertEquals("test.property-value", property.get());
    //     }

    //     @Test
    //     @DisplayName("should return empty Optional when property cannot resolved.")
    //     public void test3() {
    //         // Properties not resolved.
    //         StubResolver resolver =
    //             new StubResolver(
    //                 StubResolver.NULL_VALUE_RESOLVER
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         Optional<String> property = externalizedProperties.resolver("test.property");

    //         assertFalse(property.isPresent());
    //     }
    // }

    // @Nested
    // class ResolvePropertyMethodWithProcessorsOverload {
    //     @Test
    //     @DisplayName("should throw when property name argument is null or empty.")
    //     public void test1() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 null,
    //                 Processors.NONE
    //             )
    //         );

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "",
    //                 Processors.NONE
    //             )
    //         );
    //     }
        
    //     @Test
    //     @DisplayName("should throw when target type argument is null.")
    //     public void test2() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 (Processors)null
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should return resolved property value.")
    //     public void test3() {
    //         // Just return property name appended with "-value".
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         Optional<String> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             Processors.NONE
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals("test.property-value", property.get());
    //     }

    //     @Test
    //     @DisplayName("should return empty Optional when property cannot resolved.")
    //     public void test4() {
    //         // Properties not resolved.
    //         StubResolver resolver =
    //             new StubResolver(
    //                 StubResolver.NULL_VALUE_RESOLVER
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         Optional<String> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             Processors.NONE
    //         );

    //         assertFalse(property.isPresent());
    //     }
        
    //     @Test
    //     @DisplayName(
    //         "should apply processors defined in @ProcessorClasses annotation to the property value."
    //     )
    //     public void test5() {
    //         // Always returns base64 encoded property name.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> 
    //                 base64Encode(propertyName)
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(
    //                 resolver,
    //                 Arrays.asList(new Base64Decode()) // Register base64 processor.
    //             );
    //         Optional<?> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             Processors.of(Base64Decode.class)
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals("test.property", property.get());
    //     }

    //     @Test
    //     @DisplayName(
    //         "should throw when processors defined in @ProcessorClasses annotation was not registered."
    //     )
    //     public void test6() {
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         // Base64Decode processor not registered.
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             ProcessingException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 Processors.of(Base64Decode.class)
    //             )
    //         );
    //     }
    // }

    // @Nested
    // class ResolvePropertyMethodWithClassOverload {
    //     @Test
    //     @DisplayName("should throw when property name argument is null or empty.")
    //     public void test1() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 null,
    //                 Integer.class
    //             )
    //         );

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "",
    //                 Integer.class
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when target type argument is null.")
    //     public void test2() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 (Class<?>)null
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should skip conversion when target class is String.")
    //     public void test3() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         Optional<String> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             String.class
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals("test.property-value", property.get());
    //     }

    //     @Test
    //     @DisplayName("should convert property to the target class.")
    //     public void test4() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         Optional<Integer> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             Integer.class
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals(1, property.get());
    //     }

    //     @Test
    //     @DisplayName("should throw when property cannot be converted to the target class.")
    //     public void test5() {
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "invalid_integer");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);
            
    //         assertThrows(
    //             ConversionException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 Integer.class
    //             )
    //         );
    //     }
    // }

    // @Nested
    // class ResolvePropertyMethodWithProcessorsAndClassOverload {
    //     @Test
    //     @DisplayName("should throw when property name argument is null or empty.")
    //     public void test1() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 null,
    //                 Processors.NONE,
    //                 Integer.class
    //             )
    //         );

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "",
    //                 Processors.NONE,
    //                 Integer.class
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when target type argument is null.")
    //     public void test2() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 null,
    //                 Integer.class
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when target type argument is null.")
    //     public void test3() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 Processors.NONE,
    //                 (Class<?>)null
    //             )
    //         );
    //     }
        
    //     @Test
    //     @DisplayName(
    //         "should apply processors defined in Processors and skip conversion " + 
    //         "when target type is String."
    //     )
    //     public void test4() {
    //         // Always returns base64 encoded property name.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> 
    //                 base64Encode(propertyName)
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(
    //                 resolver,
    //                 Arrays.asList(new Base64Decode()) // Register base64 processor.
    //             );
            
    //         Optional<?> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             Processors.of(Base64Decode.class),
    //             String.class
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals("test.property", property.get());
    //     }
        
    //     @Test
    //     @DisplayName(
    //         "should apply processors defined in Processors and convert to target type."
    //     )
    //     public void test5() {
    //         // Always returns base64 encoded "1".
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> 
    //                 base64Encode("1")
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(
    //                 resolver,
    //                 Arrays.asList(new Base64Decode()) // Register base64 processor.
    //             );
            
    //         Optional<?> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             Processors.of(Base64Decode.class),
    //             Integer.class
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals(1, (Integer)property.get());
    //     }

    //     @Test
    //     @DisplayName(
    //         "should throw when processors defined in Processors was not registered."
    //     )
    //     public void test6() {
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         // Base64Decode processor not registered.
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             ProcessingException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 Processors.of(Base64Decode.class),
    //                 Integer.class
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when property cannot be converted to the target class")
    //     public void test7() {
    //         // Always returns base64 encoded "1".
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> 
    //                 base64Encode("invalid_integer")
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(
    //                 resolver,
    //                 Arrays.asList(new Base64Decode()) // Register base64 processor.
    //             );
            
    //         assertThrows(
    //             ConversionException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 Processors.of(Base64Decode.class),
    //                 Integer.class
    //             )
    //         );
    //     }
    // }

    // @Nested
    // class ResolvePropertyMethodWithTypeReferenceOverload {
    //     @Test
    //     @DisplayName("should throw when property name argument is null or empty.")
    //     public void test1() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 null,
    //                 new TypeReference<List<Integer>>(){}
    //             )
    //         );

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "",
    //                 new TypeReference<List<Integer>>(){}
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when target type argument is null.")
    //     public void test2() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 (TypeReference<?>)null
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should skip conversion when target type is String.")
    //     public void test3() {
    //         // Just return property name appended with "-value".
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         Optional<String> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             new TypeReference<String>(){}
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals("test.property-value", property.get());
    //     }

    //     @Test
    //     @DisplayName("should convert property to the target type reference")
    //     public void test4() {
    //         // Always returns 1,2,3.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1,2,3");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         Optional<List<Integer>> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             new TypeReference<List<Integer>>(){}
    //         );

    //         assertTrue(property.isPresent());
    //         assertIterableEquals(
    //             Arrays.asList(
    //                 1, 2, 3
    //             ), 
    //             property.get()
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when property cannot be converted to the target type reference")
    //     public void test5() {
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "invalid_integer");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);
            
    //         assertThrows(
    //             ConversionException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 new TypeReference<Integer>(){}
    //             )
    //         );
    //     }
    // }

    // @Nested
    // class ResolvePropertyMethodWithProcessorsAndTypeReferenceOverload {
    //     @Test
    //     @DisplayName("should throw when property name argument is null or empty.")
    //     public void test1() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 null,
    //                 Processors.NONE,
    //                 new TypeReference<List<Integer>>(){}
    //             )
    //         );

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "",
    //                 Processors.NONE,
    //                 new TypeReference<List<Integer>>(){}
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when processors argument is null.")
    //     public void test2() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 (Processors)null,
    //                 new TypeReference<List<Integer>>(){}
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when target type argument is null.")
    //     public void test3() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 Processors.NONE,
    //                 (TypeReference<?>)null
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName(
    //         "should apply processors defined in Processors and skip conversion " + 
    //         "when target type is String."
    //     )
    //     public void test4() {
    //         // Always returns base64 encoded property name.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> 
    //                 base64Encode(propertyName)
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(
    //                 resolver,
    //                 Arrays.asList(new Base64Decode()) // Register base64 processor.
    //             );

    //         Optional<?> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             Processors.of(Base64Decode.class),
    //             (Type)String.class
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals("test.property", property.get());
    //     }
        
    //     @Test
    //     @DisplayName(
    //         "should apply processors defined in Processors and convert to target type."
    //     )
    //     public void test5() {
    //         // Always returns base64 encoded "1".
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> 
    //                 base64Encode("1")
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(
    //                 resolver,
    //                 Arrays.asList(new Base64Decode()) // Register base64 processor.
    //             );
            
    //         Optional<?> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             Processors.of(Base64Decode.class),
    //             new TypeReference<Integer>(){}
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals(1, (Integer)property.get());
    //     }

    //     @Test
    //     @DisplayName(
    //         "should throw when processors defined in Processors was not registered."
    //     )
    //     public void test6() {
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         // Base64Decode processor not registered.
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             ProcessingException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 Processors.of(Base64Decode.class),
    //                 new TypeReference<Integer>(){}
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when property cannot be converted to the target type reference")
    //     public void test7() {
    //         // Always returns base64 encoded "1".
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> 
    //                 base64Encode("invalid_integer")
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(
    //                 resolver,
    //                 Arrays.asList(new Base64Decode()) // Register base64 processor.
    //             );
            
    //         assertThrows(
    //             ConversionException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 Processors.of(Base64Decode.class),
    //                 new TypeReference<Integer>(){}
    //             )
    //         );
    //     }
    // }

    // @Nested
    // class ResolvePropertyMethodWithTypeOverload {
    //     @Test
    //     @DisplayName("should throw when property name argument is null or empty")
    //     public void test1() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 null,
    //                 (Type)Integer.class
    //             )
    //         );

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "",
    //                 (Type)Integer.class
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when target type argument is null")
    //     public void test2() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 (Type)null
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should skip conversion when target class is String.")
    //     public void test3() {
    //         // Just return property name appended with "-value".
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         Optional<?> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             (Type)String.class
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals("test.property-value", property.get());
    //     }

    //     @Test
    //     @DisplayName("should convert property to the target type")
    //     public void test4() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         Optional<?> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             (Type)Integer.class
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals(1, (Integer)property.get());
    //     }

    //     @Test
    //     @DisplayName("should throw when property cannot be converted to the target type")
    //     public void test5() {
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "invalid_integer");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);
            
    //         assertThrows(
    //             ConversionException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 (Type)Integer.class
    //             )
    //         );
    //     }
    // }

    // @Nested
    // class ResolvePropertyMethodWithProcessorsAndTypeOverload {
    //     @Test
    //     @DisplayName("should throw when property name argument is null or empty")
    //     public void test1() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 null,
    //                 Processors.NONE,
    //                 (Type)Integer.class
    //             )
    //         );

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "",
    //                 Processors.NONE,
    //                 (Type)Integer.class
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when processors argument is null")
    //     public void test2() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 (Processors)null,
    //                 (Type)Integer.class
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when target type argument is null")
    //     public void test3() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 Processors.NONE,
    //                 (Type)null
    //             )
    //         );
    //     }
        
    //     @Test
    //     @DisplayName(
    //         "should apply processors defined in Processors and skip conversion " + 
    //         "when target type is String."
    //     )
    //     public void test4() {
    //         // Always returns base64 encoded property name.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> 
    //                 base64Encode(propertyName)
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(
    //                 resolver,
    //                 Arrays.asList(new Base64Decode()) // Register base64 processor.
    //             );
            
    //         Optional<?> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             Processors.of(Base64Decode.class),
    //             (Type)String.class
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals("test.property", property.get());
    //     }
        
    //     @Test
    //     @DisplayName(
    //         "should apply processors defined in Processors and convert to target type."
    //     )
    //     public void test5() {
    //         // Always returns base64 encoded "1".
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> 
    //                 base64Encode("1")
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(
    //                 resolver,
    //                 Arrays.asList(new Base64Decode()) // Register base64 processor.
    //             );
            
    //         Optional<?> property = externalizedProperties.resolveProperty(
    //             "test.property",
    //             Processors.of(Base64Decode.class),
    //             (Type)Integer.class
    //         );

    //         assertTrue(property.isPresent());
    //         assertEquals(1, (Integer)property.get());
    //     }

    //     @Test
    //     @DisplayName(
    //         "should throw when processors defined in Processors was not registered."
    //     )
    //     public void test6() {
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         // Base64Decode processor not registered.
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             ProcessingException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 Processors.of(Base64Decode.class),
    //                 (Type)Integer.class
    //             )
    //         );
    //     }

    //     @Test
    //     @DisplayName("should throw when property cannot be converted to the target type reference")
    //     public void test7() {
    //         // Always returns base64 encoded "1".
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> 
    //                 base64Encode("invalid_integer")
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(
    //                 resolver,
    //                 Arrays.asList(new Base64Decode()) // Register base64 processor.
    //             );
            
    //         assertThrows(
    //             ConversionException.class, 
    //             () -> externalizedProperties.resolveProperty(
    //                 "test.property",
    //                 Processors.of(Base64Decode.class),
    //                 (Type)Integer.class
    //             )
    //         );
    //     }
    // }

    // @Nested
    // class ResolvePropertyMethodWithProxyMethodInfoOverload {
    //     @Test
    //     @DisplayName("should throw when proxy method info argument is null.")
    //     public void test1() {
    //         // Just return property name.
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolver((ProxyMethodInfo)null)
    //         );
    //     }

    //     @Test
    //     @DisplayName("should return resolved property value.")
    //     public void test2() {
    //         // Just return property name.
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         StubProxyMethodInfo proxyMethod = 
    //             StubProxyMethodInfo.fromMethod(BasicProxyInterface.class, "property");

    //         Optional<?> property = externalizedProperties.resolver(proxyMethod);

    //         assertTrue(property.isPresent());
    //         assertEquals("property-value", property.get());
    //     }

    //     @Test
    //     @DisplayName("should return empty Optional when property cannot resolved.")
    //     public void test3() {
    //         // Properties not resolved.
    //         StubResolver resolver =
    //             new StubResolver(
    //                 StubResolver.NULL_VALUE_RESOLVER
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         StubProxyMethodInfo proxyMethod = 
    //             StubProxyMethodInfo.fromMethod(BasicProxyInterface.class, "property");

    //         Optional<?> property = externalizedProperties.resolver(proxyMethod);

    //         assertFalse(property.isPresent());
    //     }

    //     @Test
    //     @DisplayName(
    //         "should convert resolved property value according to proxy method's return type."
    //     )
    //     public void test4() {
    //         // Always returns 1.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> "1");
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         StubProxyMethodInfo proxyMethod = StubProxyMethodInfo.fromMethod(
    //             PrimitiveProxyInterface.class, 
    //             "intPrimitiveProperty"
    //         );

    //         Optional<?> property = externalizedProperties.resolver(proxyMethod);

    //         assertTrue(property.isPresent());
    //         assertEquals(1, (Integer)property.get());
    //     }

    //     @Test
    //     @DisplayName(
    //         "should apply processors defined in @ProcessorClasses annotation " +
    //         "to the property value and convert according to proxy method's return type."
    //     )
    //     public void test5() {
    //         // Always base64 encoded "1".
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> 
    //                 base64Encode("1")
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(
    //                 resolver,
    //                 Arrays.asList(new Base64Decode()) // Register base64 processor.
    //             );

    //         StubProxyMethodInfo proxyMethod = StubProxyMethodInfo.fromMethod(
    //             ProcessorProxyInterface.class, 
    //             "base64DecodeInt"
    //         );

    //         Optional<?> property = externalizedProperties.resolver(proxyMethod);

    //         assertTrue(property.isPresent());
    //         assertEquals(1, (Integer)property.get());
    //     }

    //     @Test
    //     @DisplayName(
    //         "should apply processors defined in @ProcessorClasses annotation to the property value."
    //     )
    //     public void test6() {
    //         // Always base64 encoded property name.
    //         StubResolver resolver =
    //             new StubResolver(propertyName -> 
    //                 base64Encode(propertyName)
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(
    //                 resolver,
    //                 Arrays.asList(new Base64Decode()) // Register base64 processor.
    //             );

    //         StubProxyMethodInfo proxyMethod = StubProxyMethodInfo.fromMethod(
    //             ProcessorProxyInterface.class, 
    //             "base64Decode"
    //         );

    //         Optional<?> property = externalizedProperties.resolver(proxyMethod);

    //         assertTrue(property.isPresent());
    //         assertEquals("test.base64Decode", property.get());
    //     }

    //     @Test
    //     @DisplayName(
    //         "should throw when proxy method info does not have @ExternalizedProperty annotation."
    //     )
    //     public void test7() {
    //         // Just return property name.
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         StubProxyMethodInfo proxyMethod = StubProxyMethodInfo.fromMethod(
    //             BasicProxyInterface.class, 
    //             "propertyWithNoAnnotationAndNoDefaultValue"
    //         );

    //         assertThrows(
    //             IllegalArgumentException.class, 
    //             () -> externalizedProperties.resolver(proxyMethod)
    //         );
    //     }

    //     @Test
    //     @DisplayName(
    //         "should throw when processors defined in @ProcessorClasses annotation was not registered."
    //     )
    //     public void test8() {
    //         StubResolver resolver =
    //             new StubResolver();
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         // Method is annotation with @ProcessorClasses(Base64Decode.class)
    //         // Base64Decode is not registered to InternalExternalizedProperties.
    //         StubProxyMethodInfo proxyMethod = StubProxyMethodInfo.fromMethod(
    //             ProcessorProxyInterface.class, 
    //             "base64Decode"
    //         );

    //         assertThrows(
    //             ProcessingException.class, 
    //             () -> externalizedProperties.resolver(proxyMethod)
    //         );
    //     }
    // }

    // @Nested
    // class ExpandVariablesMethod {
    //     @Test
    //     @DisplayName("should expand variables in source string")
    //     public void test1() {
    //         // Always returns propertyName + .variable.
    //         StubResolver resolver =
    //             new StubResolver(
    //                 propertyName -> propertyName + ".variable"
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         String result = externalizedProperties.expandVariables("test.property.${myvar}");

    //         assertNotNull(result);
    //         assertEquals("test.property.myvar.variable", result);
    //     }

    //     @Test
    //     @DisplayName(
    //         "should throw when requested variable value in source string cannot be resolved"
    //     )
    //     public void test2() {
    //         // Do not resolve any property.
    //         StubResolver resolver =
    //             new StubResolver(
    //                 StubResolver.NULL_VALUE_RESOLVER
    //             );
            
    //         InternalExternalizedProperties externalizedProperties = 
    //             internalExternalizedProperties(resolver);

    //         assertThrows(
    //             VariableExpansionException.class, 
    //             () -> externalizedProperties.expandVariables(
    //                 "test.property.${non.existing.var}"
    //             )
    //         );
    //     }
    // }

    @Nested
    class ProxyMethod {
        @Test
        @DisplayName("should throw when proxy interface argument is null")
        public void test1() {
            // Do not resolve any property.
            StubResolver resolver =
                new StubResolver(
                    StubResolver.NULL_VALUE_RESOLVER
                );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(null) 
            );
        }

        @Test
        @DisplayName("should create a proxy")
        public void test2() {
            // Do not resolve any property.
            StubResolver resolver =
                new StubResolver(
                    StubResolver.NULL_VALUE_RESOLVER
                );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            BasicProxyInterface proxy = externalizedProperties.proxy(BasicProxyInterface.class);

            assertNotNull(proxy);
            assertTrue(proxy instanceof Proxy);
        }
    }

    @Nested
    class ProxyMethodWithClassLoaderOverload {
        @Test
        @DisplayName("should throw when proxy interface argument is null")
        public void test1() {
            StubResolver resolver =
                new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(null, getClass().getClassLoader()) 
            );
        }

        @Test
        @DisplayName("should throw when class loader argument is null")
        public void test2() {
            StubResolver resolver =
                new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(BasicProxyInterface.class, null)
            );
        }

        @Test
        @DisplayName("should throw when proxy interface argument is not an interface")
        public void test3() {
            StubResolver resolver =
                new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(InternalExternalizedPropertiesTests.class)
            );
        }

        @Test
        @DisplayName("should throw when proxy interface contains void-returning methods")
        public void test4() {
            StubResolver resolver =
                new StubResolver();
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.proxy(VoidReturnTypeProxyInterface.class)
            );
        }

        @Test
        @DisplayName("should create a proxy")
        public void test5() {
            // Do not resolve any property.
            StubResolver resolver =
                new StubResolver(
                    StubResolver.NULL_VALUE_RESOLVER
                );
            
            InternalExternalizedProperties externalizedProperties = 
                internalExternalizedProperties(resolver);

            BasicProxyInterface proxy = externalizedProperties.proxy(
                BasicProxyInterface.class,
                BasicProxyInterface.class.getClassLoader()
            );

            assertNotNull(proxy);
            assertTrue(proxy instanceof Proxy);
        }
    }

    private InternalExternalizedProperties internalExternalizedProperties(
            Resolver resolver
    ) {
        return internalExternalizedProperties(resolver, Collections.emptyList());
    }

    private InternalExternalizedProperties internalExternalizedProperties(
            Resolver resolver,
            Collection<Processor> processors
    ) {
        return new InternalExternalizedProperties(
            resolver, 
            new RootProcessor(
                processors
            ),
            new RootConverter(
                new DefaultConverter()
            ),
            new BasicVariableExpander(resolver),
            (ep, proxyInterface) -> 
                new ExternalizedPropertyInvocationHandler(ep)
        );
    }

    // private String base64Encode(String property) {
    //     return new String(Base64.getEncoder().encode(property.getBytes()));
    // }
}
