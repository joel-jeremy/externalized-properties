package io.github.joeljeremy7.externalizedproperties.core.testfixtures;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.internal.InvocationContextFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Utility methods to manage {@link ProxyMethod}s.
 */
public class InvocationContextUtils {
    private InvocationContextUtils(){}

    /**
     * Create a test factory for creating {@link InvocationContext} instances.
     * 
     * @param <T> The type of the proxy interface.
     * @param proxyInterface The proxy interface.
     * @return A test factory for creating {@link InvocationContext} instances.
     */
    public static <T> InvocationContextTestFactory<T> testFactory(Class<T> proxyInterface) {
        return new InvocationContextTestFactory<>(proxyInterface);
    }

    /**
     * Get a {@link ProxyMethod} instance from a proxy interface's method reference.
     * 
     * <p>This will only work on interfaces.</p>
     * 
     * <p>Example:</p>
     * <blockquote><pre>
     *{@code // Proxy method which represents AppPropertiesProxy::propertyMethod}
     *{@code // Example matching method signature (no arguments): String propertyMethod()}
     *InvocationContext context = InvocationContextUtils.fromMethodReference(
     *    AppPropertiesProxy.class, 
     *    AppPropertiesProxy::propertyMethod
     *);
     * </blockquote></pre>
     * 
     * @param <TProxyInterface> The proxy interface.
     * @param <R> The method reference return type.
     * @param proxyInterface The proxy interface which declared the method to be referenced.
     * @param methodReference The method reference to extract the method from.
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @return The {@link InvocationContext} instance.
     */
    public static <TProxyInterface, R> InvocationContext fromMethodReference(
            Class<TProxyInterface> proxyInterface,
            MethodReference<TProxyInterface, R> methodReference,
            ExternalizedProperties externalizedProperties
    ) {
        return fromMethodReference(
            proxyInterface, 
            proxy -> { 
                // Invoked on proxy instance.
                // Ignore return value.
                methodReference.ref(proxy); 
            },
            externalizedProperties
        );
    }

    /**
     * Get a {@link ProxyMethod} instance from a proxy interface's method reference.
     * 
     * <p>This will only work on interfaces.</p>
     * 
     * <p>Example:</p>
     * <blockquote><pre>
     *{@code // Proxy method which represents AppPropertiesProxy::propertyMethodWithOneArg}
     *{@code // Example matching method signature (one argument):}
     *{@code // String propertyMethodWithOneArg(String arg1)}
     *InvocationContext context = InvocationContextUtils.fromMethodReference(
     *    AppPropertiesProxy.class, 
     *    AppPropertiesProxy::propertyMethodWithOneArg
     *);
     * </blockquote></pre>
     * 
     * @param <TProxyInterface> The proxy interface.
     * @param <TArg1> The type of first argument of the method reference.
     * @param <R> The method reference return type.
     * @param proxyInterface The proxy interface which declared the method to be referenced.
     * @param methodReference The method reference to extract the method from.
     * @param arg1 The first invocation argument.
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @return The {@link InvocationContext} instance.
     */
    public static <TProxyInterface, TArg1, R> InvocationContext fromMethodReference(
            Class<TProxyInterface> proxyInterface,
            MethodReference.WithOneArg<TProxyInterface, TArg1, R> methodReference,
            TArg1 arg1,
            ExternalizedProperties externalizedProperties
    ) {
        return fromMethodReference(
            proxyInterface, 
            proxy -> { 
                // Invoked on proxy instance.
                // Ignore return value.
                methodReference.ref(proxy, arg1); 
            },
            externalizedProperties
        );
    }

    /**
     * Get a {@link ProxyMethod} instance from a proxy interface's method reference.
     * 
     * <p>This will only work on interfaces.</p>
     * 
     * <p>Example:</p>
     * <blockquote><pre>
     *{@code // Proxy method which represents AppPropertiesProxy::propertyMethodWithTwoArgs}
     *{@code // Example matching method signature (two arguments):}
     *{@code // String propertyMethodWithTwoArgs(String arg1, String arg2)}
     *InvocationContext context = InvocationContextUtils.fromMethodReference(
     *    AppPropertiesProxy.class, 
     *    AppPropertiesProxy::propertyMethodWithTwoArgs
     *);
     * </blockquote></pre>
     * 
     * @param <TProxyInterface> The proxy interface.
     * @param <TArg1> The type of first argument of the method reference.
     * @param <TArg2> The type of second argument of the method reference.
     * @param <R> The method reference return type.
     * @param proxyInterface The proxy interface which declared the method to be referenced.
     * @param methodReference The method reference to extract the method from.
     * @param arg1 The first invocation argument.
     * @param arg2 The second invocation argument.
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @return The {@link InvocationContext} instance.
     */
    public static <TProxyInterface, TArg1, TArg2, R> InvocationContext fromMethodReference(
            Class<TProxyInterface> proxyInterface,
            MethodReference.WithTwoArgs<TProxyInterface, TArg1, TArg2, R> methodReference,
            TArg1 arg1,
            TArg2 arg2,
            ExternalizedProperties externalizedProperties
    ) {
        return fromMethodReference(
            proxyInterface, 
            proxy -> { 
                // Invoked on proxy instance.
                // Ignore return value.
                methodReference.ref(proxy, arg1, arg2); 
            },
            externalizedProperties
        );
    }

    /**
     * Get a {@link ProxyMethod} instance from a proxy interface's method reference.
     * 
     * <p>This will only work on interfaces.</p>
     * 
     * <p>Example:</p>
     * <blockquote><pre>
     *{@code // Proxy method which represents AppPropertiesProxy::propertyMethodWithThreeArgs}
     *{@code // Example matching method signature (three arguments):}
     *{@code // String propertyMethodWithThreeArgs(String arg1, String arg2, String arg3)}
     *InvocationContext context = InvocationContextUtils.fromMethodReference(
     *    AppPropertiesProxy.class, 
     *    AppPropertiesProxy::propertyMethodWithThreeArgs
     *);
     * </blockquote></pre>
     * 
     * @param <TProxyInterface> The proxy interface.
     * @param <TArg1> The type of first argument of the method reference.
     * @param <TArg2> The type of second argument of the method reference.
     * @param <TArg3> The type of third argument of the method reference.
     * @param <R> The method reference return type.
     * @param proxyInterface The proxy interface which declared the method to be referenced.
     * @param methodReference The method reference to extract the method from.
     * @param arg1 The first invocation argument.
     * @param arg2 The second invocation argument.
     * @param arg3 The thord invocation argument.
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @return The {@link InvocationContext} instance.
     */
    public static <TProxyInterface, TArg1, TArg2, TArg3, R> InvocationContext fromMethodReference(
            Class<TProxyInterface> proxyInterface,
            MethodReference.WithThreeArgs<TProxyInterface, TArg1, TArg2, TArg3, R> methodReference,
            TArg1 arg1,
            TArg2 arg2,
            TArg3 arg3,
            ExternalizedProperties externalizedProperties
    ) {
        return fromMethodReference(
            proxyInterface, 
            proxy -> { 
                // Invoked on proxy instance.
                // Ignore return value.
                methodReference.ref(proxy, arg1, arg2, arg3); 
            },
            externalizedProperties
        );
    }

    private static <TProxyInterface> InvocationContext fromMethodReference(
            Class<TProxyInterface> proxyInterface,
            Consumer<TProxyInterface> invoker,
            ExternalizedProperties externalizedProperties
    ) {
        AtomicReference<Object> proxyRef = new AtomicReference<>();
        AtomicReference<Method> methodRef = new AtomicReference<>();
        AtomicReference<Object[]> argsRef = new AtomicReference<>();

        @SuppressWarnings("unchecked")
        TProxyInterface proxy = (TProxyInterface)Proxy.newProxyInstance(
            proxyInterface.getClassLoader(), 
            new Class<?>[] { proxyInterface }, 
            (p, method, args) -> {
                proxyRef.set(p);
                methodRef.set(method);
                argsRef.set(args);
                return getDefaultValue(method.getReturnType());
            }
        );

        // Invoke the method reference with the proxy instance.
        invoker.accept(proxy);

        return new InvocationContextFactory(externalizedProperties)
            .create(proxyRef.get(), methodRef.get(), argsRef.get());
    }

    private static Object getDefaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }

        if (boolean.class.equals(returnType)) {
            return false;
        } else if (byte.class.equals(returnType)) {
            return (byte)0;
        } else if (short.class.equals(returnType)) {
            return (short)0;
        } else if (int.class.equals(returnType)) {
            return 0;
        } else if (long.class.equals(returnType)) {
            return 0L;
        } else if (float.class.equals(returnType)) {
            return 0.0F;
        } else if (double.class.equals(returnType)) {
            return 0.0D;
        } else if (char.class.equals(returnType)) {
            return Character.MIN_VALUE;
        }

        throw new IllegalArgumentException("Unsupported type: " + returnType.getName());
    }

    /**
     * Invocation context factory that is specific to a proxy interface.
     */
    public static class InvocationContextTestFactory<TProxyInterface> {

        private final Class<TProxyInterface> proxyInterface;
    
        private InvocationContextTestFactory(Class<TProxyInterface> proxyInterface) {
            this.proxyInterface = proxyInterface;
        }
    
        /**
         * Get a {@link InvocationContext} instance from a proxy interface's method reference.
         * 
         * <p>This will only work on interfaces.</p>
         * 
         * <p>Example:</p>
         * <blockquote><pre>
         *{@code // Proxy method which represents AppPropertiesProxy::propertyMethod}
         *{@code // Example matching method signature (no arguments): String propertyMethod()}
         *InvocationContext context = InvocationContextUtils.fromMethodReference(
         *    AppPropertiesProxy.class, 
         *    AppPropertiesProxy::propertyMethod
         *);
         * </blockquote></pre>
         * 
         * @param <R> The method reference return type.
         * @param methodReference The method reference to extract the method from.
         * @param externalizedProperties The {@link ExternalizedProperties} instance.
         * @return The {@link InvocationContext} instance.
         */
        public <R> InvocationContext fromMethodReference(
                MethodReference<TProxyInterface, R> methodReference,
                ExternalizedProperties externalizedProperties
        ) {
            return InvocationContextUtils.fromMethodReference(
                proxyInterface, 
                methodReference, 
                externalizedProperties
            );
        }
    
        /**
         * Get a {@link InvocationContext} instance from a proxy interface's method reference.
         * 
         * <p>This will only work on interfaces.</p>
         * 
         * <p>Example:</p>
         * <blockquote><pre>
         * {@code // Proxy method which represents AppPropertiesProxy::propertyMethodWithOneArg}
         * {@code // Example matching method signature (one argument):}
         * {@code // String propertyMethodWithOneArg(String arg1)}
         * InvocationContext context = invocationContextTestFactory.fromMethodReference(
         *     AppPropertiesProxy::propertyMethodWithOneArg
         * );
         * </blockquote></pre>
         * 
         * @param <TArg1> The type of first argument of the method reference.
         * @param <R> The method reference return type.
         * @param methodReference The method reference to extract the method from.
         * @param arg1 The first invocation argument.
         * @param externalizedProperties The {@link ExternalizedProperties} instance.
         * @return The {@link InvocationContext} instance.
         */
        public <TArg1, R> InvocationContext fromMethodReference(
                MethodReference.WithOneArg<TProxyInterface, TArg1, R> methodReference,
                TArg1 arg1,
                ExternalizedProperties externalizedProperties
        ) {
            return InvocationContextUtils.fromMethodReference(
                proxyInterface, 
                methodReference, 
                arg1,
                externalizedProperties
            );
        }
    
        /**
         * Get a {@link InvocationContext} instance from a proxy interface's method reference.
         * 
         * <p>This will only work on interfaces.</p>
         * 
         * <p>Example:</p>
         * <blockquote><pre>
         * {@code // Proxy method which represents AppPropertiesProxy::propertyMethodWithTwoArgs}
         * {@code // Example matching method signature (two arguments):}
         * {@code // String propertyMethodWithTwoArgs(String arg1, String arg2)}
         * InvocationContext context = invocationContextTestFactory.fromMethodReference(
         *     AppPropertiesProxy::propertyMethodWithTwoArgs
         * );
         * </blockquote></pre>
         * 
         * @param <TArg1> The type of first argument of the method reference.
         * @param <TArg2> The type of second argument of the method reference.
         * @param <R> The method reference return type.
         * @param methodReference The method reference to extract the method from.
         * @param arg1 The first invocation argument.
         * @param arg2 The second invocation argument.
         * @param externalizedProperties The {@link ExternalizedProperties} instance.
         * @return The {@link InvocationContext} instance.
         */
        public <TArg1, TArg2, R> InvocationContext fromMethodReference(
                MethodReference.WithTwoArgs<TProxyInterface, TArg1, TArg2, R> methodReference,
                TArg1 arg1,
                TArg2 arg2,
                ExternalizedProperties externalizedProperties
        ) {
            return InvocationContextUtils.fromMethodReference(
                proxyInterface, 
                methodReference, 
                arg1,
                arg2,
                externalizedProperties
            );
        }
    
        /**
         * Get a {@link ProxyMethod} instance from a proxy interface's method reference.
         * 
         * <p>This will only work on interfaces.</p>
         * 
         * <p>Example:</p>
         * <blockquote><pre>
         * {@code // Proxy method which represents AppPropertiesProxy::propertyMethodWithThreeArgs}
         * {@code // Example matching method signature (three arguments):}
         * {@code // String propertyMethodWithThreeArgs(String arg1, String arg2, String arg3)}
         * InvocationContext context = invocationContextTestFactory.fromMethodReference(
         *     AppPropertiesProxy::propertyMethodWithThreeArgs
         * );
         * </blockquote></pre>
         * 
         * @param <TArg1> The type of first argument of the method reference.
         * @param <TArg2> The type of second argument of the method reference.
         * @param <TArg3> The type of third argument of the method reference.
         * @param <R> The method reference return type.
         * @param methodReference The method reference to extract the method from.
         * @param arg1 The first invocation argument.
         * @param arg2 The second invocation argument.
         * @param arg3 The thord invocation argument.
         * @param externalizedProperties The {@link ExternalizedProperties} instance.
         * @return The {@link InvocationContext} instance.
         */
        public <TArg1, TArg2, TArg3, R> InvocationContext fromMethodReference(
                MethodReference.WithThreeArgs<TProxyInterface, TArg1, TArg2, TArg3, R> methodReference,
                TArg1 arg1,
                TArg2 arg2,
                TArg3 arg3,
                ExternalizedProperties externalizedProperties
        ) {
            return InvocationContextUtils.fromMethodReference(
                proxyInterface, 
                methodReference,
                arg1,
                arg2,
                arg3, 
                externalizedProperties
            );
        }
    }
}