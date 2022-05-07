package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.conversion.Delimiter;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProxyMethodAdapterTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when method argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new ProxyMethodAdapter(null)
            );
        }
    }

    @Nested
    class DeclaringClassMethod {
        @Test
        @DisplayName("should return method's declaring class.")
        public void test1() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );

            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            assertEquals(proxyInterfaceMethod.getDeclaringClass(), proxyMethod.declaringClass());
        }
    }

    @Nested
    class NameMethod {
        @Test
        @DisplayName("should return method name.")
        public void test1() {
            String methodName = "property";
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                methodName
            );

            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);
            
            assertEquals(methodName, proxyMethod.name());
        }
    }

    @Nested
    class ExternalizedPropertyNameMethod {
        @Test
        @DisplayName("should return name of the property.")
        public void test1() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);
            
            Optional<String> propertyName = proxyMethod.externalizedPropertyName();
            
            assertTrue(propertyName.isPresent());

            // See ProxyInterface.property @ExternalizedProperty annotation value.
            assertEquals("property", propertyName.get());
        }
    }

    @Nested
    class AnnotationsMethod {
        @Test
        @DisplayName(
            "should return all annotations the method is annotated with."
        )
        public void test() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);
        
            Annotation[] annotations = proxyMethod.annotations();
            
            assertTrue(annotations.length == 1);
            assertEquals(ExternalizedProperty.class, annotations[0].annotationType());
        }

        @Test
        @DisplayName(
            "should return empty array when method is not annotated with any annotations."
        )
        public void test2() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::defaultPropertyWithParameterNoAnnotation
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            Annotation[] annotations = proxyMethod.annotations();

            assertTrue(annotations.length == 0);
        }
    }

    @Nested
    class FindAnnotationMethod {
        @Test
        @DisplayName(
            "should return the annotation " + 
            "when method is annotation with the specified annotation class."
        )
        public void test() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);
        
            Optional<ExternalizedProperty> annotation = 
                proxyMethod.findAnnotation(ExternalizedProperty.class);
            
            assertTrue(annotation.isPresent());
        }

        @Test
        @DisplayName(
            "should return an empty Optional " + 
            "when method is not annotated with the specified annotation class."
        )
        public void test2() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            // Method not annotated with @Delimiter.
            Optional<Delimiter> nonExistentAnnotation =
                proxyMethod.findAnnotation(Delimiter.class);
            
            assertFalse(nonExistentAnnotation.isPresent());
        }
    }

    @Nested
    class HasAnnotationMethod {
        @Test
        @DisplayName(
            "should return true when method is annotated with the specified annotation class."
        )
        public void test1() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            assertTrue(
                proxyMethod.hasAnnotation(ExternalizedProperty.class)
            );
        }

        @Test
        @DisplayName(
            "should return false when method is annotated with the specified annotation class."
        )
        public void test2() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            assertFalse(
                proxyMethod.hasAnnotation(Delimiter.class)
            );
        }
    }

    @Nested
    class RawReturnTypeMethod {
        @Test
        @DisplayName("should return method's return type.")
        public void test1() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            Class<?> returnType = proxyMethod.rawReturnType();

            assertEquals(proxyInterfaceMethod.getReturnType(), returnType);
        }
    }

    @Nested
    class ReturnTypeMethod {
        @Test
        @DisplayName("should return method's generic return type.")
        public void test1() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            Type genericReturnType = proxyMethod.returnType();

            assertEquals(proxyInterfaceMethod.getGenericReturnType(), genericReturnType);
        }
    }

    @Nested
    class RawParameterTypesMethod {
        @Test
        @DisplayName("should return method's parameter types.")
        public void test1() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::defaultPropertyWithParameter
            );

            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            Class<?>[] parameterTypes = proxyMethod.rawParameterTypes();

            assertArrayEquals(proxyInterfaceMethod.getParameterTypes(), parameterTypes);
        }
    }

    @Nested
    class ParameterTypesMethod {
        @Test
        @DisplayName("should return method's generic parameter types.")
        public void test1() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::defaultPropertyWithParameter
            );

            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            Type[] parameterTypes = proxyMethod.parameterTypes();

            assertArrayEquals(proxyInterfaceMethod.getGenericParameterTypes(), parameterTypes);
        }
    }

    @Nested
    class HasReturnTypeMethod {
        @Test
        @DisplayName("should return true when method's return type matches.")
        public void test1() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            assertTrue(
                proxyMethod.hasReturnType(String.class)
            );
        }

        @Test
        @DisplayName("should return false when method's return type does not match.")
        public void test2() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            assertFalse(
                proxyMethod.hasReturnType(Integer.class)
            );
        }
    }

    @Nested
    class HasReturnTypeMethodWithTypeArgument {
        @Test
        @DisplayName("should return true when method's return type matches.")
        public void test1() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            Type type = String.class;

            assertTrue(
                proxyMethod.hasReturnType(type)
            );
        }

        @Test
        @DisplayName("should return false when method's return type does not match.")
        public void test2() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            Type type = Integer.class;

            assertFalse(
                proxyMethod.hasReturnType(type)
            );
        }
    }

    @Nested
    class TypeParametersOfReturnTypeMethod {
        @Test
        @DisplayName(
            "should return method return type's generic type parameters " + 
            "when method's return type is a generic type."
        )
        public void test1() {
            Method method = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::optionalProperty
            );
            ProxyMethod proxyMethod = proxyMethod(method);

            Type[] genericTypeParameters = 
                proxyMethod.typeParametersOfReturnType();

            // Optional has <String> generic type parameter
            assertTrue(genericTypeParameters.length > 0);
            assertEquals(String.class, genericTypeParameters[0]);
        }

        @Test
        @DisplayName(
            "should return empty generic type parameter list " + 
            "when method's return type is not a generic type."
        )
        public void test2() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            Type[] genericTypeParameters = 
                proxyMethod.typeParametersOfReturnType();

            // ProxyInterface.property returns a String which is not generic.
            assertTrue(genericTypeParameters.length == 0);
        }
    }

    @Nested
    class TypeParameterOfReturnTypeAtMethod {
        @Test
        @DisplayName(
            "should return method return type's generic type parameter " + 
            "when method's return type is a generic type."
        )
        public void test1() {
            Method method = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::optionalProperty
            );
            ProxyMethod proxyMethod = proxyMethod(method);

            Optional<Type> genericTypeParameter = 
                proxyMethod.typeParameterOfReturnTypeAt(0);

            // Optional has <String> generic type parameter
            assertTrue(genericTypeParameter.isPresent());
            assertEquals(String.class, genericTypeParameter.get());
        }

        @Test
        @DisplayName(
            "should return empty Optional when method's return type " + 
            "is a generic type but requested type parameter index is out of bounds."
        )
        public void test2() {
            Method method = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::optionalProperty
            );
            ProxyMethod proxyMethod = proxyMethod(method);

            // Index out of bounds. 
            Optional<Type> genericTypeParameter = 
                proxyMethod.typeParameterOfReturnTypeAt(99);

            assertFalse(genericTypeParameter.isPresent());
        }

        @Test
        @DisplayName(
            "should return empty Optional" + 
            "when method's return type is not a generic type."
        )
        public void test3() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            Optional<Type> genericTypeParameter = 
                proxyMethod.typeParameterOfReturnTypeAt(0);

            // ProxyInterface.property returns a String which is not generic.
            assertFalse(genericTypeParameter.isPresent());
        }
    }
    
    @Nested
    class IsDefaultInterfaceMethodMethod {
        @Test
        @DisplayName("should return true when method is a default interface method.")
        public void test1() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::defaultPropertyWithParameterNoAnnotation
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            assertTrue(
                proxyMethod.isDefaultInterfaceMethod()
            );
        }

        @Test
        @DisplayName("should return false when method is not a default interface method.")
        public void test2() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            assertFalse(
                proxyMethod.isDefaultInterfaceMethod()
            );
        }
    }
    
    @Nested
    class MethodSignatureStringMethod {
        @Test
        @DisplayName("should return method's signature string.")
        public void test1() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::property
            );

            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            String methodSignature = proxyMethod.methodSignatureString();

            // Since method is non-generic,
            // Method.toGenericString() and Method.toString() returns the same signature.
            assertEquals(proxyInterfaceMethod.toGenericString(), methodSignature);
            assertEquals(proxyInterfaceMethod.toString(), methodSignature);
        }

        @Test
        @DisplayName("should return method's generic signature string.")
        public void test2() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::optionalProperty
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            String methodSignature = proxyMethod.methodSignatureString();

            assertEquals(proxyInterfaceMethod.toGenericString(), methodSignature);
            // Method.toString() does not include generic types.
            assertNotEquals(proxyInterfaceMethod.toString(), methodSignature);
        }
    }

    @Nested
    class ToStringMethod {
        @Test
        @DisplayName("should match methodSignatureString() method.")
        public void test2() {
            Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                ProxyInterface.class, 
                ProxyInterface::optionalProperty
            );
            ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

            String methodSignature = proxyMethod.methodSignatureString();

            assertEquals(methodSignature, proxyMethod.toString());
        }
    }

    private ProxyMethod proxyMethod(Method method) {
        return new ProxyMethodAdapter(method);
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();

        @ExternalizedProperty("generic.property")
        Optional<String> optionalProperty();

        @ExternalizedProperty("default.property")
        default String defaultPropertyWithParameter(String defaultValue) {
            return defaultValue;
        }
        
        default String defaultPropertyWithParameterNoAnnotation(String defaultValue) {
            return defaultValue;
        }
    }
}