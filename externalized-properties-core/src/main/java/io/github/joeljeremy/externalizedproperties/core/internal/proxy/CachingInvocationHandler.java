package io.github.joeljeremy.externalizedproperties.core.internal.proxy;

import static io.github.joeljeremy.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy.externalizedproperties.core.internal.Internal;
import io.github.joeljeremy.externalizedproperties.core.internal.InvocationCacheKey;
import io.github.joeljeremy.externalizedproperties.core.internal.caching.WeakConcurrentHashMapCacheStrategy;
import io.github.joeljeremy.externalizedproperties.core.internal.caching.WeakHashMapCacheStrategy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Optional;

/** Implementation of {@link InvocationHandler} that caches invocation results. */
@Internal
public class CachingInvocationHandler implements InvocationHandler {

  private final InvocationHandler decorated;
  private final CacheStrategy<InvocationCacheKey, Object> cacheStrategy;

  /**
   * Constructor.
   *
   * @apiNote It is recommended that the {@link CacheStrategy} implementation only holds weak
   *     references to the {@link Method} key in order to avoid leaks and class unloading issues.
   * @param decorated The decorated {@link InvocationHandler} instance.
   * @param cacheStrategy The cache strategy keyed by a {@link InvocationCacheKey} and whose values
   *     are the resolved properties. It is recommended that the {@link CacheStrategy}
   *     implementation only holds weak references to the {@link InvocationCacheKey} due to it
   *     holding a reference to the invoked {@link Method}. This is in order to avoid possible leaks
   *     and class unloading issues.
   * @see WeakConcurrentHashMapCacheStrategy
   * @see WeakHashMapCacheStrategy
   */
  public CachingInvocationHandler(
      InvocationHandler decorated, CacheStrategy<InvocationCacheKey, Object> cacheStrategy) {
    this.decorated = requireNonNull(decorated, "decorated");
    this.cacheStrategy = requireNonNull(cacheStrategy, "cacheStrategy");
  }

  /** {@inheritDoc} */
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    InvocationCacheKey cacheKey = new InvocationCacheKey(method, args);
    Optional<?> cached = cacheStrategy.get(cacheKey);
    if (cached.isPresent()) {
      return cached.get();
    }
    return getAndCache(cacheKey, proxy, method, args);
  }

  private Object getAndCache(
      InvocationCacheKey cacheKey, Object proxy, Method method, Object[] args) {
    try {
      Object result = decorated.invoke(proxy, method, args);
      if (result != null) {
        cacheStrategy.cache(cacheKey, result);
      }
      return result;
    } catch (Throwable e) {
      throw new ExternalizedPropertiesException(
          "Error occurred while invoking decorated invocation handler.", e);
    }
  }
}
