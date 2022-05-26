package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.Convert;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.TypeReference;
import io.github.joeljeremy7.externalizedproperties.core.UnresolvedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.internal.MethodHandleFactory;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Handles the resolution of properties for proxy methods.
 */
public class ExternalizedPropertiesExecutor {
    private final Resolver rootResolver;
    private final Converter<?> rootConverter;
    private final ProxyMethodFactory proxyMethodFactory;
    private final MethodHandleFactory methodHandleFactory;
    
    /**
     * Constructor.
     * 
     * @param rootResolver The root resolver.
     * @param rootConverter The root converter.
     * @param proxyMethodFactory The proxy method factory.
     * @param methodHandleFactory The method handle factory.
     */
    public ExternalizedPropertiesExecutor(
            Resolver rootResolver,
            Converter<?> rootConverter,
            ProxyMethodFactory proxyMethodFactory,
            MethodHandleFactory methodHandleFactory
    ) {
        requireNonNull(rootResolver, "rootResolver");
        requireNonNull(rootConverter, "rootConverter");
        requireNonNull(proxyMethodFactory, "proxyMethodFactory");
        requireNonNull(methodHandleFactory, "methodHandleFactory");
    
        this.rootResolver = rootResolver;
        this.rootConverter = rootConverter;
        this.proxyMethodFactory = proxyMethodFactory;
        this.methodHandleFactory = methodHandleFactory;
    }

    /**
     * Handle proxy method invocation.
     * 
     * @param proxy The proxy.
     * @param method The proxy method.
     * @param args Proxy method invocation arguments.
     * @return The proxy method's result.
     */
    public Object handle(Object proxy, Method method, Object[] args) {
        ProxyMethod proxyMethod = proxyMethodFactory.proxyMethod(method, args);
        Optional<String> externalizedPropertyName = proxyMethod.externalizedPropertyName();
        if (externalizedPropertyName.isPresent()) {
            Optional<?> result = rootResolver.resolve(proxyMethod, externalizedPropertyName.get())
                .map(resolved -> rootConverter.convert(proxyMethod, resolved).value());
                    
            if (result.isPresent()) {
                return result.get();
            }
        }

        if (proxyMethod.hasAnnotation(Convert.class)) {
            // No need to validate. Already validated when proxy was built.
            String valueToConvert = (String)args[0];
            Type targetType = determineConvertTargetType(args[1]);

            return rootConverter.convert(proxyMethod, valueToConvert, targetType).value();
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
        DefaultInterfaceMethodHandler handler = buildDefaultInterfaceMethodHandler(
            proxy, 
            method, 
            methodHandleFactory
        );
        return handler.invoke(args);
    }

    private static DefaultInterfaceMethodHandler buildDefaultInterfaceMethodHandler(
            Object proxy, 
            Method proxyMethod, 
            MethodHandleFactory methodHandleFactory
    ) {
        if (proxyMethod.isDefault()) {
            MethodHandle methodHandle = methodHandleFactory.createMethodHandle(proxyMethod).bindTo(proxy);
            return args -> {
                try {
                    return methodHandle.invokeWithArguments(args);
                } catch (Throwable e) {
                    throw new ExternalizedPropertiesException(
                        String.format(
                            "Error occurred while invoking default interface method. " +
                            "Proxy method: %s.",
                            proxyMethod.toGenericString()
                        ), 
                        e
                    );
                }
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

    private static String getExternalizedPropertyNameOrNullLiteral(Method proxyMethod) {
        ExternalizedProperty externalizedProperty = 
            proxyMethod.getAnnotation(ExternalizedProperty.class);
        if (externalizedProperty == null) {
            return "null";
        }
        return externalizedProperty.value();
    }

    private static Type determineConvertTargetType(Object arg) {
        if (arg instanceof TypeReference<?>) {
            return ((TypeReference<?>)arg).type();
        }
        // Class is also a Type.
        else if (arg instanceof Type) {
            return (Type)arg;
        }

        throw new IllegalArgumentException(String.format(
            "Unable to determine target type of @%s proxy method. " +
            "Method argument (%s): %s.",
            Convert.class.getSimpleName(),
            arg.getClass().getName(),
            arg
        ));
    }

    private static interface DefaultInterfaceMethodHandler {
        Object invoke(Object... args);
    }
}
