package io.github.joeljeremy.externalizedproperties.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.testfixtures.MethodUtils;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Nested
class InvocationCacheKeyTests {
  @Nested
  class HashCodeMethod {
    @Test
    @DisplayName("should return the same of hash code everytime")
    void test1() {
      Method method = stubMethod();
      Object[] args = new Object[] {"1", Class.class};
      InvocationCacheKey cacheKey = new InvocationCacheKey(method, args);
      int hashCode1 = cacheKey.hashCode();
      int hashCode2 = cacheKey.hashCode();

      assertEquals(hashCode1, hashCode2);
    }

    @Test
    @DisplayName(
        "should return the same of hash code for matching methods " + "and args combinations")
    void test2() {
      Method method1 = stubMethod();
      Method method2 = stubMethod();
      Object[] args1 = new Object[] {"1", Class.class};
      Object[] args2 = new Object[] {"1", Class.class};
      InvocationCacheKey cacheKey1 = new InvocationCacheKey(method1, args1);
      InvocationCacheKey cacheKey2 = new InvocationCacheKey(method2, args2);
      assertEquals(cacheKey1.hashCode(), cacheKey2.hashCode());
    }

    @Test
    @DisplayName("should return the same of hash code for matching methods " + "(no args)")
    void test3() {
      Method method1 = stubMethod();
      Method method2 = stubMethod();
      InvocationCacheKey cacheKey1 = new InvocationCacheKey(method1);
      InvocationCacheKey cacheKey2 = new InvocationCacheKey(method2);
      assertEquals(cacheKey1.hashCode(), cacheKey2.hashCode());
    }

    @Test
    @DisplayName("should return a different hash code for different args combinations")
    void test4() {
      Method method = stubMethod();
      Object[] args1 = new Object[] {"1", Class.class};
      Object[] args2 = new Object[] {"2", Class.class};
      InvocationCacheKey cacheKey1 = new InvocationCacheKey(method, args1);
      InvocationCacheKey cacheKey2 = new InvocationCacheKey(method, args2);
      int hashCode1 = cacheKey1.hashCode();
      int hashCode2 = cacheKey2.hashCode();

      assertNotEquals(hashCode1, hashCode2);
    }
  }

  @Nested
  class EqualsMethod {
    @Test
    @DisplayName("should return true when method and args are all equal")
    void test1() {
      Method method1 = stubMethod();
      Method method2 = stubMethod();
      Object[] args1 = new Object[] {"1", Class.class};
      Object[] args2 = new Object[] {"1", Class.class};
      InvocationCacheKey cacheKey1 = new InvocationCacheKey(method1, args1);
      InvocationCacheKey cacheKey2 = new InvocationCacheKey(method2, args2);

      assertTrue(cacheKey1.equals(cacheKey2));
    }

    @Test
    @DisplayName("should return true when method and args are all equal " + "(args has an array)")
    void test2() {
      Method method1 = stubMethod();
      Method method2 = stubMethod();
      Object[] args1 =
          new Object[] {"1", new Object[] {Class.class, Integer.class, InvocationCacheKey.class}};
      Object[] args2 =
          new Object[] {"1", new Object[] {Class.class, Integer.class, InvocationCacheKey.class}};
      InvocationCacheKey cacheKey1 = new InvocationCacheKey(method1, args1);
      InvocationCacheKey cacheKey2 = new InvocationCacheKey(method2, args2);

      assertTrue(cacheKey1.equals(cacheKey2));
    }

    @Test
    @DisplayName("should return false when methods are not equal (no args)")
    void test3() {
      Method method =
          MethodUtils.getMethod(StubProxyInterface.class, StubProxyInterface::methodName);
      Method otherMethod =
          MethodUtils.getMethod(OtherProxyInterface.class, OtherProxyInterface::methodName);
      InvocationCacheKey cacheKey1 = new InvocationCacheKey(method);
      InvocationCacheKey cacheKey2 = new InvocationCacheKey(otherMethod);

      assertFalse(cacheKey1.equals(cacheKey2));
    }

    @Test
    @DisplayName("should return false args are not all equal")
    void test4() {
      Method method1 = stubMethod();
      Method method2 = stubMethod();
      Object[] args1 = new Object[] {"1", new Object[] {Class.class, Integer.class, String.class}};
      Object[] args2 =
          new Object[] {"1", new Object[] {Class.class, Integer.class, InvocationCacheKey.class}};
      InvocationCacheKey cacheKey1 = new InvocationCacheKey(method1, args1);
      InvocationCacheKey cacheKey2 = new InvocationCacheKey(method2, args2);

      assertFalse(cacheKey1.equals(cacheKey2));
    }

    @Test
    @DisplayName("should return false when other object is not an invocation cache key")
    void test5() {
      Method method =
          MethodUtils.getMethod(StubProxyInterface.class, StubProxyInterface::methodName);
      InvocationCacheKey cacheKey1 = new InvocationCacheKey(method);

      assertFalse(cacheKey1.equals(new Object()));
    }

    @Test
    @DisplayName("should return false when other object is null")
    void test6() {
      Method method =
          MethodUtils.getMethod(StubProxyInterface.class, StubProxyInterface::methodName);
      InvocationCacheKey cacheKey1 = new InvocationCacheKey(method);

      assertFalse(cacheKey1.equals(null));
    }
  }

  private static Method stubMethod() {
    return MethodUtils.getMethod(StubProxyInterface.class, StubProxyInterface::methodName);
  }

  private static interface StubProxyInterface {
    String methodName();
  }

  private static interface OtherProxyInterface {
    String methodName();
  }
}
