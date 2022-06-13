package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeUtilitiesTests {
    private static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
        ExternalizedProperties.builder().defaults().build();
    private static final InvocationContextTestFactory<TypesInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(TypesInterface.class);
    
    @Nested
    class GetRawTypeMethod {
        @Test
        @DisplayName("should throw when type argument is null.")
        void nullTest() {
            assertThrows(
                IllegalArgumentException.class,
                () -> TypeUtilities.getRawType(null)
            );
        }

        @Test
        @DisplayName("should return same class when type is a class.")
        void classTest1() {
            assertEquals(
                String.class, 
                TypeUtilities.getRawType(String.class)
            );
        }

        @Test
        @DisplayName("should return raw type when type is a parameterized type.")
        void parameterizedTest1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::parameterizedTypeReturnType,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();

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
        void genericArrayTypeTest1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::genericArrayTypeReturnTypeWithTypeVariable,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();

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
        void genericArrayTypeTest2() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::genericArrayTypeReturnTypeWithTypeVariableExtends,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();

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
        void typeVariableTest1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::typeVariableReturnType,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();

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
        void typeVariableTest2() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::typeVariableReturnTypeExtends,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();

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
        void wildcardTypeTest1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::wildcardTypeReturnType,
                EXTERNALIZED_PROPERTIES
            );
                
            // This is a parameterized List<?> type. 
            // Need to extract the wildcard type below.
            Type genericReturnType = context.method().returnType();

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
        void wildcardTypeTest2() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::wildcardTypeReturnTypeExtends,
                EXTERNALIZED_PROPERTIES
            );
                
            // This is a parameterized List<?> type. 
            // Need to extract the wildcard type below.
            Type genericReturnType = context.method().returnType();

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
        void wildcardTypeTest3() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::wildcardTypeReturnTypeSuper,
                EXTERNALIZED_PROPERTIES
            );
                
            // This is a parameterized List<?> type. 
            // Need to extract the wildcard type below.
            Type genericReturnType = context.method().returnType();

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
        void unrecognizedTypeTest() {
            // For any reason JDK decides to add a new type.
            UnsupportedType unsupportedType = new UnsupportedType();
            assertThrows(
                IllegalArgumentException.class, 
                () -> TypeUtilities.getRawType(unsupportedType)
            );
        }

        @Test
        @DisplayName("should throw when type argument is an unrecognized type")
        void unrecognizedGenericArrayComponentTypeTest() {
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
        void test1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::parameterizedTypeReturnType,
                EXTERNALIZED_PROPERTIES
            );
            
            Type genericReturnType = context.method().returnType();

            assertArrayEquals(
                new Type[] { String.class },
                TypeUtilities.getTypeParameters(genericReturnType)
            );
        }

        @Test
        @DisplayName("should return empty array when type is not a parameterized type.")
        void test2() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::nonParameterizedTypeReturnType,
                EXTERNALIZED_PROPERTIES
            );
            
            Type genericReturnType = context.method().returnType();

            assertEquals(0, TypeUtilities.getTypeParameters(genericReturnType).length);
        }
    }

    @Nested
    class IsClassMethod {
        @Test
        @DisplayName("should return true when type is a class.")
        void test1() {
            Type type = String.class;
            assertTrue(type instanceof Class<?>);
            assertTrue(TypeUtilities.isClass(type));
        }

        @Test
        @DisplayName("should return false when type is not a class.")
        void test2() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::parameterizedTypeReturnType,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();

            assertFalse(genericReturnType instanceof Class<?>);
            assertFalse(TypeUtilities.isClass(genericReturnType));
        }
    }

    @Nested
    class IsParameterizedTypeMethod {
        @Test
        @DisplayName("should return true when type is a parameterized type.")
        void test1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::parameterizedTypeReturnType,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();
            assertTrue(genericReturnType instanceof ParameterizedType);
            assertTrue(TypeUtilities.isParameterizedType(genericReturnType));
        }

        @Test
        @DisplayName("should return false when type is not a parameterized type.")
        void test2() {
            Type type = String.class;
            assertFalse(type instanceof ParameterizedType);
            assertFalse(TypeUtilities.isParameterizedType(type));
        }
    }

    @Nested
    class IsGenericArrayTypeMethod {
        @Test
        @DisplayName("should return true when type is a generic array type.")
        void test1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::genericArrayTypeReturnTypeWithTypeVariable,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();
            assertTrue(genericReturnType instanceof GenericArrayType);
            assertTrue(TypeUtilities.isGenericArrayType(genericReturnType));
        }

        @Test
        @DisplayName("should return false when type is not a generic array type.")
        void test2() {
            Type type = String.class;
            assertFalse(type instanceof GenericArrayType);
            assertFalse(TypeUtilities.isGenericArrayType(type));
        }
    }

    @Nested
    class IsTypeVariableMethod {
        @Test
        @DisplayName("should return true when type is a type variable.")
        void test1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::typeVariableReturnType,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();
            assertTrue(genericReturnType instanceof TypeVariable<?>);
            assertTrue(TypeUtilities.isTypeVariable(genericReturnType));
        }

        @Test
        @DisplayName("should return false when type is not a type variable.")
        void test2() {
            Type type = String.class;
            assertFalse(type instanceof TypeVariable<?>);
            assertFalse(TypeUtilities.isTypeVariable(type));
        }
    }

    @Nested
    class IsWildcardTypeMethod {
        @Test
        @DisplayName("should return true when type is a wildcard type.")
        void test1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::wildcardTypeReturnType,
                EXTERNALIZED_PROPERTIES
            );
            
            // This is a parameterized List<?> type. 
            // Need to extract the wildcard type below.
            Type genericReturnType = context.method().returnType();

            assertTrue(genericReturnType instanceof ParameterizedType);

            ParameterizedType pt = TypeUtilities.asParameterizedType(genericReturnType);
            Type wildcardType = pt.getActualTypeArguments()[0];
                
            assertTrue(wildcardType instanceof WildcardType);
            assertTrue(TypeUtilities.isWildcardType(wildcardType));
        }

        @Test
        @DisplayName("should return false when type is not a wildcard type.")
        void test2() {
            Type type = String.class;
            assertFalse(type instanceof WildcardType);
            assertFalse(TypeUtilities.isWildcardType(type));
        }
    }

    @Nested
    class AsClassMethod {
        @Test
        @DisplayName("should return a class when type is a class.")
        void test1() {
            Type type = String.class;

            assertEquals(
                String.class, 
                TypeUtilities.asClass(type)
            );
        }

        @Test
        @DisplayName("should return null when type is not a class.")
        void test2() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::parameterizedTypeReturnType,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();
            assertTrue(genericReturnType instanceof ParameterizedType);
            assertNull(TypeUtilities.asClass(genericReturnType));
        }
    }

    @Nested
    class AsParameterizedTypeMethod {
        @Test
        @DisplayName("should return a parameterized type when type is a parameterized type.")
        void test1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::parameterizedTypeReturnType,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();
            assertTrue(genericReturnType instanceof ParameterizedType);
            assertEquals(
                genericReturnType,
                TypeUtilities.asParameterizedType(genericReturnType)
            );
        }

        @Test
        @DisplayName("should return null when type is not a parameterized type.")
        void test2() {
            Type type = String.class;
            assertFalse(type instanceof ParameterizedType);
            assertNull(TypeUtilities.asParameterizedType(type));
        }
    }

    @Nested
    class AsGenericArrayTypeMethod {
        @Test
        @DisplayName("should return a generic array type when type is a generic array type.")
        void test1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::genericArrayTypeReturnTypeWithTypeVariable,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();
            assertTrue(genericReturnType instanceof GenericArrayType);
            assertEquals(
                genericReturnType,
                TypeUtilities.asGenericArrayType(genericReturnType)
            );
        }

        @Test
        @DisplayName("should return null when type is not a generic array type.")
        void test2() {
            Type type = String.class;
            assertFalse(type instanceof GenericArrayType);
            assertNull(TypeUtilities.asGenericArrayType(type));
        }
    }

    @Nested
    class AsTypeVariableMethod {
        @Test
        @DisplayName("should return a type variable when type is a type variable.")
        void test1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::typeVariableReturnType,
                EXTERNALIZED_PROPERTIES
            );
                
            Type genericReturnType = context.method().returnType();
            assertTrue(genericReturnType instanceof TypeVariable<?>);
            assertEquals(
                genericReturnType,
                TypeUtilities.asTypeVariable(genericReturnType)
            );
        }

        @Test
        @DisplayName("should return false when type is not a type variable.")
        void test2() {
            Type type = String.class;
            assertFalse(type instanceof TypeVariable<?>);
            assertNull(TypeUtilities.asTypeVariable(type));
        }
    }

    @Nested
    class AsWildcardTypeMethod {
        @Test
        @DisplayName("should return a wildcard type when type is a wildcard type.")
        void test1() {
            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                TypesInterface::wildcardTypeReturnType,
                EXTERNALIZED_PROPERTIES
            );
            
            // This is a parameterized List<?> type. 
            // Need to extract the wildcard type below.
            Type genericReturnType = context.method().returnType();

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
        void test2() {
            Type type = String.class;
            assertFalse(type instanceof WildcardType);
            assertNull(TypeUtilities.asWildcardType(type));
        }
    }

    @Nested
    class GetArrayTypeMethod {
        @Test
        @DisplayName("should throw when type argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> TypeUtilities.getArrayType(null)
            );
        }

        @Test
        @DisplayName("should return generic array type if type is a paramerized type")
        void test2() {
            Type parameterizedType = new TypeReference<List<String>>(){}.type();
            Type genericArrayType = TypeUtilities.getArrayType(parameterizedType);

            assertTrue(genericArrayType instanceof GenericArrayType);

            GenericArrayType casted = (GenericArrayType)genericArrayType;
            assertEquals(parameterizedType, casted.getGenericComponentType());
        }

        @Test
        @DisplayName("should return generic array type if type is a type variable")
        <T> void test3() {
            Type typeVariable = new TypeReference<T>(){}.type();
            Type genericArrayType = TypeUtilities.getArrayType(typeVariable);

            assertTrue(genericArrayType instanceof GenericArrayType);

            GenericArrayType casted = (GenericArrayType)genericArrayType;
            assertEquals(typeVariable, casted.getGenericComponentType());
        }

        @Test
        @DisplayName(
            "should return 2D generic array type if type is another generic array type"
        )
        <T> void test4() {
            Type genericArrayType = new TypeReference<List<String>[]>(){}.type();
            Type genericArrayType2d = TypeUtilities.getArrayType(genericArrayType);

            assertTrue(genericArrayType2d instanceof GenericArrayType);

            GenericArrayType casted = (GenericArrayType)genericArrayType2d;
            assertEquals(genericArrayType, casted.getGenericComponentType());
        }

        @Test
        @DisplayName(
            "should return generic array type whose generic component type " + 
            "is the wildcard type's upper bound (T in '? extends T')"
        )
        <T> void test5() {
            // We are sure this will succeed.
            ParameterizedType parameterizedType = TypeUtilities.asParameterizedType(
                new TypeReference<List<? extends Set<String>>>(){}.type()
            );
    
            // We are sure this will succeed.
            WildcardType wildcardType = TypeUtilities.asWildcardType(
                parameterizedType.getActualTypeArguments()[0]
            );

            // This is Set<String>.
            Type wildcardTypeUpperBound = wildcardType.getUpperBounds()[0];

            Type genericArrayType = TypeUtilities.getArrayType(wildcardType);

            assertTrue(genericArrayType instanceof GenericArrayType);

            GenericArrayType casted = (GenericArrayType)genericArrayType;
            assertEquals(
                wildcardTypeUpperBound, 
                casted.getGenericComponentType()
            );
        }

        @Test
        @DisplayName(
            "should return generic array type whose generic component type " + 
            "is the wildcard type's lower bound (T in '? super T')"
        )
        <T> void test6() {
            // We are sure this will succeed.
            ParameterizedType parameterizedType = TypeUtilities.asParameterizedType(
                new TypeReference<List<? super Set<String>>>(){}.type()
            );
    
            // We are sure this will succeed.
            WildcardType wildcardType = TypeUtilities.asWildcardType(
                parameterizedType.getActualTypeArguments()[0]
            );
            
            // This is Set<String>.
            Type wildcardTypeLowerBound = wildcardType.getLowerBounds()[0];

            Type genericArrayType = TypeUtilities.getArrayType(wildcardType);

            assertTrue(genericArrayType instanceof GenericArrayType);

            GenericArrayType casted = (GenericArrayType)genericArrayType;
            assertEquals(
                wildcardTypeLowerBound, 
                casted.getGenericComponentType()
            );
        }

        @Test
        @DisplayName("should return raw array type if type is a class")
        <T> void test7() {
            Class<String> arrayComponentClass = String.class;
            Type genericArrayType = TypeUtilities.getArrayType(arrayComponentClass);

            assertTrue(genericArrayType instanceof Class<?>);

            Class<?> casted = (Class<?>)genericArrayType;
            assertEquals(arrayComponentClass, casted.getComponentType());
        }
    }

    @Nested
    class GetRawArrayTypeMethod {
        @Test
        @DisplayName("should throw when type argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> TypeUtilities.getRawArrayType(null)
            );
        }

        @Test
        @DisplayName("should return raw array type for paramerized type")
        void test2() {
            Type parameterizedType = new TypeReference<List<String>>(){}.type();
            Class<?> rawArrayType = TypeUtilities.getRawArrayType(parameterizedType);
            assertEquals(List[].class, rawArrayType);
        }

        @Test
        @DisplayName("should return raw array type for type variable")
        <T> void test3() {
            Type typeVariable = new TypeReference<T>(){}.type();
            Class<?> rawArrayType = TypeUtilities.getRawArrayType(typeVariable);
            assertEquals(Object[].class, rawArrayType);
        }

        @Test
        @DisplayName(
            "should return 2D raw array type if type is a generic array type"
        )
        <T> void test4() {
            Type genericArrayType = new TypeReference<List<String>[]>(){}.type();
            Class<?> rawArrayType = TypeUtilities.getRawArrayType(genericArrayType);
            assertEquals(List[][].class, rawArrayType);
        }

        @Test
        @DisplayName(
            "should return 2D raw array type if type is another array type"
        )
        <T> void test5() {
            Class<?> rawArrayType = TypeUtilities.getRawArrayType(String[].class);
            assertEquals(String[][].class, rawArrayType);
        }

        @Test
        @DisplayName(
            "should return raw array type whose component type " + 
            "is the wildcard type's upper bound (T in '? extends T')"
        )
        <T> void test6() {
            // We are sure this will succeed.
            ParameterizedType parameterizedType = TypeUtilities.asParameterizedType(
                new TypeReference<List<? extends Set<String>>>(){}.type()
            );
    
            // We are sure this will succeed.
            WildcardType wildcardType = TypeUtilities.asWildcardType(
                parameterizedType.getActualTypeArguments()[0]
            );

            Class<?> rawArrayType = TypeUtilities.getRawArrayType(wildcardType);

            assertEquals(
                Set[].class, 
                rawArrayType
            );
        }

        @Test
        @DisplayName(
            "should return generic array type whose generic component type " + 
            "is the wildcard type's lower bound (T in '? super T')"
        )
        <T> void test7() {
            // We are sure this will succeed.
            ParameterizedType parameterizedType = TypeUtilities.asParameterizedType(
                new TypeReference<List<? super Set<String>>>(){}.type()
            );
    
            // We are sure this will succeed.
            WildcardType wildcardType = TypeUtilities.asWildcardType(
                parameterizedType.getActualTypeArguments()[0]
            );

            Class<?> rawArrayType = TypeUtilities.getRawArrayType(wildcardType);

            assertEquals(
                Set[].class, 
                rawArrayType
            );
        }

        @Test
        @DisplayName("should return raw array type if type is a class")
        <T> void test8() {
            Class<String> arrayComponentClass = String.class;
            Class<?> rawArrayType = TypeUtilities.getRawArrayType(arrayComponentClass);
            assertEquals(String[].class, rawArrayType);
        }
    }

    private static interface TypesInterface {
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

    private static class UnsupportedType implements Type {}
}
