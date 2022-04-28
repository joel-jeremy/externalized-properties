package io.github.joeljeremy7.externalizedproperties.core.testfixtures;

import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

public class ProxyMethodFactory<TProxyInterface> {

    private final Class<TProxyInterface> proxyInterface;

    public ProxyMethodFactory(Class<TProxyInterface> proxyInterface) {
        this.proxyInterface = proxyInterface;
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
     *ProxyMethod proxyMethod = ProxyMethodUtils.fromMethodReference(
     *    AppPropertiesProxy.class, 
     *    AppPropertiesProxy::propertyMethod
     *);
     * </blockquote></pre>
     * 
     * @param <R> The method reference return type.
     * @param methodReference The method reference to extract the method from.
     * @return The {@link ProxyMethod} instance.
     */
    public <R> ProxyMethod fromMethodReference(
            ProxyMethodReference<TProxyInterface, R> methodReference
    ) {
        return ProxyMethodUtils.fromMethodReference(proxyInterface, methodReference);
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
     * @param <TArg1> The type of first argument of the method reference.
     * @param <R> The method reference return type.
     * @param methodReference The method reference to extract the method from.
     * @return The {@link ProxyMethod} instance.
     */
    public <TArg1, R> ProxyMethod fromMethodReference(
            ProxyMethodReference.WithOneArg<TProxyInterface, TArg1, R> methodReference
    ) {
        return ProxyMethodUtils.fromMethodReference(proxyInterface, methodReference);
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
     * @param <TArg1> The type of first argument of the method reference.
     * @param <TArg2> The type of second argument of the method reference.
     * @param <R> The method reference return type.
     * @param methodReference The method reference to extract the method from.
     * @return The {@link ProxyMethod} instance.
     */
    public <TArg1, TArg2, R> ProxyMethod fromMethodReference(
            ProxyMethodReference.WithTwoArgs<TProxyInterface, TArg1, TArg2, R> methodReference
    ) {
        return ProxyMethodUtils.fromMethodReference(proxyInterface, methodReference);
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
     * @param <TArg1> The type of first argument of the method reference.
     * @param <TArg2> The type of second argument of the method reference.
     * @param <TArg3> The type of third argument of the method reference.
     * @param <R> The method reference return type.
     * @param methodReference The method reference to extract the method from.
     * @return The {@link ProxyMethod} instance.
     */
    public <TArg1, TArg2, TArg3, R> ProxyMethod fromMethodReference(
            ProxyMethodReference.WithThreeArgs<TProxyInterface, TArg1, TArg2, TArg3, R> methodReference
    ) {
        return ProxyMethodUtils.fromMethodReference(proxyInterface, methodReference);
    }
}