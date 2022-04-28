package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeUtilitiesTests {
    @Nested
    class GetRawTypeMethod {
        @Test
        @DisplayName("should throw when type argument is null.")
        public void nullTest() {
            assertThrows(
                IllegalArgumentException.class,
                () -> TypeUtilities.getRawType(null)
            );
        }

        @Test
        @DisplayName("should return same class when type is a class.")
        public void classTest1() {
            assertEquals(
                String.class, 
                TypeUtilities.getRawType(String.class)
            );
        }

        @Test
        @DisplayName("should return raw type when type is a parameterized type.")
        public void parameterizedTest1() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "parameterizedTypeReturnType"
                );
                
            Type genericReturnType = proxyMethod.returnType();

            assertTrue(genericReturnType instanceof ParameterizedType);
            assertEquals(
                List.class, 
                TypeUtilities.getRawType(genericReturnType)
            );
        }

        @Test
        @DisplayName(
            "should return Object array class when type is a generic array type " + 
            "with type variable that has no extends declaration."
        )
        public void genericArrayTypeTest1() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "genericArrayTypeReturnTypeWithTypeVariable"
                );
                
            Type genericReturnType = proxyMethod.returnType();

            assertTrue(genericReturnType instanceof GenericArrayType);
            assertEquals(
                Object[].class, 
                TypeUtilities.getRawType(genericReturnType)
            );
        }

        @Test
        @DisplayName(
            "should return correct bound when type is a generic array type " + 
            "with type variable that has an extends declaration."
        )
        public void genericArrayTypeTest2() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "genericArrayTypeReturnTypeWithTypeVariableExtends"
                );
                
            Type genericReturnType = proxyMethod.returnType();

            assertTrue(genericReturnType instanceof GenericArrayType);
            assertEquals(
                List[].class, 
                TypeUtilities.getRawType(genericReturnType)
            );
        }

        @Test
        @DisplayName(
            "should return Object class when type is a type variable " + 
            "that has no extends declaration."
        )
        public void typeVariableTest1() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "typeVariableReturnType"
                );
                
            Type genericReturnType = proxyMethod.returnType();

            assertTrue(genericReturnType instanceof TypeVariable<?>);
            assertEquals(
                Object.class, 
                TypeUtilities.getRawType(genericReturnType)
            );
        }

        @Test
        @DisplayName(
            "should return correct bound when type is a type variable " + 
            "that has an extends declaration."
        )
        public void typeVariableTest2() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "typeVariableReturnTypeExtends"
                );
                
            Type genericReturnType = proxyMethod.returnType();

            assertTrue(genericReturnType instanceof TypeVariable<?>);
            assertEquals(
                List.class, 
                TypeUtilities.getRawType(genericReturnType)
            );
        }
        
        @Test
        @DisplayName(
            "should return Object class when type is a wildcard type " + 
            "that has no extends declaration."
        )
        public void wildcardTypeTest1() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "wildcardTypeReturnType"
                );
                
            // This is a parameterized List<?> type. 
            // Need to extract the wildcard type below.
            Type genericReturnType = proxyMethod.returnType();

            assertTrue(genericReturnType instanceof ParameterizedType);

            ParameterizedType pt = TypeUtilities.asParameterizedType(genericReturnType);
            Type wildcardType = pt.getActualTypeArguments()[0];

            assertTrue(wildcardType instanceof WildcardType);
            assertEquals(
                Object.class, 
                TypeUtilities.getRawType(wildcardType)
            );
        }
        
        @Test
        @DisplayName(
            "should return correct upper bound when type is a wildcard type " + 
            "that has an extends declaration e.g. <? extends String>."
        )
        public void wildcardTypeTest2() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "wildcardTypeReturnTypeExtends"
                );
                
            // This is a parameterized List<?> type. 
            // Need to extract the wildcard type below.
            Type genericReturnType = proxyMethod.returnType();

            assertTrue(genericReturnType instanceof ParameterizedType);

            ParameterizedType pt = TypeUtilities.asParameterizedType(genericReturnType);
            Type wildcardType = pt.getActualTypeArguments()[0];

            assertTrue(wildcardType instanceof WildcardType);
            assertEquals(
                String.class, 
                TypeUtilities.getRawType(wildcardType)
            );
        }
        
        @Test
        @DisplayName(
            "should return correct upper bound when type is a wildcard type " + 
            "that has a super declaration e.g. <? super String>."
        )
        public void wildcardTypeTest3() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "wildcardTypeReturnTypeSuper"
                );
                
            // This is a parameterized List<?> type. 
            // Need to extract the wildcard type below.
            Type genericReturnType = proxyMethod.returnType();

            assertTrue(genericReturnType instanceof ParameterizedType);

            ParameterizedType pt = TypeUtilities.asParameterizedType(genericReturnType);
            Type wildcardType = pt.getActualTypeArguments()[0];

            assertTrue(wildcardType instanceof WildcardType);
            assertEquals(
                String.class, 
                TypeUtilities.getRawType(wildcardType)
            );
        }

        @Test
        @DisplayName("should throw when type argument is an unrecognized type")
        public void unrecognizedTypeTest() {
            // For any reason JDK decides to add a new type.
            assertThrows(
                IllegalArgumentException.class, 
                () -> TypeUtilities.getRawType(new UnsupportedType())
            );
        }

        @Test
        @DisplayName("should throw when type argument is an unrecognized type")
        public void unrecognizedGenericArrayComponentTypeTest() {
            GenericArrayType genericArrayType = new GenericArrayType() {
                @Override
                public Type getGenericComponentType() {
                    return new UnsupportedType();
                }
            };
            assertThrows(
                IllegalArgumentException.class, 
                () -> TypeUtilities.getRawType(genericArrayType)
            );
        }
    }

    @Nested
    class GetTypeParametersMethod {
        @Test
        @DisplayName("should return generic type parameter.")
        public void test1() {
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "parameterizedTypeReturnType"
                );
            
            Type genericReturnType = proxyMethod.returnType();

            assertArrayEquals(
                new Type[] { String.class },
                TypeUtilities.getTypeParameters(genericReturnType)
            );
        }

        @Test
        @DisplayName("should return empty array when type is not a parameterized type.")
        public void test2() {
            ProxyMethod proxyMethod = 
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "nonParameterizedTypeReturnType"
                );
            
            Type genericReturnType = proxyMethod.returnType();

            assertTrue(TypeUtilities.getTypeParameters(genericReturnType).length == 0);
        }
    }

    @Nested
    class IsClassMethod {
        @Test
        @DisplayName("should return true when type is a class.")
        public void test1() {
            Type type = String.class;
            assertTrue(type instanceof Class<?>);
            assertTrue(TypeUtilities.isClass(type));
        }

        @Test
        @DisplayName("should return false when type is not a class.")
        public void test2() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "parameterizedTypeReturnType"
                );
                
            Type genericReturnType = proxyMethod.returnType();

            assertFalse(genericReturnType instanceof Class<?>);
            assertFalse(TypeUtilities.isClass(genericReturnType));
        }
    }

    @Nested
    class IsParameterizedTypeMethod {
        @Test
        @DisplayName("should return true when type is a parameterized type.")
        public void test1() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "parameterizedTypeReturnType"
                );
                
            Type genericReturnType = proxyMethod.returnType();
            assertTrue(genericReturnType instanceof ParameterizedType);
            assertTrue(TypeUtilities.isParameterizedType(genericReturnType));
        }

        @Test
        @DisplayName("should return false when type is not a parameterized type.")
        public void test2() {
            Type type = String.class;
            assertFalse(type instanceof ParameterizedType);
            assertFalse(TypeUtilities.isParameterizedType(type));
        }
    }

    @Nested
    class IsGenericArrayTypeMethod {
        @Test
        @DisplayName("should return true when type is a generic array type.")
        public void test1() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "genericArrayTypeReturnTypeWithTypeVariable"
                );
                
            Type genericReturnType = proxyMethod.returnType();
            assertTrue(genericReturnType instanceof GenericArrayType);
            assertTrue(TypeUtilities.isGenericArrayType(genericReturnType));
        }

        @Test
        @DisplayName("should return false when type is not a generic array type.")
        public void test2() {
            Type type = String.class;
            assertFalse(type instanceof GenericArrayType);
            assertFalse(TypeUtilities.isGenericArrayType(type));
        }
    }

    @Nested
    class IsTypeVariableMethod {
        @Test
        @DisplayName("should return true when type is a type variable.")
        public void test1() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "typeVariableReturnType"
                );
                
            Type genericReturnType = proxyMethod.returnType();
            assertTrue(genericReturnType instanceof TypeVariable<?>);
            assertTrue(TypeUtilities.isTypeVariable(genericReturnType));
        }

        @Test
        @DisplayName("should return false when type is not a type variable.")
        public void test2() {
            Type type = String.class;
            assertFalse(type instanceof TypeVariable<?>);
            assertFalse(TypeUtilities.isTypeVariable(type));
        }
    }

    @Nested
    class IsWildcardTypeMethod {
        @Test
        @DisplayName("should return true when type is a wildcard type.")
        public void test1() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "wildcardTypeReturnType"
                );
            
            // This is a parameterized List<?> type. 
            // Need to extract the wildcard type below.
            Type genericReturnType = proxyMethod.returnType();

            assertTrue(genericReturnType instanceof ParameterizedType);

            ParameterizedType pt = TypeUtilities.asParameterizedType(genericReturnType);
            Type wildcardType = pt.getActualTypeArguments()[0];
                
            assertTrue(wildcardType instanceof WildcardType);
            assertTrue(TypeUtilities.isWildcardType(wildcardType));
        }

        @Test
        @DisplayName("should return false when type is not a wildcard type.")
        public void test2() {
            Type type = String.class;
            assertFalse(type instanceof WildcardType);
            assertFalse(TypeUtilities.isWildcardType(type));
        }
    }

    @Nested
    class AsClassMethod {
        @Test
        @DisplayName("should return a class when type is a class.")
        public void test1() {
            Type type = String.class;

            assertEquals(
                String.class, 
                TypeUtilities.asClass(type)
            );
        }

        @Test
        @DisplayName("should return null when type is not a class.")
        public void test2() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "parameterizedTypeReturnType"
                );
                
            Type genericReturnType = proxyMethod.returnType();
            assertTrue(genericReturnType instanceof ParameterizedType);
            assertNull(TypeUtilities.asClass(genericReturnType));
        }
    }

    @Nested
    class AsParameterizedTypeMethod {
        @Test
        @DisplayName("should return a parameterized type when type is a parameterized type.")
        public void test1() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "parameterizedTypeReturnType"
                );
                
            Type genericReturnType = proxyMethod.returnType();
            assertTrue(genericReturnType instanceof ParameterizedType);
            assertEquals(
                genericReturnType,
                TypeUtilities.asParameterizedType(genericReturnType)
            );
        }

        @Test
        @DisplayName("should return null when type is not a parameterized type.")
        public void test2() {
            Type type = String.class;
            assertFalse(type instanceof ParameterizedType);
            assertNull(TypeUtilities.asParameterizedType(type));
        }
    }

    @Nested
    class AsGenericArrayTypeMethod {
        @Test
        @DisplayName("should return a generic array type when type is a generic array type.")
        public void test1() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "genericArrayTypeReturnTypeWithTypeVariable"
                );
                
            Type genericReturnType = proxyMethod.returnType();
            assertTrue(genericReturnType instanceof GenericArrayType);
            assertEquals(
                genericReturnType,
                TypeUtilities.asGenericArrayType(genericReturnType)
            );
        }

        @Test
        @DisplayName("should return null when type is not a generic array type.")
        public void test2() {
            Type type = String.class;
            assertFalse(type instanceof GenericArrayType);
            assertNull(TypeUtilities.asGenericArrayType(type));
        }
    }

    @Nested
    class AsTypeVariableMethod {
        @Test
        @DisplayName("should return a type variable when type is a type variable.")
        public void test1() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "typeVariableReturnType"
                );
                
            Type genericReturnType = proxyMethod.returnType();
            assertTrue(genericReturnType instanceof TypeVariable<?>);
            assertEquals(
                genericReturnType,
                TypeUtilities.asTypeVariable(genericReturnType)
            );
        }

        @Test
        @DisplayName("should return false when type is not a type variable.")
        public void test2() {
            Type type = String.class;
            assertFalse(type instanceof TypeVariable<?>);
            assertNull(TypeUtilities.asTypeVariable(type));
        }
    }

    @Nested
    class AsWildcardTypeMethod {
        @Test
        @DisplayName("should return a wildcard type when type is a wildcard type.")
        public void test1() {
            ProxyMethod proxyMethod =
                ProxyMethodUtils.fromMethod(
                    TypesInterface.class,
                    "wildcardTypeReturnType"
                );
            
            // This is a parameterized List<?> type. 
            // Need to extract the wildcard type below.
            Type genericReturnType = proxyMethod.returnType();

            assertTrue(genericReturnType instanceof ParameterizedType);

            ParameterizedType pt = TypeUtilities.asParameterizedType(genericReturnType);
            Type wildcardType = pt.getActualTypeArguments()[0];
                
            assertTrue(wildcardType instanceof WildcardType);
            assertEquals(
                wildcardType,
                TypeUtilities.asWildcardType(wildcardType)
            );
        }

        @Test
        @DisplayName("should return null when type is not a wildcard type.")
        public void test2() {
            Type type = String.class;
            assertFalse(type instanceof WildcardType);
            assertNull(TypeUtilities.asWildcardType(type));
        }
    }

    public static interface TypesInterface {
        String nonParameterizedTypeReturnType();
        List<String> parameterizedTypeReturnType();
        
        <T> T[] genericArrayTypeReturnTypeWithTypeVariable();
        <T extends List<?>> T[] genericArrayTypeReturnTypeWithTypeVariableExtends();
        
        <T> T typeVariableReturnType();
        <T extends List<?>> T typeVariableReturnTypeExtends();

        List<?> wildcardTypeReturnType();
        List<? extends String> wildcardTypeReturnTypeExtends();
        List<? super String> wildcardTypeReturnTypeSuper();
    }

    public static class UnsupportedType implements Type {}
}
