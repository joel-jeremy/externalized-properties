package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ConverterFacade;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.TypeReference;
import io.github.joeljeremy7.externalizedproperties.core.UnresolvedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpanderFacade;
import io.github.joeljeremy7.externalizedproperties.core.internal.ExternalizedPropertyName;
import io.github.joeljeremy7.externalizedproperties.core.internal.MethodHandleFactory;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Invocation handler for Externalized Properties. It handles invocations of methods 
 * that are marked with {@link ExternalizedProperty} annotation.
 */
public class ExternalizedPropertiesInvocationHandler implements InvocationHandler {
    private final Resolver rootResolver;
    private final Converter<?> rootConverter;
    private final VariableExpander variableExpander;
    private final ProxyMethodFactory proxyMethodFactory;
    private final MethodHandleFactory methodHandleFactory;

    /**
     * Constructor.
     * 
     * @param rootResolver The root resolver.
     * @param rootConverter The root converter.
     * @param variableExpander The variable expander.
     * @param proxyMethodFactory The proxy method factory.
     */
    public ExternalizedPropertiesInvocationHandler(
            Resolver rootResolver,
            Converter<?> rootConverter,
            VariableExpander variableExpander,
            ProxyMethodFactory proxyMethodFactory
    ) {
        this.rootResolver = requireNonNull(rootResolver, "rootResolver");
        this.rootConverter = requireNonNull(rootConverter, "rootConverter");
        this.variableExpander = requireNonNull(
            variableExpander, 
            "variableExpander"
        );
        this.proxyMethodFactory = requireNonNull(
            proxyMethodFactory, 
            "proxyMethodFactory"
        );
        this.methodHandleFactory = new MethodHandleFactory();
    }

    /**
     * Handles the externalized properties proxy method invocation.
     * 
     * @param proxy The proxy object.
     * @param method The invoked method.
     * @param args The method invocation arguments.
     * @return Method return value.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Args is null instead of an empty array if there are no method parameters...
        args = args == null ? new Object[0] : args;

        // Handle invocations to native Object methods:
        // toString, equals, hashCode
        Object objectMethodResult = handleIfObjectMethod(proxy, method, args);
        if (objectMethodResult != null) {
            return objectMethodResult;
        }

        Optional<String> externalizedPropertyName = 
            ExternalizedPropertyName.fromProxyMethodInvocation(method, args);
        // @ExternalizedProperty and @ResolverFacade handling.
        if (externalizedPropertyName.isPresent()) {
            return resolveProperty(
                proxy,
                method, 
                args, 
                externalizedPropertyName.get()
            );
        }
        // @ConverterFacade handling
        else if (method.isAnnotationPresent(ConverterFacade.class)) {
            return handleConverterFacade(method, args);
        }
        // @VariableExpanderFacade handling.
        else if (method.isAnnotationPresent(VariableExpanderFacade.class)) {
            return handleVariableExpanderFacade(method, args);
        }

        // Either there was no property name (means not annotated with @ExternalizedProperty)
        // @ConverterFacade, or @VariableExpanderFacade. That, or property cannot be resolved.
        return determineDefaultValueOrThrow(
            proxy, 
            method, 
            args
        );
    }
    
    private Object resolveProperty(
            Object proxy,
            Method method, 
            Object[] args, 
            String externalizedPropertyName
    ) {
        ProxyMethod proxyMethod = proxyMethodFactory.proxyMethod(method);
        String expandedName = variableExpander.expandVariables(
            proxyMethod, 
            externalizedPropertyName
        );
        return rootResolver.resolve(proxyMethod, expandedName)
            .<Object>map(resolved -> {
                Type targetType = proxyMethod.returnType();
                if (proxyMethod.parameterTypes().length == 2) {
                    // Means this is a resolver facade with target type as second parameter.
                    targetType = determineConvertTargetType(args[1]);
                }
                return rootConverter.convert(proxyMethod, resolved, targetType).value();
            })
            .orElseGet(() -> determineDefaultValueOrThrow(proxy, method, args));
    }

    private String handleVariableExpanderFacade(Method method, Object[] args) {
        ProxyMethod proxyMethod = proxyMethodFactory.proxyMethod(method);
        // No need to validate. Already validated when proxy was built.
        String valueToExpand = (String)args[0];
        return variableExpander.expandVariables(proxyMethod, valueToExpand);
    }

    private Object handleConverterFacade(Method method, Object[] args) {
        ProxyMethod proxyMethod = proxyMethodFactory.proxyMethod(method);
        // No need to validate. Already validated when proxy was built.
        String valueToConvert = (String)args[0];
        Type targetType = proxyMethod.returnType();
        if (proxyMethod.parameterTypes().length == 2) {
            // Target type was provided as second parameter.
            targetType = determineConvertTargetType(args[1]);
        }
        return rootConverter.convert(proxyMethod, valueToConvert, targetType).value();
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
    private Object determineDefaultValueOrThrow(
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

        String propertyName = 
            ExternalizedPropertyName.fromProxyMethodInvocation(method, args)
                .orElse("null");

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
    private Object invokeDefaultInterfaceMethod(
            Object proxy, 
            Method method, 
            Object[] args
    ) {
        DefaultInterfaceMethodHandler handler = 
            buildDefaultInterfaceMethodHandler(proxy, method);
        return handler.invoke(args);
    }

    private DefaultInterfaceMethodHandler buildDefaultInterfaceMethodHandler(
            Object proxy, 
            Method proxyMethod
    ) {
        MethodHandle methodHandle = 
            methodHandleFactory.createMethodHandle(proxyMethod).bindTo(proxy);
        
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

    // If new target types are supported, add it there or else a ClassCastException 
    // will get thrown.
    private static Type determineConvertTargetType(Object arg) {
        if (arg instanceof TypeReference<?>) {
            return ((TypeReference<?>)arg).type();
        }

        // Class is also a Type.
        // Safe to cast as only allowed types are: TypeReference, Class, and Type.
        return (Type)arg;
    }
    
    // Avoid calling methods in proxy object to avoid recursion.
    private static @Nullable Object handleIfObjectMethod(
            Object proxy, 
            Method method, 
            Object[] args
    ) {
        if ("toString".equals(method.getName())) {
            return proxyToString(proxy);
        }
        else if ("equals".equals(method.getName()) && method.getParameterCount() == 1) {
            return proxyEquals(proxy, args);
        }
        else if ("hashCode".equals(method.getName())) {
            return proxyHashCode(proxy);
        }

        return null;
    }

    private static int proxyHashCode(Object proxy) {
        return System.identityHashCode(proxy);
    }

    // Only do reference equality.
    private static boolean proxyEquals(Object proxy, Object[] args) {
        return proxy == args[0];
    }

    private static String proxyToString(Object proxy) {
        return proxy.getClass().getName() + '@' + 
            Integer.toHexString(proxyHashCode(proxy));
    }

    private static interface DefaultInterfaceMethodHandler {
        Object invoke(Object... args);
    }
}