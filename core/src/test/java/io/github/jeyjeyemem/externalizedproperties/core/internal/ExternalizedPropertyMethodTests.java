package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.StringVariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.annotations.Delimiter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers.DefaultPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.UnresolvedExternalizedPropertyException;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.SystemPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.JavaPropertiesProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ThrowingProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExternalizedPropertyMethodTests {
    private final ExternalizedPropertyResolver resolver = new SystemPropertyResolver();
    private final StringVariableExpander variableExpander = new InternalStringVariableExpander(resolver);
    private final ResolvedPropertyConverter converter = new InternalResolvedPropertyConverter(
        new DefaultPropertyConversionHandler()
    );
    private final BasicProxyInterface proxyStub = ExternalizedProperties.builder()
        .resolvers(resolver)
        .build()
        .initialize(BasicProxyInterface.class);
    
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when proxy argument is null")
        public void test1() {
            assertThrows(IllegalArgumentException.class, () ->
                new ExternalizedPropertyMethod(
                    null, 
                    getProxyInterfaceMethod(
                        BasicProxyInterface.class, 
                        "property1"
                    ),
                    resolver,
                    converter,
                    variableExpander
                ));
        }

        @Test
        @DisplayName("should throw when method argument is null")
        public void test2() {
            assertThrows(IllegalArgumentException.class, () ->
                new ExternalizedPropertyMethod(
                    proxyStub,
                    null,
                    resolver,
                    converter,
                    variableExpander
                ));
        }

        @Test
        @DisplayName("should throw when externalized property resolver argument is null")
        public void test3() {
            assertThrows(IllegalArgumentException.class, () ->
                new ExternalizedPropertyMethod(
                    proxyStub,
                    getProxyInterfaceMethod(
                        BasicProxyInterface.class, 
                        "property1"
                    ),
                    null,
                    converter,
                    variableExpander
                ));
        }

        @Test
        @DisplayName("should throw when variable expander argument is null")
        public void test4() {
            assertThrows(IllegalArgumentException.class, () ->
                new ExternalizedPropertyMethod(
                    proxyStub,
                    getProxyInterfaceMethod(
                        BasicProxyInterface.class, 
                        "property1"
                    ),
                    resolver,
                    converter,
                    null
                ));
        }

        @Test
        @DisplayName("should throw when resolved property converter argument is null")
        public void test5() {
            assertThrows(IllegalArgumentException.class, () ->
                new ExternalizedPropertyMethod(
                    proxyStub,
                    getProxyInterfaceMethod(
                        BasicProxyInterface.class, 
                        "property1"
                    ),
                    resolver,
                    null,
                    variableExpander
                ));
        }
    }

    @Nested
    class ExternalizedPropertyAnnotationMethod {
        @Test
        @DisplayName("should return @ExternalizedProperty instance when method is annotated")
        public void test1() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            Optional<ExternalizedProperty> annotation = 
                externalizedPropertyMethod.externalizedPropertyAnnotation();
            
            // BasicProxyInterface.property1 is annotated with @ExternalizedProperty
            assertTrue(annotation.isPresent());
        }

        @Test
        @DisplayName("should return an empty Optional when method is not annotated")
        public void test2() {
            Method noExternalizedPropertyAnnotation = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "propertyWithNoAnnotationButWithDefaultValue"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(noExternalizedPropertyAnnotation);

            Optional<ExternalizedProperty> annotation = 
                externalizedPropertyMethod.externalizedPropertyAnnotation();
            
            // BasicProxyInterface.property1 is annotated with @ExternalizedProperty
            assertFalse(annotation.isPresent());
        }
    }

    @Nested
    class PropertyNameMethod {
        @Test
        @DisplayName("should return name of the property")
        public void test1() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);
            
            Optional<String> propertyName = externalizedPropertyMethod.propertyName();
            
            assertTrue(propertyName.isPresent());

            // See BasicProxyInterface.property1 @ExternalizedProperty annotation value.
            assertEquals("property.1", propertyName.get());
        }
    }

    @Nested
    class FindAnnotationMethod {
        @Test
        @DisplayName(
            "should return the annotation " + 
            "when method is annotation with the specified annotation class"
        )
        public void test() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);
            
            Optional<ExternalizedProperty> annotation = 
                externalizedPropertyMethod.findAnnotation(ExternalizedProperty.class);
            
            assertTrue(annotation.isPresent());
        }

        @Test
        @DisplayName(
            "should return an empty Optional " + 
            "when method is not annotated with the specified annotation class"
        )
        public void test2() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            // Method not annotated with @Delimiter.
            Optional<Delimiter> nonExistentAnnotation =
                externalizedPropertyMethod.findAnnotation(Delimiter.class);
            
            assertFalse(nonExistentAnnotation.isPresent());
        }
    }

    @Nested
    class HasAnnotationMethod {
        @Test
        @DisplayName(
            "should return true when method is annotated with the specified annotation class"
        )
        public void test1() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            assertTrue(
                externalizedPropertyMethod.hasAnnotation(ExternalizedProperty.class)
            );
        }

        @Test
        @DisplayName(
            "should return false when method is annotated with the specified annotation class"
        )
        public void test2() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            assertFalse(
                externalizedPropertyMethod.hasAnnotation(Delimiter.class)
            );
        }
    }

    @Nested
    class ReturnTypeMethod {
        @Test
        @DisplayName("should return method's return type")
        public void test1() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            Class<?> returnType = externalizedPropertyMethod.returnType();

            assertEquals(property1Method.getReturnType(), returnType);
        }
    }
    @Nested
    class HasReturnTypeMethod {
        @Test
        @DisplayName("should return true when method's return type matches")
        public void test1() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            assertTrue(
                externalizedPropertyMethod.hasReturnType(String.class)
            );
        }

        @Test
        @DisplayName("should return false when method's return type does not match")
        public void test2() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            assertFalse(
                externalizedPropertyMethod.hasReturnType(Integer.class)
            );
        }
    }
    @Nested
    class GenericReturnTypeParametersMethod {
        @Test
        @DisplayName(
            "should return method return type's generic type parameters " + 
            "when method's return type is a generic type"
        )
        public void test1() {
            Method optionalPropertyMethod = getProxyInterfaceMethod(
                OptionalProxyInterface.class, 
                "optionalProperty"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(optionalPropertyMethod);

            List<Type> genericTypeParameters = 
                externalizedPropertyMethod.genericReturnTypeParameters();

            // Optional has <String> generic type parameter
            assertFalse(genericTypeParameters.isEmpty());
            assertEquals(String.class, genericTypeParameters.get(0));
        }

        @Test
        @DisplayName(
            "should return empty generic type parameter list " + 
            "when method's return type is not a generic type"
        )
        public void test2() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            List<Type> genericTypeParameters = 
                externalizedPropertyMethod.genericReturnTypeParameters();

            // BasicProxyInterface.property1 returns a String which is not generic.
            assertTrue(genericTypeParameters.isEmpty());
        }
    }
    @Nested
    class GenericReturnTypeParameterMethod {
        @Test
        @DisplayName(
            "should return method return type's generic type parameter " + 
            "when method's return type is a generic type"
        )
        public void test1() {
            Method optionalPropertyMethod = getProxyInterfaceMethod(
                OptionalProxyInterface.class, 
                "optionalProperty"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(optionalPropertyMethod);

            Optional<Type> genericTypeParameter = 
                externalizedPropertyMethod.genericReturnTypeParameter(0);

            // Optional has <String> generic type parameter
            assertTrue(genericTypeParameter.isPresent());
            assertEquals(String.class, genericTypeParameter.get());
        }

        @Test
        @DisplayName(
            "should return empty Optional when method's return type " + 
            "is a generic type but requested type parameter index is out of bounds"
        )
        public void test2() {
            Method optionalPropertyMethod = getProxyInterfaceMethod(
                OptionalProxyInterface.class, 
                "optionalProperty"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(optionalPropertyMethod);

            // Index out of bounds. 
            Optional<Type> genericTypeParameter = 
                externalizedPropertyMethod.genericReturnTypeParameter(99);

            assertFalse(genericTypeParameter.isPresent());
        }

        @Test
        @DisplayName(
            "should return empty Optional" + 
            "when method's return type is not a generic type"
        )
        public void test3() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            Optional<Type> genericTypeParameter = 
                externalizedPropertyMethod.genericReturnTypeParameter(0);

            // BasicProxyInterface.property1 returns a String which is not generic.
            assertFalse(genericTypeParameter.isPresent());
        }
    }

    @Nested
    class GenericReturnTypeParameterOrReturnTypeMethod {
        @Test
        @DisplayName(
            "should return method return type's generic type parameter " + 
            "when method's return type is a generic type"
        )
        public void test1() {
            Method optionalPropertyMethod = getProxyInterfaceMethod(
                OptionalProxyInterface.class, 
                "optionalProperty"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(optionalPropertyMethod);

            Type genericTypeParameter = 
                externalizedPropertyMethod.genericReturnTypeParameterOrReturnType(0);

            // Optional has <String> generic type parameter
            assertEquals(String.class, genericTypeParameter);
        }

        @Test
        @DisplayName(
            "should return method's return type " + 
            "when method is a generic type but requested type parameter index is out of bounds"
        )
        public void test2() {
            Method optionalPropertyMethod = getProxyInterfaceMethod(
                OptionalProxyInterface.class, 
                "optionalProperty"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(optionalPropertyMethod);

            // Index out of bounds. 
            // Return method return type instead of generic type parameter.
            Type returnType = 
                externalizedPropertyMethod.genericReturnTypeParameterOrReturnType(99);

            assertEquals(
                optionalPropertyMethod.getReturnType(), 
                returnType
            );
        }

        @Test
        @DisplayName(
            "should return method's return type " + 
            "when method's return type is not a generic type"
        )
        public void test3() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            Type returnType = 
                externalizedPropertyMethod.genericReturnTypeParameterOrReturnType(0);

            // BasicProxyInterface.property1 returns a String which is not generic.
            assertEquals(
                property1Method.getReturnType(),
                returnType
            );
        }
    }

    @Nested
    class IsDefaultInterfaceMethodMethod {
        @Test
        @DisplayName("should return true when method is a default interface method")
        public void test1() {
            Method propertyWithDefaultValue = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "propertyWithDefaultValue"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(propertyWithDefaultValue);

            assertTrue(
                externalizedPropertyMethod.isDefaultInterfaceMethod()
            );
        }

        @Test
        @DisplayName("should return false when method is not a default interface method")
        public void test2() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            assertFalse(
                externalizedPropertyMethod.isDefaultInterfaceMethod()
            );
        }
    }

    
    @Nested
    class MethodSignatureStringMethod {
        @Test
        @DisplayName("should return method's signature string")
        public void test1() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            String methodSignature = externalizedPropertyMethod.methodSignatureString();

            // Since method is non-generic,
            // Method.toGenericString() and Method.toString() returns the same signature.
            assertEquals(property1Method.toGenericString(), methodSignature);
            assertEquals(property1Method.toString(), methodSignature);
        }

        @Test
        @DisplayName("should return method's generic signature string")
        public void test2() {
            Method optionalProperty = getProxyInterfaceMethod(
                OptionalProxyInterface.class, 
                "optionalProperty"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(optionalProperty);

            String methodSignature = externalizedPropertyMethod.methodSignatureString();

            assertEquals(optionalProperty.toGenericString(), methodSignature);
            // Method.toString() does not include generic types.
            assertNotEquals(optionalProperty.toString(), methodSignature);
        }
    }

    @Nested
    class ToString {
        @Test
        @DisplayName("should match methodSignatureString() method")
        public void test2() {
            Method optionalProperty = getProxyInterfaceMethod(
                OptionalProxyInterface.class, 
                "optionalProperty"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(optionalProperty);

            String methodSignature = externalizedPropertyMethod.methodSignatureString();

            assertEquals(methodSignature, externalizedPropertyMethod.toString());
        }
    }

    @Nested
    class InvokeDefaultInterfaceMethodMethod {
        @Test
        @DisplayName("should invoke default interface method")
        public void test1() {
            Method propertyWithDefaultValue = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "propertyWithDefaultValue"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(propertyWithDefaultValue);

            Object value = externalizedPropertyMethod.invokeDefaultInterfaceMethod(new String[0]);

            // See BasicProxyInterface.propertyWithDefaultValue default return value.
            assertEquals("default.value", value);
        }

        @Test
        @DisplayName("should throw when method is not a default interface method")
        public void test2() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            assertThrows(
                IllegalStateException.class, 
                () -> externalizedPropertyMethod.invokeDefaultInterfaceMethod(new String[0])
            );
        }

        @Test
        @DisplayName("should rethrow when default interface method throws an exception")
        public void test3() {
            Method throwingPropertyMethod = getProxyInterfaceMethod(
                ThrowingProxyInterface.class, 
                "throwingProperty"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(throwingPropertyMethod);

            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> externalizedPropertyMethod.invokeDefaultInterfaceMethod(new String[0])
            );
        }

        @Test
        @DisplayName("should receive method arguments")
        public void test4() {
            Method propertyWithDefaultValueParameterMethod = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "propertyWithDefaultValueParameter",
                String.class // Method has one string parameter.
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(propertyWithDefaultValueParameterMethod);

            Object value = externalizedPropertyMethod.invokeDefaultInterfaceMethod(
                new String[] { "my-default-value" }
            );

            assertEquals("my-default-value", value);
        }
    }

    @Nested
    class DetermineDefaultValueMethod {
        @Test
        @DisplayName(
            "should return default interface method invocation result " + 
            "when method is a default interface method"
        )
        public void test1() {
            Method propertyWithDefaultValue = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "propertyWithDefaultValue"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(propertyWithDefaultValue);

            Object value = externalizedPropertyMethod.determineDefaultValue(new String[0]);

            // See BasicProxyInterface.propertyWithDefaultValue default return value.
            assertEquals("default.value", value);
        }

        @Test
        @DisplayName(
            "should return an empty Optional when method has an Optional method return type"
        )
        public void test2() {
            Method optionalPropertyMethod = getProxyInterfaceMethod(
                OptionalProxyInterface.class, 
                "optionalProperty"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(optionalPropertyMethod);

            Object emptyOptional = 
                externalizedPropertyMethod.determineDefaultValue(new String[0]);

            assertEquals(Optional.empty(), emptyOptional);
        }

        @Test
        @DisplayName(
            "should throw when method is not a default interface method " +
            "and does not have an Optional method return type."
        )
        public void test3() {
            Method property1Method = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "property1"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(property1Method);

            assertThrows(
                UnresolvedExternalizedPropertyException.class, 
                () -> externalizedPropertyMethod.determineDefaultValue(new String[0])
            );
        }
    }

    @Nested
    class ResolvePropertyMethod {
        @Test
        @DisplayName("should return value from externalized property resolver")
        public void test1() {
            Method javaVersionMethod = getProxyInterfaceMethod(
                JavaPropertiesProxyInterface.class, 
                "javaVersion"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(javaVersionMethod);

            // property.1 is not in system properties so this should return default value.
            Object resolvedValue = externalizedPropertyMethod.resolveProperty(new String[0]);

            // Since we're using SystemPropertyResolver,
            // resolvedValue must match the java.version system property value.
            assertEquals(System.getProperty("java.version"), resolvedValue);
        }

        @Test
        @DisplayName(
            "should return default value " + 
            "when method is not annotated with @ExternalizedProperty"
        )
        public void test2() {
            Method propertyWithNoAnnotationButWithDefaultValueMethod = 
                getProxyInterfaceMethod(
                    BasicProxyInterface.class, 
                    "propertyWithNoAnnotationButWithDefaultValue"
                );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(propertyWithNoAnnotationButWithDefaultValueMethod);

            Object resolvedValue = externalizedPropertyMethod.resolveProperty(new String[0]);
            Object defaultValue = externalizedPropertyMethod.determineDefaultValue(new String[0]);

            // Resolved value must match default value.
            assertEquals(defaultValue, resolvedValue);
        }

        @Test
        @DisplayName(
            "should return default value " + 
            "when method is annotated with @ExternalizedProperty " + 
            "but property cannot be resolved via the externalized property resolver"
        )
        public void test3() {
            Method propertyWithDefaultValue = getProxyInterfaceMethod(
                BasicProxyInterface.class, 
                "propertyWithDefaultValue"
            );

            ExternalizedPropertyMethod externalizedPropertyMethod = 
                externalizedPropertyMethod(propertyWithDefaultValue);

            // property.1 is not in system properties so this should return default value.
            Object resolvedValue = externalizedPropertyMethod.resolveProperty(new String[0]);
            Object defaultValue = externalizedPropertyMethod.determineDefaultValue(new String[0]);

            // Resolved value must match default value.
            assertEquals(defaultValue, resolvedValue);
        }
    }

    private ExternalizedPropertyMethod externalizedPropertyMethod(Method method) {
        ExternalizedPropertyMethod externalizedPropertyMethod =
            new ExternalizedPropertyMethod(
                proxyStub,
                method,
                resolver,
                converter,
                variableExpander
            );
        return externalizedPropertyMethod;
    }

    private static Method getProxyInterfaceMethod(
            Class<?> proxyInterface, 
            String name, 
            Class<?>... parameterTypes
    ) {
        try {
            return proxyInterface.getDeclaredMethod(name, parameterTypes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to find property method.", e);
        }
    }
}
