package io.github.joeljeremy7.externalizedproperties.core.internal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationArguments;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.ResolverFacade;
import io.github.joeljeremy7.externalizedproperties.core.conversion.Delimiter;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.MethodUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class InvocationContextFactoryTests {
  private static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
      ExternalizedProperties.builder().defaults().build();
  private static final ProxyInterface PROXY =
      EXTERNALIZED_PROPERTIES.initialize(ProxyInterface.class);

  @Nested
  class CreateMethod {
    @Test
    @DisplayName("should not return null")
    void test1() {
      InvocationContextFactory invocationContextFactory = invocationContextFactory();

      Method method = MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::resolve);
      String propertyName = "property";
      Object[] args = new Object[] {propertyName};

      InvocationContext context = invocationContextFactory.create(PROXY, method, args);

      assertNotNull(context);
    }
  }

  @Nested
  class ProxyMethodTests {
    @Nested
    class DeclaringClassMethod {
      @Test
      @DisplayName("should return method's declaring class")
      void test1() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);

        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        assertEquals(proxyInterfaceMethod.getDeclaringClass(), proxyMethod.declaringClass());
      }
    }

    @Nested
    class NameMethod {
      @Test
      @DisplayName("should return method name")
      void test1() {
        String methodName = "property";
        Method proxyInterfaceMethod = MethodUtils.getMethod(ProxyInterface.class, methodName);

        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        assertEquals(methodName, proxyMethod.name());
      }
    }

    @Nested
    class AnnotationsMethod {
      @Test
      @DisplayName("should return all annotations the method is annotated with.")
      void test() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Annotation[] annotations = proxyMethod.annotations();

        assertEquals(1, annotations.length);
        assertEquals(ExternalizedProperty.class, annotations[0].annotationType());
      }

      @Test
      @DisplayName("should return empty array when method is not annotated with any annotations.")
      void test2() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(
                ProxyInterface.class, ProxyInterface::defaultPropertyWithParameterNoAnnotation);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Annotation[] annotations = proxyMethod.annotations();

        assertEquals(0, annotations.length);
      }
    }

    @Nested
    class FindAnnotationMethod {
      @Test
      @DisplayName(
          "should return the annotation "
              + "when method is annotation with the specified annotation class.")
      void test() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Optional<ExternalizedProperty> annotation =
            proxyMethod.findAnnotation(ExternalizedProperty.class);

        assertTrue(annotation.isPresent());
      }

      @Test
      @DisplayName(
          "should return an empty Optional "
              + "when method is not annotated with the specified annotation class.")
      void test2() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        // Method not annotated with @Delimiter.
        Optional<Delimiter> nonExistentAnnotation = proxyMethod.findAnnotation(Delimiter.class);

        assertFalse(nonExistentAnnotation.isPresent());
      }
    }

    @Nested
    class HasAnnotationMethod {
      @Test
      @DisplayName(
          "should return true when method is annotated with the specified annotation class.")
      void test1() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        assertTrue(proxyMethod.hasAnnotation(ExternalizedProperty.class));
      }

      @Test
      @DisplayName(
          "should return false when method is annotated with the specified annotation class.")
      void test2() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        assertFalse(proxyMethod.hasAnnotation(Delimiter.class));
      }
    }

    @Nested
    class RawReturnTypeMethod {
      @Test
      @DisplayName("should return method's return type")
      void test1() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Class<?> returnType = proxyMethod.rawReturnType();

        assertEquals(proxyInterfaceMethod.getReturnType(), returnType);
      }
    }

    @Nested
    class ReturnTypeMethod {
      @Test
      @DisplayName("should return method's generic return type")
      void test1() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Type genericReturnType = proxyMethod.returnType();

        assertEquals(proxyInterfaceMethod.getGenericReturnType(), genericReturnType);
      }
    }

    @Nested
    class RawParameterTypesMethod {
      @Test
      @DisplayName("should return method's parameter types")
      void test1() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(
                ProxyInterface.class, ProxyInterface::defaultPropertyWithParameter);

        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Class<?>[] parameterTypes = proxyMethod.rawParameterTypes();

        assertArrayEquals(proxyInterfaceMethod.getParameterTypes(), parameterTypes);
      }
    }

    @Nested
    class RawParameterTypeAtMethod {
      @Test
      @DisplayName("should return method parameter's raw type at specified index")
      void test1() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(
                ProxyInterface.class, ProxyInterface::defaultPropertyWithParameter);

        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Optional<Class<?>> parameterType = proxyMethod.rawParameterTypeAt(0);

        assertTrue(parameterType.isPresent());
        assertEquals(String.class, parameterType.get());
      }

      @Test
      @DisplayName("should return empty Optional if there are no parameter at the specified index.")
      void test2() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(
                ProxyInterface.class, ProxyInterface::defaultPropertyWithParameter);

        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Optional<Class<?>> parameterType = proxyMethod.rawParameterTypeAt(1);

        assertFalse(parameterType.isPresent());
      }
    }

    @Nested
    class ParameterTypesMethod {
      @Test
      @DisplayName("should return method's generic parameter types")
      void test1() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(
                ProxyInterface.class, ProxyInterface::defaultPropertyWithParameter);

        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Type[] parameterTypes = proxyMethod.parameterTypes();

        assertArrayEquals(proxyInterfaceMethod.getGenericParameterTypes(), parameterTypes);
      }
    }

    @Nested
    class ParameterTypeAtMethod {
      @Test
      @DisplayName("should return method parameter's type at specified index")
      void test1() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(
                ProxyInterface.class, ProxyInterface::defaultPropertyWithParameter);

        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Optional<Type> parameterType = proxyMethod.parameterTypeAt(0);

        assertTrue(parameterType.isPresent());
        assertEquals((Type) String.class, parameterType.get());
      }

      @Test
      @DisplayName("should return empty Optional if there are no parameter at the specified index.")
      void test2() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(
                ProxyInterface.class, ProxyInterface::defaultPropertyWithParameter);

        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Optional<Type> parameterType = proxyMethod.parameterTypeAt(1);

        assertFalse(parameterType.isPresent());
      }
    }

    @Nested
    class HasReturnTypeMethod {
      @Test
      @DisplayName("should return true when method's return type matches")
      void test1() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        assertTrue(proxyMethod.hasReturnType(String.class));
      }

      @Test
      @DisplayName("should return false when method's return type does not match")
      void test2() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        assertFalse(proxyMethod.hasReturnType(Integer.class));
      }
    }

    @Nested
    class HasReturnTypeMethodWithTypeArgument {
      @Test
      @DisplayName("should return true when method's return type matches")
      void test1() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Type type = String.class;

        assertTrue(proxyMethod.hasReturnType(type));
      }

      @Test
      @DisplayName("should return false when method's return type does not match")
      void test2() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Type type = Integer.class;

        assertFalse(proxyMethod.hasReturnType(type));
      }
    }

    @Nested
    class TypeParametersOfReturnTypeMethod {
      @Test
      @DisplayName(
          "should return method return type's generic type parameters "
              + "when method's return type is a generic type.")
      void test1() {
        Method method =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::optionalProperty);
        ProxyMethod proxyMethod = proxyMethod(method);

        Type[] genericTypeParameters = proxyMethod.typeParametersOfReturnType();

        // Optional has <String> generic type parameter
        assertEquals(1, genericTypeParameters.length);
        assertEquals(String.class, genericTypeParameters[0]);
      }

      @Test
      @DisplayName(
          "should return empty generic type parameter list "
              + "when method's return type is not a generic type.")
      void test2() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Type[] genericTypeParameters = proxyMethod.typeParametersOfReturnType();

        // ProxyInterface.property returns a String which is not generic.
        assertEquals(0, genericTypeParameters.length);
      }
    }

    @Nested
    class TypeParameterOfReturnTypeAtMethod {
      @Test
      @DisplayName(
          "should return method return type's generic type parameter "
              + "when method's return type is a generic type.")
      void test1() {
        Method method =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::optionalProperty);
        ProxyMethod proxyMethod = proxyMethod(method);

        Optional<Type> genericTypeParameter = proxyMethod.typeParameterOfReturnTypeAt(0);

        // Optional has <String> generic type parameter
        assertTrue(genericTypeParameter.isPresent());
        assertEquals(String.class, genericTypeParameter.get());
      }

      @Test
      @DisplayName(
          "should return empty Optional when method's return type "
              + "is a generic type but requested type parameter index is out of bounds.")
      void test2() {
        Method method =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::optionalProperty);
        ProxyMethod proxyMethod = proxyMethod(method);

        // Index out of bounds.
        Optional<Type> genericTypeParameter = proxyMethod.typeParameterOfReturnTypeAt(99);

        assertFalse(genericTypeParameter.isPresent());
      }

      @Test
      @DisplayName(
          "should return empty Optional" + "when method's return type is not a generic type.")
      void test3() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        Optional<Type> genericTypeParameter = proxyMethod.typeParameterOfReturnTypeAt(0);

        // ProxyInterface.property returns a String which is not generic.
        assertFalse(genericTypeParameter.isPresent());
      }
    }

    @Nested
    class IsDefaultInterfaceMethodMethod {
      @Test
      @DisplayName("should return true when method is a default interface method")
      void test1() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(
                ProxyInterface.class, ProxyInterface::defaultPropertyWithParameterNoAnnotation);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        assertTrue(proxyMethod.isDefaultInterfaceMethod());
      }

      @Test
      @DisplayName("should return false when method is not a default interface method")
      void test2() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        assertFalse(proxyMethod.isDefaultInterfaceMethod());
      }
    }

    @Nested
    class MethodSignatureStringMethod {
      @Test
      @DisplayName("should return method's signature string")
      void test1() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);

        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        String methodSignature = proxyMethod.methodSignatureString();

        // Since method is non-generic,
        // Method.toGenericString() and Method.toString() returns the same signature.
        assertEquals(proxyInterfaceMethod.toGenericString(), methodSignature);
        assertEquals(proxyInterfaceMethod.toString(), methodSignature);
      }

      @Test
      @DisplayName("should return method's generic signature string")
      void test2() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::optionalProperty);
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
      @DisplayName("should match methodSignatureString() method")
      void test2() {
        Method proxyInterfaceMethod =
            MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::optionalProperty);
        ProxyMethod proxyMethod = proxyMethod(proxyInterfaceMethod);

        String methodSignature = proxyMethod.methodSignatureString();

        assertEquals(methodSignature, proxyMethod.toString());
      }
    }
  }

  @Nested
  class InvocationContextTests {
    @Nested
    class ExternalizedPropertiesMethod {
      @Test
      @DisplayName("should return externalized properties")
      void test1() {
        InvocationContextFactory invocationContextFactory = invocationContextFactory();

        Method method = MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::resolve);
        Object[] args = new Object[] {"property"};

        InvocationContext context = invocationContextFactory.create(PROXY, method, args);

        assertNotNull(context);

        assertEquals(EXTERNALIZED_PROPERTIES, context.externalizedProperties());
      }
    }

    @Nested
    class MethodMethod {
      @Test
      @DisplayName("should return proxy method")
      void test1() {
        InvocationContextFactory invocationContextFactory = invocationContextFactory();

        Method method = MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::resolve);
        Object[] args = new Object[] {"property"};

        InvocationContext context = invocationContextFactory.create(PROXY, method, args);

        assertNotNull(context.method());
      }
    }

    @Nested
    class ArgumentsMethod {
      @Test
      @DisplayName("should return invocation arguments")
      void test1() {
        InvocationContextFactory invocationContextFactory = invocationContextFactory();

        Method method = MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::resolve);
        Object[] args = new Object[] {"property"};

        InvocationContext context = invocationContextFactory.create(PROXY, method, args);

        assertNotNull(context.arguments());
      }
    }
  }

  @Nested
  class InvocationArgumentsTests {
    @Nested
    class CountMethod {
      @Test
      @DisplayName("should return length of arguments array")
      void test1() {
        Method method = MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::resolve);
        Object[] args = new Object[] {"property"};

        InvocationArguments invocationArgs = invocationArguments(method, args);
        assertEquals(args.length, invocationArgs.count());
      }
    }

    @Nested
    class GetMethod {
      @Test
      @DisplayName("should return a copy of arguments array")
      void test1() {
        Method method = MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::resolve);
        Object[] args = new Object[] {"property"};

        InvocationArguments invocationArgs = invocationArguments(method, args);

        Object[] argsClone = invocationArgs.get();
        // Array entries are equals.
        assertArrayEquals(args, argsClone);
        // Not same because get() returns a clone.
        assertNotSame(args, argsClone);
      }

      @Test
      @DisplayName(
          "should return a copy of arguments array "
              + "(modifications will not be reflected in the source args array)")
      void test2() {
        Method method = MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::resolve);
        Object[] args = new Object[] {"property"};

        InvocationArguments invocationArgs = invocationArguments(method, args);

        Object[] argsClone = invocationArgs.get();
        // Modify the args array.
        argsClone[0] = "modified-property";

        Object[] anotherArgsClone = invocationArgs.get();
        // Value in newly retrieved args is still same as the original source value.
        assertEquals(args[0], anotherArgsClone[0]);
      }
    }

    @Nested
    class GetMethodWithIndexOverload {
      @Test
      @DisplayName("should return argument at the specified index")
      void test1() {
        Method method = MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::resolve);
        Object[] args = new Object[] {"property"};

        InvocationArguments invocationArgs = invocationArguments(method, args);
        Optional<?> arg = invocationArgs.get(0);
        assertTrue(arg.isPresent());
        assertEquals(args[0], arg.get());
      }

      @Test
      @DisplayName("should return empty Optional when no argument exists at the specified index")
      void test2() {
        Method method = MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        Object[] args = new Object[0];

        InvocationArguments invocationArgs = invocationArguments(method, args);
        Optional<?> arg = invocationArgs.get(0);
        assertFalse(arg.isPresent());
      }
    }

    @Nested
    class GetOrThrowMethod {
      @Test
      @DisplayName("should return argument at the specified index")
      void test1() {
        Method method = MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::resolve);
        Object[] args = new Object[] {"property"};

        InvocationArguments invocationArgs = invocationArguments(method, args);
        assertEquals(args[0], invocationArgs.getOrThrow(0));
      }

      @Test
      @DisplayName("should throw when no argument exists at the specified index")
      void test2() {
        Method method = MethodUtils.getMethod(ProxyInterface.class, ProxyInterface::property);
        Object[] args = new Object[0];

        InvocationArguments invocationArgs = invocationArguments(method, args);
        assertThrows(IndexOutOfBoundsException.class, () -> invocationArgs.getOrThrow(0));
      }
    }
  }

  private static InvocationContextFactory invocationContextFactory() {
    return new InvocationContextFactory(EXTERNALIZED_PROPERTIES);
  }

  private static InvocationContext invocationContext(Method method, Object... args) {
    return invocationContextFactory().create(PROXY, method, args);
  }

  private static ProxyMethod proxyMethod(Method method, Object... args) {
    return invocationContext(method, args).method();
  }

  private static InvocationArguments invocationArguments(Method method, Object... args) {
    return invocationContext(method, args).arguments();
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
