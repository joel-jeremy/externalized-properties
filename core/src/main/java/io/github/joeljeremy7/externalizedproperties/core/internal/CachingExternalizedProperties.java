package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.internal.caching.WeakConcurrentHashMapCacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.internal.caching.WeakHashMapCacheStrategy;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * {@link ExternalizedProperties} decorator to enable property resolution caching.
 */
public class CachingExternalizedProperties implements ExternalizedProperties {

    private final ExternalizedProperties decorated;
    private final CacheStrategy<ClassLoader, ClassValue<?>> cacheStrategy;

    /**
     * Constructor.
     * 
     * @param decorated The decorated {@link ExternalizedProperties} instance.
     * @param cacheStrategy The cache strategy keyed by a {@link ClassLoader} and whose values
     * are the {@link ClassValue}s associated to that classloader. It is recommended that the 
     * {@link CacheStrategy} implementation only holds weak references to the {@link ClassLoader} 
     * key in order to avoid leaks and class unloading issues.
     * 
     * @see WeakConcurrentHashMapCacheStrategy
     * @see WeakHashMapCacheStrategy
     */
    public CachingExternalizedProperties(
            ExternalizedProperties decorated,
            CacheStrategy<ClassLoader, ClassValue<?>> cacheStrategy
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.cacheStrategy = requireNonNull(cacheStrategy, "cacheStrategy");
    }

    /** {@inheritDoc} */
    @Override
    public <T> T initialize(Class<T> proxyInterface) {
        // Use proxy interface's class loader as done by ExternalizedProperties.
        ClassLoader classLoader = proxyInterface.getClassLoader();

        ClassValue<?> proxyInterfaceClassValue = cacheStrategy.get(classLoader)
            .orElseGet(() -> {
                ClassValue<?> picv = proxyInterfaceClassValue(decorated);
                cacheStrategy.cache(classLoader, picv);
                return picv;
            });

        @SuppressWarnings("unchecked")
        T proxy = (T)proxyInterfaceClassValue.get(proxyInterface);
        return proxy;
    }

    /** {@inheritDoc} */
    @Override
    public <T> T initialize(Class<T> proxyInterface, ClassLoader classLoader) {
        ClassValue<?> proxyInterfaceClassValue = cacheStrategy.get(classLoader)
            .orElseGet(() -> {
                ClassValue<?> picv = 
                    proxyInterfaceClassValueWithClassLoader(decorated, classLoader);
                cacheStrategy.cache(classLoader, picv);
                return picv;
            });

        @SuppressWarnings("unchecked")
        T proxy = (T)proxyInterfaceClassValue.get(proxyInterface);
        return proxy;
    }

    private ClassValue<?> proxyInterfaceClassValue(ExternalizedProperties decorated) {
        return new ClassValue<Object>() {
            @Override
            protected Object computeValue(Class<?> proxyInterface) {
                return decorated.initialize(proxyInterface);
            }
        };
    }

    private ClassValue<?> proxyInterfaceClassValueWithClassLoader(
            ExternalizedProperties decorated,
            ClassLoader classLoader
    ) {
        return new ClassValue<Object>() {
            @Override
            protected Object computeValue(Class<?> proxyInterface) {
                return decorated.initialize(proxyInterface, classLoader);
            }
        };
    }
}
