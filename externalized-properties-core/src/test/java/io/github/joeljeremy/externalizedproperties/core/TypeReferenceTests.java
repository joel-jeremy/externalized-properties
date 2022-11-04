package io.github.joeljeremy.externalizedproperties.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.testfixtures.MethodUtils;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class TypeReferenceTests {
  @Nested
  class TypeMethod {
    @Test
    @DisplayName("should return non-generic type")
    void test1() {
      TypeReference<Integer> listType = new TypeReference<Integer>() {};

      // Detects Integer.
      assertTrue(listType.type() instanceof Class<?>);
      assertEquals(Integer.class, listType.type());
    }

    @Test
    @DisplayName("should return generic parameterized type")
    void test2() {
      TypeReference<List<String>> listType = new TypeReference<List<String>>() {};

      // Method returns a List<String>.
      // Used for comparison of generic type.
      Method method = MethodUtils.getMethod(GenericTypes.class, GenericTypes::listProperty);

      // Detects List<String>.
      assertTrue(listType.type() instanceof ParameterizedType);
      assertEquals(method.getGenericReturnType(), listType.type());
    }

    @Test
    @DisplayName("should detect non-generic array type")
    void test4() {
      TypeReference<Integer[]> listType = new TypeReference<Integer[]>() {};

      assertTrue(listType.type() instanceof Class<?>);
      assertEquals(Integer[].class, listType.type());
    }

    @Test
    @DisplayName("should return generic array type")
    void test5() {
      TypeReference<Optional<Optional<String>>[]> listType =
          new TypeReference<Optional<Optional<String>>[]>() {};

      // Method returns a Optional<Optional<String>>[].
      // Used for comparison of generic type.
      Method method =
          MethodUtils.getMethod(GenericTypes.class, GenericTypes::arrayPropertyNestedGeneric);

      assertTrue(listType.type() instanceof GenericArrayType);
      assertEquals(method.getGenericReturnType(), listType.type());
    }

    @Test
    @DisplayName("should return upper bound of type variable")
    <T extends Number> void test6() {
      TypeReference<T> listType = new TypeReference<T>() {};

      assertTrue(listType.type() instanceof TypeVariable);
      // Type variable has correct bounds as <T>.
      TypeVariable<?> typeVariable = (TypeVariable<?>) listType.type();
      assertEquals(Number.class, typeVariable.getBounds()[0]);
    }
  }

  @Nested
  class RawTypeMethod {
    @Test
    @DisplayName("should return non-generic class when referenced type is non-generic")
    void test1() {
      TypeReference<Integer> listType = new TypeReference<Integer>() {};

      // Detects Integer.
      assertEquals(Integer.class, listType.rawType());
    }

    @Test
    @DisplayName("should return raw class of generic parameterized type")
    void test2() {
      TypeReference<List<String>> listType = new TypeReference<List<String>>() {};

      // Detects List<String>.
      assertEquals(List.class, listType.rawType());
    }

    @Test
    @DisplayName("should return raw class of non-generic array type")
    void test4() {
      TypeReference<Integer[]> listType = new TypeReference<Integer[]>() {};

      assertEquals(listType.rawType(), Integer[].class);
    }

    @Test
    @DisplayName("should return raw class of generic array type")
    void test5() {
      TypeReference<Optional<Integer>[]> listType = new TypeReference<Optional<Integer>[]>() {};

      assertEquals(listType.rawType(), Optional[].class);
    }

    @Test
    @DisplayName("should return upper bound of type variable")
    <T extends Number> void test6() {
      TypeReference<T> listType = new TypeReference<T>() {};

      // Should return the type variable bound.
      assertEquals(listType.rawType(), Number.class);
    }
  }

  @Nested
  class GenericTypeParametersMethod {
    @Test
    @DisplayName("should return empty array when referenced type is non-generic")
    void test1() {
      TypeReference<Integer> listType = new TypeReference<Integer>() {};

      assertEquals(0, listType.genericTypeParameters().length);
    }

    @Test
    @DisplayName("should return type parameter array when referenced type is a parameterized type")
    void test2() {
      TypeReference<List<String>> listType = new TypeReference<List<String>>() {};

      // Return String from List<String>.
      assertArrayEquals(new Type[] {String.class}, listType.genericTypeParameters());
    }

    @Test
    @DisplayName("should return empty array when referenced type is a non-generic array type")
    void test4() {
      TypeReference<Integer[]> listType = new TypeReference<Integer[]>() {};

      assertEquals(0, listType.genericTypeParameters().length);
    }

    @Test
    @DisplayName("should return empty array when referenced type is a generic array type")
    void test5() {
      TypeReference<Optional<Integer>[]> listType = new TypeReference<Optional<Integer>[]>() {};

      assertEquals(0, listType.genericTypeParameters().length);
    }

    @Test
    @DisplayName("should return empty array when referenced type is a type variable")
    <T> void test6() {
      TypeReference<T> listType = new TypeReference<T>() {};

      assertEquals(0, listType.genericTypeParameters().length);
    }
  }

  @Nested
  class HashCodeMethod {
    @Test
    @DisplayName("should return hash code of the type")
    void test1() {
      TypeReference<String> typeReference = new TypeReference<String>() {};
      Type type = typeReference.type();
      assertEquals(type.hashCode(), typeReference.hashCode());
    }

    @Test
    @DisplayName("should return the same of hash code everytime")
    void test2() {
      TypeReference<String> typeReference = new TypeReference<String>() {};
      int hashCode1 = typeReference.hashCode();
      int hashCode2 = typeReference.hashCode();
      assertEquals(hashCode1, hashCode2);
    }

    @Test
    @DisplayName("should return different hash codes for different types")
    void test3() {
      TypeReference<String> typeReference1 = new TypeReference<String>() {};
      TypeReference<Integer> typeReference2 = new TypeReference<Integer>() {};
      assertNotEquals(typeReference1.hashCode(), typeReference2.hashCode());
    }
  }

  @Nested
  class EqualsMethod {
    @Test
    @DisplayName("should return true when types are equal")
    void test1() {
      TypeReference<Integer> typeReference1 = new TypeReference<Integer>() {};
      TypeReference<Integer> typeReference2 = new TypeReference<Integer>() {};

      assertTrue(typeReference1.equals(typeReference2));
    }

    @Test
    @DisplayName("should return false when types are not equal")
    void test2() {
      TypeReference<String> typeReference1 = new TypeReference<String>() {};
      TypeReference<Integer> typeReference2 = new TypeReference<Integer>() {};

      assertFalse(typeReference1.equals(typeReference2));
    }

    @Test
    @DisplayName("should return false when other object is not a type reference")
    void test3() {
      TypeReference<String> typeReference1 = new TypeReference<String>() {};

      assertFalse(typeReference1.equals(new Object()));
    }

    @Test
    @DisplayName("should return false when other object is null")
    void test4() {
      TypeReference<String> typeReference1 = new TypeReference<String>() {};

      assertFalse(typeReference1.equals(null));
    }
  }

  static interface GenericTypes {
    List<String> listProperty();

    Optional<Optional<String>>[] arrayPropertyNestedGeneric();
  }
}
