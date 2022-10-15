package io.github.joeljeremy.externalizedproperties.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.joeljeremy.externalizedproperties.core.ConverterFacade;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.Resolver;
import io.github.joeljeremy.externalizedproperties.core.ResolverFacade;
import io.github.joeljeremy.externalizedproperties.core.TypeReference;
import io.github.joeljeremy.externalizedproperties.core.VariableExpanderFacade;
import io.github.joeljeremy.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.joeljeremy.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy.externalizedproperties.core.internal.proxy.ExternalizedPropertiesInvocationHandler;
import io.github.joeljeremy.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.StubResolver;
import io.github.joeljeremy.externalizedproperties.core.variableexpansion.SimpleVariableExpander;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SystemExternalizedPropertiesTests {
  @Nested
  class InitializeMethod {
    @Test
    @DisplayName("should throw when proxy interface argument is null")
    void validationTest1() {
      // Do not resolve any property.
      Resolver resolver = new StubResolver(StubResolver.NULL_DELEGATE);

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      assertThrows(IllegalArgumentException.class, () -> externalizedProperties.initialize(null));
    }

    @Test
    @DisplayName("should throw when proxy interface argument is not an interface")
    void validationTest2() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(SystemExternalizedPropertiesTests.class));
    }

    @Test
    @DisplayName("should throw when proxy interface contains void-returning methods")
    void validationTest3() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(VoidReturnTypeProxyInterface.class));
    }

    @Test
    @DisplayName("should throw when proxy interface contains Void-returning methods")
    void validationTest4() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(VoidClassReturnTypeProxyInterface.class));
    }

    @Test
    @DisplayName("should throw when @ResolverFacade proxy method have no" + "method parameters")
    void validationTest5() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(NoArgProxyInterface.class));
    }

    @Test
    @DisplayName(
        "should throw when @ResolverFacade proxy method have more than" + "2 method parameters")
    void validationTest6() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(MoreThanTwoArgsProxyInterface.class));
    }

    @Test
    @DisplayName(
        "should throw when @ResolverFacade proxy method first parameter have "
            + "is not a String (property name)")
    void validationTest7() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(InvalidFirstArgTypeProxyInterface.class));
    }

    @Test
    @DisplayName(
        "should throw when @ResolverFacade proxy method second parameter "
            + "is not the target type")
    void validationTest8() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(InvalidSecondArgTypeProxyInterface.class));
    }

    @Test
    @DisplayName(
        "should throw when @ConverterFacade proxy method have invalid " + "method parameter types")
    void validationTest9() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(InvalidArgsConverterFacadeProxyInterface.class));
    }

    @Test
    @DisplayName("should throw when @ConverterFacade proxy method has no" + "method parameters")
    void validationTest10() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(NoArgsConverterFacadeProxyInterface.class));
    }

    @Test
    @DisplayName(
        "should throw when @ConverterFacade proxy method have more than" + "2 method parameters")
    void validationTest11() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  MoreThanTwoArgsConverterFacadeProxyInterface.class));
    }

    @Test
    @DisplayName(
        "should throw when @ConverterFacade proxy method first parameter "
            + "is not a String (value to convert)")
    void validationTest12() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidFirstArgTypeConverterFacadeProxyInterface.class));
    }

    @Test
    @DisplayName(
        "should throw when @ConverterFacade proxy method second parameter "
            + "is not the target type")
    void validationTest13() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidSecondArgTypeConverterFacadeProxyInterface.class));
    }

    @Test
    @DisplayName("should throw when exclusive annotations are found in proxy method")
    void validationTest14() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(ExclusiveProxyInterface.class));
    }

    @Test
    @DisplayName("should initialize a proxy that handles @ExternalizedProperty annotations")
    void test1() {
      StubResolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ProxyInterface proxy = externalizedProperties.initialize(ProxyInterface.class);

      assertNotNull(proxy);
      assertTrue(proxy instanceof Proxy);

      assertEquals(resolver.delegate().apply("property"), proxy.property());
    }

    @Test
    @DisplayName("should initialize a proxy that handles @ResolverFacade annotations")
    void test2() {
      // Always returns "1".
      StubResolver resolver = new StubResolver(pn -> "1");

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ResolverFacadeProxyInterface proxy =
          externalizedProperties.initialize(ResolverFacadeProxyInterface.class);

      assertNotNull(proxy);
      assertTrue(proxy instanceof Proxy);

      assertEquals(resolver.delegate().apply("property"), proxy.resolve("property"));

      assertEquals(1, proxy.resolve("int.property", new TypeReference<Integer>() {}));

      assertEquals(1, proxy.resolve("int.property", Integer.class));

      assertEquals(1, proxy.resolve("int.property", (Type) Integer.class));
    }

    @Test
    @DisplayName("should initialize a proxy that handles @ConverterFacade annotations")
    void test3() {
      Resolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ConverterFacadeProxyInterface proxy =
          externalizedProperties.initialize(ConverterFacadeProxyInterface.class);

      assertNotNull(proxy);
      assertTrue(proxy instanceof Proxy);

      // With target type reference
      assertEquals(
          1, (Integer) proxy.convertToTargetTypeReference("1", new TypeReference<Integer>() {}));

      // With target class
      assertEquals(1, (Integer) proxy.convertToTargetClass("1", Integer.class));

      // With target type
      assertEquals(1, (Integer) proxy.convertToTargetType("1", (Type) Integer.class));
    }

    @Test
    @DisplayName(
        "should throw ClassCastException when value converted to target type reference "
            + "is not assignable to the proxy method return type")
    void test4() {
      Resolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ReturnTypeMismatchConverterFacadeProxyInterface proxy =
          externalizedProperties.initialize(ReturnTypeMismatchConverterFacadeProxyInterface.class);

      // Target type is List<String> but proxy interface method
      // return type is Integer.
      TypeReference<List<String>> targetType = new TypeReference<List<String>>() {};
      assertThrows(
          ClassCastException.class, () -> proxy.convertToTargetTypeReference("1,2,3", targetType));
    }

    @Test
    @DisplayName(
        "should throw ClassCastException when value converted to target class "
            + "is not assignable to the proxy method return type")
    void test5() {
      Resolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ReturnTypeMismatchConverterFacadeProxyInterface proxy =
          externalizedProperties.initialize(ReturnTypeMismatchConverterFacadeProxyInterface.class);

      // Target type is List but proxy interface method
      // return type is Integer.
      assertThrows(ClassCastException.class, () -> proxy.convertToTargetClass("1,2,3", List.class));
    }

    @Test
    @DisplayName(
        "should throw ClassCastException when value converted to target type "
            + "is not assignable to the proxy method return type")
    void test6() {
      Resolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ReturnTypeMismatchConverterFacadeProxyInterface proxy =
          externalizedProperties.initialize(ReturnTypeMismatchConverterFacadeProxyInterface.class);

      // Target type is List<String> but proxy interface method
      // return type is Integer.
      Type targetType = new TypeReference<List<String>>() {}.type();
      assertThrows(ClassCastException.class, () -> proxy.convertToTargetType("1,2,3", targetType));
    }

    @Test
    @DisplayName(
        "should throw ClassCastException when value converted to target type"
            + "is not assignable to the proxy method return type variable")
    void test7() {
      Resolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ReturnTypeMismatchConverterFacadeProxyInterface proxy =
          externalizedProperties.initialize(ReturnTypeMismatchConverterFacadeProxyInterface.class);

      // Target type is List<String> but proxy interface method
      // return type is Integer.
      Type targetType = new TypeReference<List<String>>() {}.type();
      assertThrows(
          ClassCastException.class,
          () -> {
            // Should throw ClassCastException.
            // Value is a List<String> but return variable was resolved to Integer.
            Integer mismatch =
                proxy.convertToTargetTypeWithTypeVariableReturnType("1,2,3", targetType);

            fail("Did not throw. Result value: " + mismatch);
          });
    }

    @Test
    @DisplayName("should initialize a proxy that handles @VariableExpanderFacade annotations")
    void test8() {
      StubResolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      VariableExpanderFacadeProxyInterface proxy =
          externalizedProperties.initialize(VariableExpanderFacadeProxyInterface.class);

      assertNotNull(proxy);
      assertTrue(proxy instanceof Proxy);

      assertEquals(
          resolver.delegate().apply("java.version"), proxy.expandVariables("${java.version}"));
    }

    @Test
    @DisplayName(
        "should throw when @VariableExpanderFacade proxy method does not have "
            + "any method parameters")
    void test9() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(NoArgsVariableExpanderFacadeProxyInterface.class));
    }

    @Test
    @DisplayName(
        "should throw when @VariableExpanderFacade proxy method have more than"
            + "1 method parameters")
    void test10() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidNumberOfArgsVariableExpanderFacadeProxyInterface.class));
    }

    @Test
    @DisplayName(
        "should throw when @VariableExpanderFacade proxy method have an invalid" + "parameter type")
    void test11() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidArgTypeVariableExpanderFacadeProxyInterface.class));
    }

    @Test
    @DisplayName(
        "should throw when @VariableExpanderFacade proxy method have an invalid" + "return type")
    void test12() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidReturnTypeVariableExpanderFacadeProxyInterface.class));
    }
  }

  @Nested
  class InitializeMethodWithClassLoaderOverload {
    @Test
    @DisplayName("should throw when proxy interface argument is null")
    void validationTest1() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());
      ClassLoader classLoader = getClass().getClassLoader();

      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(null, classLoader));
    }

    @Test
    @DisplayName("should throw when class loader argument is null")
    void validationTest2() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(ProxyInterface.class, null));
    }

    @Test
    @DisplayName("should throw when proxy interface argument is not an interface")
    void validationTest3() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  SystemExternalizedPropertiesTests.class, classLoader));
    }

    @Test
    @DisplayName("should throw when proxy interface contains void-returning methods")
    void validationTest4() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(VoidReturnTypeProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName("should throw when proxy interface contains Void-returning methods")
    void validationTest5() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  VoidClassReturnTypeProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName("should throw when @ResolverFacade proxy method have no" + "method parameters")
    void validationTest6() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () -> externalizedProperties.initialize(NoArgProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName(
        "should throw when @ResolverFacade proxy method have more than" + "2 method parameters")
    void validationTest7() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(MoreThanTwoArgsProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName(
        "should throw when @ResolverFacade proxy method first parameter "
            + "is not a String (property name)")
    void validationTest8() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidFirstArgTypeProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName(
        "should throw when @ResolverFacade proxy method second parameter "
            + "is not the target type")
    void validationTest9() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidSecondArgTypeProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName(
        "should throw when @ConverterFacade proxy method have invalid " + "method parameter types")
    void validationTest10() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidArgsConverterFacadeProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName("should throw when @ConverterFacade proxy method have no " + "method parameters")
    void validationTest11() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  NoArgsConverterFacadeProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName(
        "should throw when @ConverterFacade proxy method have more than" + "2 method parameters")
    void validationTest12() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  MoreThanTwoArgsConverterFacadeProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName(
        "should throw when @ConverterFacade proxy method first parameter "
            + "is not a String (value to convert)")
    void validationTest13() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidFirstArgTypeConverterFacadeProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName(
        "should throw when @ConverterFacade proxy method second parameter "
            + "is not the target type")
    void validationTest14() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidSecondArgTypeConverterFacadeProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName("should initialize a proxy that handles @ExternalizedProperty annotations")
    void test1() {
      StubResolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ProxyInterface proxy =
          externalizedProperties.initialize(ProxyInterface.class, getClass().getClassLoader());

      assertNotNull(proxy);
      assertTrue(proxy instanceof Proxy);

      assertEquals(resolver.delegate().apply("property"), proxy.property());
    }

    @Test
    @DisplayName("should initialize a proxy that handles @ResolverFacade annotations")
    void test2() {
      // Always returns "1".
      StubResolver resolver = new StubResolver(pn -> "1");

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ResolverFacadeProxyInterface proxy =
          externalizedProperties.initialize(
              ResolverFacadeProxyInterface.class, getClass().getClassLoader());

      assertNotNull(proxy);
      assertTrue(proxy instanceof Proxy);

      assertEquals(resolver.delegate().apply("property"), proxy.resolve("property"));

      assertEquals(1, proxy.resolve("property", new TypeReference<Integer>() {}));

      assertEquals(1, proxy.resolve("property", Integer.class));

      assertEquals(1, proxy.resolve("property", (Type) Integer.class));
    }

    @Test
    @DisplayName("should initialize a proxy that handles @ConverterFacade annotations")
    void test3() {
      Resolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ConverterFacadeProxyInterface proxy =
          externalizedProperties.initialize(
              ConverterFacadeProxyInterface.class, getClass().getClassLoader());

      assertNotNull(proxy);
      assertTrue(proxy instanceof Proxy);

      assertEquals(1, proxy.convertToInt("1"));

      // With target type reference
      assertEquals(
          1, (Integer) proxy.convertToTargetTypeReference("1", new TypeReference<Integer>() {}));

      // With target class
      assertEquals(1, (Integer) proxy.convertToTargetClass("1", Integer.class));

      // With target type
      assertEquals(1, (Integer) proxy.convertToTargetType("1", (Type) Integer.class));
    }

    @Test
    @DisplayName(
        "should throw ClassCastException when value converted to target type reference "
            + "is not assignable to the proxy method return type")
    void test4() {
      Resolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ReturnTypeMismatchConverterFacadeProxyInterface proxy =
          externalizedProperties.initialize(
              ReturnTypeMismatchConverterFacadeProxyInterface.class, getClass().getClassLoader());

      // Target type is List<String> but proxy interface method
      // return type is Integer.
      TypeReference<List<String>> targetType = new TypeReference<List<String>>() {};
      assertThrows(
          ClassCastException.class, () -> proxy.convertToTargetTypeReference("1,2,3", targetType));
    }

    @Test
    @DisplayName(
        "should throw ClassCastException when value converted to target class "
            + "is not assignable to the proxy method return type")
    void test5() {
      Resolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ReturnTypeMismatchConverterFacadeProxyInterface proxy =
          externalizedProperties.initialize(
              ReturnTypeMismatchConverterFacadeProxyInterface.class, getClass().getClassLoader());

      // Target type is List but proxy interface method
      // return type is Integer.
      assertThrows(ClassCastException.class, () -> proxy.convertToTargetClass("1,2,3", List.class));
    }

    @Test
    @DisplayName(
        "should throw ClassCastException when value converted to target type "
            + "is not assignable to the proxy method return type")
    void test6() {
      Resolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ReturnTypeMismatchConverterFacadeProxyInterface proxy =
          externalizedProperties.initialize(
              ReturnTypeMismatchConverterFacadeProxyInterface.class, getClass().getClassLoader());

      // Target type is List<String> but proxy interface method
      // return type is Integer.
      Type targetType = new TypeReference<List<String>>() {}.type();
      assertThrows(ClassCastException.class, () -> proxy.convertToTargetType("1,2,3", targetType));
    }

    @Test
    @DisplayName(
        "should throw ClassCastException when value converted to target type"
            + "is not assignable to the proxy method return type variable")
    void test7() {
      Resolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      ReturnTypeMismatchConverterFacadeProxyInterface proxy =
          externalizedProperties.initialize(
              ReturnTypeMismatchConverterFacadeProxyInterface.class, getClass().getClassLoader());

      // Target type is List<String> but proxy interface method
      // return type is Integer.
      Type targetType = new TypeReference<List<String>>() {}.type();
      assertThrows(
          ClassCastException.class,
          () -> {
            // Should throw ClassCastException.
            // Value is a List<String> but return variable was resolved to Integer.
            Integer mismatch =
                proxy.convertToTargetTypeWithTypeVariableReturnType("1,2,3", targetType);

            fail("Did not throw. Result value: " + mismatch);
          });
    }

    @Test
    @DisplayName("should initialize a proxy that handles @VariableExpanderFacade annotations")
    void test8() {
      StubResolver resolver = new StubResolver();

      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(resolver);

      VariableExpanderFacadeProxyInterface proxy =
          externalizedProperties.initialize(
              VariableExpanderFacadeProxyInterface.class, getClass().getClassLoader());

      assertNotNull(proxy);
      assertTrue(proxy instanceof Proxy);

      assertEquals(resolver.delegate().apply("property"), proxy.expandVariables("${property}"));
    }

    @Test
    @DisplayName(
        "should throw when @VariableExpanderFacade proxy method does not have "
            + "any method parameters")
    void test9() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  NoArgsVariableExpanderFacadeProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName(
        "should throw when @VariableExpanderFacade proxy method have more than"
            + "1 method parameters")
    void test10() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidNumberOfArgsVariableExpanderFacadeProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName(
        "should throw when @VariableExpanderFacade proxy method have an invalid" + "parameter type")
    void test11() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidArgTypeVariableExpanderFacadeProxyInterface.class, classLoader));
    }

    @Test
    @DisplayName(
        "should throw when @VariableExpanderFacade proxy method have an invalid" + "return type")
    void test12() {
      SystemExternalizedProperties externalizedProperties =
          internalExternalizedProperties(new StubResolver());

      ClassLoader classLoader = getClass().getClassLoader();
      assertThrows(
          IllegalArgumentException.class,
          () ->
              externalizedProperties.initialize(
                  InvalidReturnTypeVariableExpanderFacadeProxyInterface.class, classLoader));
    }
  }

  private static SystemExternalizedProperties internalExternalizedProperties(
      Resolver resolverToUse) {
    RootResolver rootResolver = new RootResolver(Arrays.asList(resolverToUse), new RootProcessor());

    RootConverter rootConverter = new RootConverter(new DefaultConverter());

    SimpleVariableExpander variableExpander = new SimpleVariableExpander();

    return new SystemExternalizedProperties(
        rootResolver,
        rootConverter,
        variableExpander,
        (proxyInterface, rr, rc, ve, pmf) ->
            new ExternalizedPropertiesInvocationHandler(rr, rc, ve, pmf));
  }

  private static interface NoArgProxyInterface {
    // Must have 1 or 2 parameters: The property name and the target type.
    @ResolverFacade
    String resolve();
  }

  private static interface MoreThanTwoArgsProxyInterface {
    // Must have 1 or 2 parameters: The property name and the target type.
    @ResolverFacade
    <T> T resolve(String propertyName, Class<T> targetType, int invalidArg);
  }

  private static interface InvalidFirstArgTypeProxyInterface {
    // Must have 1 or 2 parameters: The property name and the target type.
    @ResolverFacade
    String resolve(int invalidMustBeString);
  }

  private static interface InvalidSecondArgTypeProxyInterface {
    // Must have 1 or 2 parameters: The property name and the target type.
    @ResolverFacade
    String resolve(String propertyName, int invalidMustBeTargetType);
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property")
    String property();
  }

  private static interface VoidReturnTypeProxyInterface {
    // Invalid: Void return types not allowed.
    @ExternalizedProperty("test.invalid.method.void")
    void invalidVoidMethod();
  }

  private static interface VoidClassReturnTypeProxyInterface {
    // Invalid: Void return types not allowed.
    @ExternalizedProperty("test.invalid.method.void")
    Void invalidVoidClassMethod();
  }

  private static interface ResolverFacadeProxyInterface {
    @ResolverFacade
    String resolve(String propertyName);

    @ResolverFacade
    <T> T resolve(String propertyName, TypeReference<T> targetType);

    @ResolverFacade
    <T> T resolve(String propertyName, Class<T> targetType);

    @ResolverFacade
    Object resolve(String propertyName, Type targetType);
  }

  private static interface ConverterFacadeProxyInterface {
    @ConverterFacade
    <T> T convertToTargetTypeReference(String valueToConvert, TypeReference<T> targetType);

    @ConverterFacade
    <T> T convertToTargetClass(String valueToConvert, Class<T> targetType);

    @ConverterFacade
    <T> T convertToTargetType(String valueToConvert, Type targetType);

    @ConverterFacade
    int convertToInt(String valueToConvert);
  }

  private static interface InvalidArgsConverterFacadeProxyInterface {
    // First argument must be a String.
    // Second argument must be one of the ff: TypeReference, Class, Type
    @ConverterFacade
    <T> T convert(Integer mustBeString, Double mustBeTargetType);
  }

  private static interface NoArgsConverterFacadeProxyInterface {
    // Must have 1 or 2 parameters: The value to convert and the target type.
    @ConverterFacade
    <T> T convert();
  }

  private static interface MoreThanTwoArgsConverterFacadeProxyInterface {
    // Must have 1 or 2 parameters: The value to convert and the target type.
    @ConverterFacade
    <T> T convert(String valueToConvert, Class<T> targetType, String invalidArg);
  }

  private static interface InvalidFirstArgTypeConverterFacadeProxyInterface {
    // First parameter must be the value to convert (String)
    @ConverterFacade
    <T> T convert(Integer mustBeString, Class<T> targetType);
  }

  private static interface InvalidSecondArgTypeConverterFacadeProxyInterface {
    // Second parameter must be the target type.
    // Second argument must be one of the ff: TypeReference, Class, Type
    @ConverterFacade
    <T> T convert(String valueToConvert, Integer mustBeTargetType);
  }

  private static interface ReturnTypeMismatchConverterFacadeProxyInterface {
    // Return type not assignable with target type.
    @ConverterFacade
    Integer convertToTargetTypeReference(String valueToConvert, TypeReference<?> targetType);

    // Return type not assignable with target type.
    @ConverterFacade
    Integer convertToTargetClass(String valueToConvert, Class<?> targetType);

    // Return type not assignable with target type.
    @ConverterFacade
    Integer convertToTargetType(String valueToConvert, Type targetType);

    // Return type not assignable with target type.
    @ConverterFacade
    <T> T convertToTargetTypeWithTypeVariableReturnType(String valueToConvert, Type targetType);
  }

  private static interface VariableExpanderFacadeProxyInterface {
    @VariableExpanderFacade
    String expandVariables(String value);
  }

  private static interface InvalidReturnTypeVariableExpanderFacadeProxyInterface {
    // Return type must be String.
    @VariableExpanderFacade
    int expandVariables(String value);
  }

  private static interface InvalidArgTypeVariableExpanderFacadeProxyInterface {
    // Must have 1 String argument.
    @VariableExpanderFacade
    String expandVariables(int value);
  }

  private static interface NoArgsVariableExpanderFacadeProxyInterface {
    // Must have 1 String argument.
    @VariableExpanderFacade
    String expandVariables();
  }

  private static interface InvalidNumberOfArgsVariableExpanderFacadeProxyInterface {
    // Must only have 1 argument (String).
    @VariableExpanderFacade
    int expandVariables(String value, String mustOnlyBeOneArg);
  }

  private static interface ExclusiveProxyInterface {
    @ExternalizedProperty("property+resolverfacade")
    @ResolverFacade
    String exclusive1();

    @ExternalizedProperty("property+converterfacade")
    @ConverterFacade
    String exclusive2();

    @ExternalizedProperty("property+variableexpanderfacade")
    @VariableExpanderFacade
    String exclusive3();

    @ResolverFacade
    @ConverterFacade
    String exclusive4(String propertyName);

    @ResolverFacade
    @VariableExpanderFacade
    String exclusive5(String propertyName);

    @ConverterFacade
    @VariableExpanderFacade
    String property5(String valueToConvert, Class<?> targetType);
  }
}
