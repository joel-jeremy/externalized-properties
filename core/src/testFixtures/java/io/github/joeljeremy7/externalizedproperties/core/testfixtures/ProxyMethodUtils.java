package io.github.joeljeremy7.externalizedproperties.core.testfixtures;

import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.ProxyMethodAdapter;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Utility methods to manage {@link ProxyMethod}s.
 */
public class ProxyMethodUtils {
    private ProxyMethodUtils(){}

    /**
     * Get a {@link ProxyMethod} instance from a proxy interface's method reference.
     * 
     * <p>This will only work on interfaces.</p>
     * 
     * <p>Example:</p>
     * <blockquote><pre>
     *{@code // Proxy method which represents AppPropertiesProxy::propertyMethod}
     *{@code // Example matching method signature (no arguments): String propertyMethod()}
     *ProxyMethod proxyMethod = ProxyMethodUtils.fromMethodReference(
     *    AppPropertiesProxy.class, 
     *    AppPropertiesProxy::propertyMethod
     *);
     * </blockquote></pre>
     * 
     * @param <TProxyInterface> The proxy interface.
     * @param <R> The method reference return type.
     * @param proxyInterface The proxy interface which declared the method to be referenced.
     * @param methodReference The method reference to extract the method from.
     * @return The {@link ProxyMethod} instance.
     */
    public static <TProxyInterface, R> ProxyMethod fromMethodReference(
            Class<TProxyInterface> proxyInterface,
            ProxyMethodReference<TProxyInterface, R> methodReference
    ) {
        return fromMethodReference(proxyInterface, proxy -> { 
            // Invoked on proxy instance.
            // Ignore return value.
            methodReference.ref(proxy); 
        });
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
     *ProxyMethod proxyMethod = ProxyMethodUtils.fromMethodReference(
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
     * @return The {@link ProxyMethod} instance.
     */
    public static <TProxyInterface, TArg1, R> ProxyMethod fromMethodReference(
            Class<TProxyInterface> proxyInterface,
            ProxyMethodReference.WithOneArg<TProxyInterface, TArg1, R> methodReference
    ) {
        return fromMethodReference(proxyInterface, proxy -> { 
            // Invoked on proxy instance.
            // Ignore return value.
            methodReference.ref(proxy, null); 
        });
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
     *ProxyMethod proxyMethod = ProxyMethodUtils.fromMethodReference(
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
     * @return The {@link ProxyMethod} instance.
     */
    public static <TProxyInterface, TArg1, TArg2, R> ProxyMethod fromMethodReference(
            Class<TProxyInterface> proxyInterface,
            ProxyMethodReference.WithTwoArgs<TProxyInterface, TArg1, TArg2, R> methodReference
    ) {
        return fromMethodReference(proxyInterface, proxy -> { 
            // Invoked on proxy instance.
            // Ignore return value.
            methodReference.ref(proxy, null, null); 
        });
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
     *ProxyMethod proxyMethod = ProxyMethodUtils.fromMethodReference(
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
     * @return The {@link ProxyMethod} instance.
     */
    public static <TProxyInterface, TArg1, TArg2, TArg3, R> ProxyMethod fromMethodReference(
            Class<TProxyInterface> proxyInterface,
            ProxyMethodReference.WithThreeArgs<TProxyInterface, TArg1, TArg2, TArg3, R> methodReference
    ) {
        return fromMethodReference(proxyInterface, proxy -> { 
            // Invoked on proxy instance.
            // Ignore return value.
            methodReference.ref(proxy, null, null, null); 
        });
    }

    /**
     * Get a {@link Method} instance from a proxy interface's method reference.
     * 
     * <p>This will only work on interfaces.</p>
     * 
     * <p>Example:</p>
     * <blockquote><pre>
     *{@code // Proxy method which represents AppPropertiesProxy::propertyMethod}
     *{@code // Example matching method signature (no arguments):}
     *{@code // String propertyMethod()}
     *Method method = ProxyMethodUtils.getMethod(
     *    AppPropertiesProxy.class, 
     *    AppPropertiesProxy::propertyMethod
     *);
     * </blockquote></pre>
     * 
     * @param <TProxyInterface> The proxy interface.
     * @param <R> The method reference return type.
     * @param proxyInterface The proxy interface which declared the method to be referenced.
     * @param methodReference The method reference to extract the method from.
     * @return The {@link ProxyMethod} instance.
     */
    public static <TProxyInterface, R> Method getMethod(
            Class<TProxyInterface> proxyInterface,
            ProxyMethodReference<TProxyInterface, R> methodReference
    ) {
        return getMethod(proxyInterface, proxy -> { 
            // Invoked on proxy instance.
            // Ignore return value.
            methodReference.ref(proxy); 
        });
    }

    /**
     * Get a {@link Method} instance from a proxy interface's method reference.
     * 
     * <p>This will only work on interfaces.</p>
     * 
     * <p>Example:</p>
     * <blockquote><pre>
     *{@code // Proxy method which represents AppPropertiesProxy::propertyMethodWithOneArg}
     *{@code // Example matching method signature (one argument):}
     *{@code // String propertyMethodWithOneArg(String arg1)}
     *Method method = ProxyMethodUtils.getMethod(
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
     * @return The {@link ProxyMethod} instance.
     */
    public static <TProxyInterface, TArg1, TArg2, R> Method getMethod(
            Class<TProxyInterface> proxyInterface,
            ProxyMethodReference.WithOneArg<TProxyInterface, TArg1, R> methodReference
    ) {
        return getMethod(proxyInterface, proxy -> { 
            // Invoked on proxy instance.
            // Ignore return value.
            methodReference.ref(proxy, null); 
        });
    }

    /**
     * Get a {@link Method} instance from a proxy interface's method reference.
     * 
     * <p>This will only work on interfaces.</p>
     * 
     * <p>Example:</p>
     * <blockquote><pre>
     *{@code // Proxy method which represents AppPropertiesProxy::propertyMethodWithTwoArgs}
     *{@code // Example matching method signature (two arguments):}
     *{@code // String propertyMethodWithTwoArgs(String arg1, String arg2)}
     *Method method = ProxyMethodUtils.getMethod(
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
     * @return The {@link ProxyMethod} instance.
     */
    public static <TProxyInterface, TArg1, TArg2, R> Method getMethod(
            Class<TProxyInterface> proxyInterface,
            ProxyMethodReference.WithTwoArgs<TProxyInterface, TArg1, TArg2, R> methodReference
    ) {
        return getMethod(proxyInterface, proxy -> { 
            // Invoked on proxy instance.
            // Ignore return value.
            methodReference.ref(proxy, null, null); 
        });
    }

    /**
     * Get a {@link Method} instance from a proxy interface's method reference.
     * 
     * <p>This will only work on interfaces.</p>
     * 
     * <p>Example:</p>
     * <blockquote><pre>
     *{@code // Proxy method which represents AppPropertiesProxy::propertyMethodWithThreeArgs}
     *{@code // Example matching method signature (three arguments):}
     *{@code // String propertyMethodWithThreeArgs(String arg1, String arg2, String arg3)}
     *Method method = ProxyMethodUtils.getMethod(
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
     * @return The {@link ProxyMethod} instance.
     */
    public static <TProxyInterface, TArg1, TArg2, TArg3, R> Method getMethod(
            Class<TProxyInterface> proxyInterface,
            ProxyMethodReference.WithThreeArgs<TProxyInterface, TArg1, TArg2, TArg3, R> methodReference
    ) {
        return getMethod(proxyInterface, proxy -> { 
            // Invoked on proxy instance.
            // Ignore return value.
            methodReference.ref(proxy, null, null, null); 
        });
    }

    public static ProxyMethod fromMethod(
            Class<?> proxyInterface,
            String methodName,
            Class<?>... methodParameterTypes
    ) {
        Method method = getMethod(
            proxyInterface, 
            methodName, 
            methodParameterTypes
        );
        return new ProxyMethodAdapter(method);
    }

    public static ProxyMethod fromMethod(Method proxyInterfaceMethod) {
        return new ProxyMethodAdapter(proxyInterfaceMethod);
    }
    
    public static Method getMethod(
            Class<?> clazz, 
            String name, 
            Class<?>... parameterTypes
    ) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to find method.", e);
        }
    }

    private static <TProxyInterface> ProxyMethod fromMethodReference(
            Class<TProxyInterface> proxyInterface,
            Consumer<TProxyInterface> invoker
    ) {
        return new ProxyMethodAdapter(getMethod(proxyInterface, invoker));
    }

    private static <TProxyInterface> Method getMethod(
            Class<TProxyInterface> proxyInterface,
            Consumer<TProxyInterface> invoker
    ) {
        AtomicReference<Method> methodRef = new AtomicReference<>();

        @SuppressWarnings("unchecked")
        TProxyInterface proxy = (TProxyInterface)Proxy.newProxyInstance(
            proxyInterface.getClassLoader(), 
            new Class<?>[] { proxyInterface }, 
            (p, method, args) -> {
                methodRef.set(method);
                return getDefaultValue(method.getReturnType());
            }
        );

        // Invoke the method reference with the proxy instance.
        invoker.accept(proxy);

        return methodRef.get();
    }

    public static Object getDefaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }

        if (returnType.equals(boolean.class)) {
            return false;
        } else if (returnType.equals(byte.class)) {
            return (byte)0;
        } else if (returnType.equals(short.class)) {
            return (short)0;
        } else if (returnType.equals(int.class)) {
            return 0;
        } else if (returnType.equals(long.class)) {
            return 0L;
        } else if (returnType.equals(float.class)) {
            return 0.0F;
        } else if (returnType.equals(double.class)) {
            return 0.0D;
        } 

        throw new IllegalArgumentException("Unsupprted type: " + returnType.getName());
    }
}