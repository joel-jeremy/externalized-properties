package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubProxyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ArrayProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeReferenceTests {
    @Nested
    class TypeMethod {
        @Test
        @DisplayName("should return non-generic type")
        public void test1() {
            TypeReference<Integer> listType = new TypeReference<>(){};

            // Detects Integer.
            assertTrue(listType.type() instanceof Class<?>);
            assertEquals(Integer.class, listType.type());
        }

        @Test
        @DisplayName("should return generic parameterized type")
        public void test2() {
            TypeReference<List<String>> listType = new TypeReference<>(){};

            // Method returns a List<String>.
            // Used for comparison of generic type.
            Method method = 
                StubProxyMethodInfo.getMethod(
                    ListProxyInterface.class, 
                    "listProperty"
                );

            // Detects List<String>.
            assertTrue(listType.type() instanceof ParameterizedType);
            assertEquals(method.getGenericReturnType(), listType.type());
        }

        @Test
        @DisplayName("should detect non-generic array type")
        public void test4() {
            TypeReference<Integer[]> listType = new TypeReference<>(){};

            assertTrue(listType.type() instanceof Class<?>);
            assertEquals(Integer[].class, listType.type());
        }

        @Test
        @DisplayName("should return generic array type")
        public void test5() {
            TypeReference<Optional<Optional<String>>[]> listType = new TypeReference<>(){};

            // Method returns a Optional<Optional<String>>[].
            // Used for comparison of generic type.
            Method method = 
                StubProxyMethodInfo.getMethod(
                    ArrayProxyInterface.class, 
                    "arrayPropertyNestedGeneric"
                );
            
            assertTrue(listType.type() instanceof GenericArrayType);
            assertEquals(
                method.getGenericReturnType(), 
                listType.type()
            );
        }

        @Test
        @DisplayName("should return upper bound of type variable")
        public <T extends Number> void test6() {
            TypeReference<T> listType = new TypeReference<>(){};

            assertTrue(listType.type() instanceof TypeVariable);
            // Type variable has correct bounds as <T>.
            TypeVariable<?> typeVariable = (TypeVariable<?>)listType.type();
            assertEquals(Number.class, typeVariable.getBounds()[0]);
        }
    }

    @Nested
    class RawTypeMethod {
        @Test
        @DisplayName("should return non-generic class when referenced type is non-generic")
        public void test1() {
            TypeReference<Integer> listType = new TypeReference<>(){};

            // Detects Integer.
            assertEquals(Integer.class, listType.rawType());
        }

        @Test
        @DisplayName("should return raw class of generic parameterized type")
        public void test2() {
            TypeReference<List<String>> listType = new TypeReference<>(){};

            // Detects List<String>.
            assertEquals(List.class, listType.rawType());
        }

        @Test
        @DisplayName("should return raw class of non-generic array type")
        public void test4() {
            TypeReference<Integer[]> listType = new TypeReference<>(){};

            assertEquals(listType.rawType(), Integer[].class);
        }

        @Test
        @DisplayName("should return raw class of generic array type")
        public void test5() {
            TypeReference<Optional<Integer>[]> listType = new TypeReference<>(){};

            assertEquals(listType.rawType(), Optional[].class);
        }

        @Test
        @DisplayName("should return upper bound of type variable")
        public <T extends Number> void test6() {
            TypeReference<T> listType = new TypeReference<>(){};

            // Should return the type variable bound.
            assertEquals(listType.rawType(), Number.class);
        }
    }

    @Nested
    class GenericTypeParametersMethod {
        @Test
        @DisplayName("should return empty array when referenced type is non-generic")
        public void test1() {
            TypeReference<Integer> listType = new TypeReference<>(){};

            assertTrue(listType.genericTypeParameters().length == 0);
        }

        @Test
        @DisplayName(
            "should return type parameter array when referenced type is a parameterized type"
        )
        public void test2() {
            TypeReference<List<String>> listType = new TypeReference<>(){};

            // Return String from List<String>.
            assertArrayEquals(
                new Type[] { String.class },
                listType.genericTypeParameters()
            );
        }

        @Test
        @DisplayName(
            "should return empty array when referenced type is a non-generic array type"
        )
        public void test4() {
            TypeReference<Integer[]> listType = new TypeReference<>(){};

            assertTrue(listType.genericTypeParameters().length == 0);
        }

        @Test
        @DisplayName(
            "should return empty array when referenced type is a generic array type"
        )
        public void test5() {
            TypeReference<Optional<Integer>[]> listType = new TypeReference<>(){};

            assertTrue(listType.genericTypeParameters().length == 0);
        }

        @Test
        @DisplayName(
            "should return empty array when referenced type is a type variable"
        )
        public <T> void test6() {
            TypeReference<T> listType = new TypeReference<>(){};

            assertTrue(listType.genericTypeParameters().length == 0);
        }
    }
}
