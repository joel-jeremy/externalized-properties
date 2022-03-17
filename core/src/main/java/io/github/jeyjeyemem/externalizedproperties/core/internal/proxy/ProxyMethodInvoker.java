package io.github.jeyjeyemem.externalizedproperties.core.internal.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ProcessingContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.UnresolvedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.MethodHandleFactory;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodAdapter;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Contains information about a proxy method and handles the resolution of properties 
 * for the proxy method invocation.
 */
public class ProxyMethodInvoker {
    private final Object proxy;
    private final ProxyMethodAdapter proxyMethod;
    private final ExternalizedProperties externalizedProperties;
    private final MethodHandleFactory methodHandleFactory;

    /** Nullable */
    private final String expandedPropertyName;
    
    /**
     * Constructor.
     * 
     * @param proxy The proxy.
     * @param method The method.
     * @param externalizedProperties The externalized properties.
     * @param methodHandleFactory The method handle factory.
     */
    public ProxyMethodInvoker(
            Object proxy, 
            Method method,
            ExternalizedProperties externalizedProperties,
            MethodHandleFactory methodHandleFactory
    ) {
        requireNonNull(proxy, "proxy");
        requireNonNull(method, "method");
        requireNonNull(externalizedProperties, "externalizedProperties");
        requireNonNull(methodHandleFactory, "methodHandleFactory");

        this.proxy = proxy;
        this.proxyMethod = new ProxyMethodAdapter(method);
        this.externalizedProperties = externalizedProperties;
        this.methodHandleFactory = methodHandleFactory;
        
        this.expandedPropertyName = proxyMethod.externalizedPropertyAnnotation()
            .map(ep -> expandVariables(ep.value()))
            .orElse(null);
    }

    /**
     * Resolve externalized property.
     * 
     * @param args Proxy invocation method arguments.
     * @return The resolved property.
     */
    public Object resolveProperty(Object[] args) {
        if (expandedPropertyName != null) {
            Optional<?> resolved = externalizedProperties.resolver()
                .resolve(expandedPropertyName)
                .map(this::process)
                .map(this::convert);
                    
            if (resolved.isPresent()) {
                return resolved.get();
            }
        }

        // Either there was no property name 
        // (means not annotated with @ExternalizedProperty)
        // or property cannot be resolved.
        return determineDefaultValueOrThrow(args);
    }

    private String process(String value) {
        return externalizedProperties.processor().process(
            new ProcessingContext(proxyMethod, value)
        );
    }

    private Object convert(String value) {
        return externalizedProperties.converter().convert(
            new ConversionContext(
                externalizedProperties.converter(), 
                proxyMethod, 
                value
            )
        ).value();
    }

    private String expandVariables(String value) {
        return externalizedProperties.variableExpander()
            .expandVariables(value);
    }

    /**
     * Determine a default value for the externalized property method or throw.
     * This will attempt to do the following:
     * <ol>
     *  <li>
     *      Invoke the method if it's a default interface method and return the value.
     *  </li>
     *  <li>
     *      Return {@link Optional#empty()} if the method return type is an {@link Optional}.
     *  </li>
     *  <li>
     *      Throw an exception if the method return type is not an {@link Optional}.
     *  </li>
     * </ol> 
     * 
     * @param args The arguments passed to the method.
     * @return The default value that shall be returned by the method.
     * @throws UnresolvedPropertiesException if a default value cannot be determined.
     */
    public Object determineDefaultValueOrThrow(Object[] args) {
        if (proxyMethod.isDefaultInterfaceMethod()) {
            return invokeDefaultInterfaceMethod(args);
        }

        if (proxyMethod.hasReturnType(Optional.class)) {
            return Optional.empty();
        }

        String propertyName = proxyMethod.externalizedPropertyName().orElse(null);
        // Non-optional properties will throw an exception if cannot be resolved.
        throw new UnresolvedPropertiesException(
            propertyName,
            String.format(
                "Failed to resolve property '%s' for proxy method (%s). " + 
                "To prevent exceptions when a property cannot be resolved, " +
                "consider changing method's return type to an Optional.",
                propertyName,
                proxyMethod.methodSignatureString()
            )
        );
    }

    /**
     * Invoke the default interface method.
     * 
     * @param args The arguments to pass to the default interface method.
     * @return The result of the default interface method.
     */
    public Object invokeDefaultInterfaceMethod(Object[] args) {
        try {
            DefaultInterfaceMethodHandler handler = buildDefaultInterfaceMethodHandler(
                proxy, 
                proxyMethod.method(), 
                methodHandleFactory
            );
            return handler.invoke(args);
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Throwable ex) {
            throw new ExternalizedPropertiesException(
                String.format(
                    "Error occurred while invoking default interface method. " +
                    "Proxy method: %s.",
                    proxyMethod.methodSignatureString()
                ), 
                ex
            );
        }
    }

    private DefaultInterfaceMethodHandler buildDefaultInterfaceMethodHandler(
            Object proxy, 
            Method method, 
            MethodHandleFactory methodHandleFactory
    ) {
        if (proxyMethod.isDefaultInterfaceMethod()) {
            MethodHandle methodHandle = methodHandleFactory.createMethodHandle(method);
            return args -> {
                return methodHandle.bindTo(proxy).invokeWithArguments(args);
            };
        }

        // Just make it throw if method is not a default interface method.
        return args -> {
            throw new IllegalStateException(String.format(
                "Tried to invoke a non-default interface method. " +
                "Proxy method: %s.",
                proxyMethod.methodSignatureString()
            ));
        };
    }

    private static interface DefaultInterfaceMethodHandler {
        Object invoke(Object... args) throws Throwable;
    }
}
