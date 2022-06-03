package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.ResolverFacade;
import io.github.joeljeremy7.externalizedproperties.core.conversion.Delimiter;
import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.ProxyMethodFactory.ProxyMethodAdapter;
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

public class ProxyMethodFactoryTests {
    private static final ExternalizedProperties EXTERNALIZED_PROPERTIES = 
        ExternalizedProperties.builder().defaults().build();
    
    @Nested
    class ProxyMethodAdapterTests {
        @Nested
        class Constructor {
            @Test
            @DisplayName("should throw when externalized properties argument is null")
            void test1() {
                Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                    ProxyInterface.class, 
                    ProxyInterface::property
                );

                assertThrows(
                    IllegalArgumentException.class, 
                    () -> new ProxyMethodAdapter(null, proxyInterfaceMethod)
                );
            }

            @Test
            @DisplayName("should throw when method argument is null")
            void test2() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> new ProxyMethodAdapter(EXTERNALIZED_PROPERTIES, null)
                );
            }
        }

        @Nested
        class ExternalizedPropertiesMethod {
            @Test
            @DisplayName("should return externalized properties instance.")
            void test1() {
                Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                    ProxyInterface.class, 
                    ProxyInterface::property
                );

                ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

                assertEquals(EXTERNALIZED_PROPERTIES, proxyMethod.externalizedProperties());
            }
        }

        @Nested
        class DeclaringClassMethod {
            @Test
            @DisplayName("should return method's declaring class.")
            void test1() {
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
            void test1() {
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
        class AnnotationsMethod {
            @Test
            @DisplayName(
                "should return all annotations the method is annotated with."
            )
            void test() {
                Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                    ProxyInterface.class, 
                    ProxyInterface::property
                );
                ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);
            
                Annotation[] annotations = proxyMethod.annotations();
                
                assertEquals(1, annotations.length);
                assertEquals(ExternalizedProperty.class, annotations[0].annotationType());
            }

            @Test
            @DisplayName(
                "should return empty array when method is not annotated with any annotations."
            )
            void test2() {
                Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                    ProxyInterface.class, 
                    ProxyInterface::defaultPropertyWithParameterNoAnnotation
                );
                ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

                Annotation[] annotations = proxyMethod.annotations();

                assertEquals(0, annotations.length);
            }
        }

        @Nested
        class FindAnnotationMethod {
            @Test
            @DisplayName(
                "should return the annotation " + 
                "when method is annotation with the specified annotation class."
            )
            void test() {
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
            void test2() {
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
            void test1() {
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
            void test2() {
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
            void test1() {
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
            void test1() {
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
            void test1() {
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
        class RawParameterTypeAtMethod {
            @Test
            @DisplayName("should return method parameter's raw type at specified index.")
            void test1() {
                Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                    ProxyInterface.class, 
                    ProxyInterface::defaultPropertyWithParameter
                );

                ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

                Optional<Class<?>> parameterType = proxyMethod.rawParameterTypeAt(0);

                assertTrue(parameterType.isPresent());
                assertEquals(String.class, parameterType.get());
            }

            @Test
            @DisplayName(
                "should return empty Optional if there are no parameter at the specified index."
            )
            void test2() {
                Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                    ProxyInterface.class, 
                    ProxyInterface::defaultPropertyWithParameter
                );

                ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

                Optional<Class<?>> parameterType = proxyMethod.rawParameterTypeAt(1);

                assertFalse(parameterType.isPresent());
            }
        }

        @Nested
        class ParameterTypesMethod {
            @Test
            @DisplayName("should return method's generic parameter types.")
            void test1() {
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
        class ParameterTypeAtMethod {
            @Test
            @DisplayName("should return method parameter's type at specified index.")
            void test1() {
                Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                    ProxyInterface.class, 
                    ProxyInterface::defaultPropertyWithParameter
                );

                ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

                Optional<Type> parameterType = proxyMethod.parameterTypeAt(0);

                assertTrue(parameterType.isPresent());
                assertEquals((Type)String.class, parameterType.get());
            }

            @Test
            @DisplayName(
                "should return empty Optional if there are no parameter at the specified index."
            )
            void test2() {
                Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                    ProxyInterface.class, 
                    ProxyInterface::defaultPropertyWithParameter
                );

                ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

                Optional<Type> parameterType = proxyMethod.parameterTypeAt(1);

                assertFalse(parameterType.isPresent());
            }
        }

        @Nested
        class HasReturnTypeMethod {
            @Test
            @DisplayName("should return true when method's return type matches.")
            void test1() {
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
            void test2() {
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
            void test1() {
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
            void test2() {
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
            void test1() {
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
            void test2() {
                Method proxyInterfaceMethod = ProxyMethodUtils.getMethod(
                    ProxyInterface.class, 
                    ProxyInterface::property
                );
                ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

                Type[] genericTypeParameters = 
                    proxyMethod.typeParametersOfReturnType();

                // ProxyInterface.property returns a String which is not generic.
                assertEquals(0, genericTypeParameters.length);
            }
        }

        @Nested
        class TypeParameterOfReturnTypeAtMethod {
            @Test
            @DisplayName(
                "should return method return type's generic type parameter " + 
                "when method's return type is a generic type."
            )
            void test1() {
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
            void test2() {
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
            void test3() {
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
            void test1() {
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
            void test2() {
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
            void test1() {
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
            void test2() {
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
            void test2() {
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
            return new ProxyMethodAdapter(EXTERNALIZED_PROPERTIES, method);
        }
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();

        @ExternalizedProperty("generic.property")
        Optional<String> optionalProperty();

        @ExternalizedProperty("default.property")
        default String defaultPropertyWithParameter(String defaultValue) {
            return defaultValue;
        }
        
        @ResolverFacade
        String resolve(String propertyName);
        
        default String defaultPropertyWithParameterNoAnnotation(String defaultValue) {
            return defaultValue;
        }
    }
}
