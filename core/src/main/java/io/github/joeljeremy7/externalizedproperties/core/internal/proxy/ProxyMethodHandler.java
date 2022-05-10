package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.UnresolvedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.internal.MethodHandleFactory;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Optional;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Handles the resolution of properties for proxy methods.
 */
public class ProxyMethodHandler {
    private final Resolver resolver;
    private final Converter<?> converter;
    private final MethodHandleFactory methodHandleFactory;
    
    /**
     * Constructor.
     * 
     * @param resolver The resolver.
     * @param converter The converter.
     * @param methodHandleFactory The method handle factory.
     */
    public ProxyMethodHandler(
            Resolver resolver,
            Converter<?> converter,
            MethodHandleFactory methodHandleFactory
    ) {
        requireNonNull(resolver, "resolver");
        requireNonNull(converter, "converter");
        requireNonNull(methodHandleFactory, "methodHandleFactory");

        this.resolver = resolver;
        this.converter = converter;
        this.methodHandleFactory = methodHandleFactory;
    }

    /**
     * Resolve externalized property.
     * 
     * @param proxy The proxy.
     * @param method The proxy method.
     * @param args Proxy invocation method arguments.
     * @return The resolved property.
     */
    public Object handle(Object proxy, Method method, Object[] args) {
        ProxyMethod proxyMethod = new ProxyMethodAdapter(method, args);
        Optional<String> externalizedPropertyName = proxyMethod.externalizedPropertyName();
        if (externalizedPropertyName.isPresent()) {
            Optional<?> result = resolver.resolve(proxyMethod, externalizedPropertyName.get())
                .map(resolved -> converter.convert(proxyMethod, resolved).value());
                    
            if (result.isPresent()) {
                return result.get();
            }
        }

        // Either there was no property name (means not annotated with @ExternalizedProperty)
        // or property cannot be resolved.
        return determineDefaultValueOrThrow(
            proxy, 
            method, 
            args
        );
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
     * @param proxy The proxy.
     * @param method The proxy method.
     * @param args The arguments passed to the method.
     * @return The default value that shall be returned by the method.
     * @throws UnresolvedPropertiesException if a default value cannot be determined.
     */
    public Object determineDefaultValueOrThrow(
            Object proxy, 
            Method method, 
            Object[] args
    ) {
        if (method.isDefault()) {
            return invokeDefaultInterfaceMethod(proxy, method, args);
        }

        if (Optional.class.equals(method.getReturnType())) {
            return Optional.empty();
        }

        String propertyName = getExternalizedPropertyNameOrNullLiteral(method);

        // Non-optional properties will throw an exception if cannot be resolved.
        throw new UnresolvedPropertiesException(
            propertyName,
            String.format(
                "Failed to resolve property '%s' for proxy interface method (%s). " + 
                "To prevent exceptions when a property cannot be resolved, " +
                "consider changing proxy interface method's return type to an Optional.",
                propertyName,
                method.toGenericString()
            )
        );
    }

    /**
     * Invoke the default interface method.
     * 
     * @param proxy The proxy.
     * @param method The proxy method.
     * @param args The arguments to pass to the default interface method.
     * @return The result of the default interface method.
     */
    public Object invokeDefaultInterfaceMethod(
            Object proxy, 
            Method method, 
            Object[] args
    ) {
        try {
            DefaultInterfaceMethodHandler handler = buildDefaultInterfaceMethodHandler(
                proxy, 
                method, 
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
                    method.toGenericString()
                ), 
                ex
            );
        }
    }

    private DefaultInterfaceMethodHandler buildDefaultInterfaceMethodHandler(
            Object proxy, 
            Method proxyMethod, 
            MethodHandleFactory methodHandleFactory
    ) {
        if (proxyMethod.isDefault()) {
            MethodHandle methodHandle = methodHandleFactory.createMethodHandle(proxyMethod);
            return args -> {
                return methodHandle.bindTo(proxy).invokeWithArguments(args);
            };
        }

        // Just make it throw if method is not a default interface method.
        return args -> {
            throw new IllegalStateException(String.format(
                "Tried to invoke a non-default interface method. " +
                "Proxy method: %s.",
                proxyMethod.toGenericString()
            ));
        };
    }

    private String getExternalizedPropertyNameOrNullLiteral(Method proxyMethod) {
        ExternalizedProperty externalizedProperty = 
            proxyMethod.getAnnotation(ExternalizedProperty.class);
        if (externalizedProperty == null) {
            return "null";
        }
        return externalizedProperty.value();
    }

    private static interface DefaultInterfaceMethodHandler {
        Object invoke(Object... args) throws Throwable;
    }
}
