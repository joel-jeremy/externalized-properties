package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Stream;

/**
 * The {@link DefaultInterfaceMethodHandler} factory.
 */
public class DefaultInterfaceMethodHandlerFactory {
    // Not Java 1.7, 1.8, etc.
    private static final boolean IS_RUNNING_ON_JAVA_9_OR_LATER = 
        !javaVersion().startsWith("1.");
    
    private final Map<Method, DefaultInterfaceMethodHandler> weakHandlerCache = new WeakHashMap<>();

    /**
     * Create a {@link DefaultInterfaceMethodHandler} for the specified default interface method.
     * 
     * @param defaultInterfaceMethod The default interface method.
     * @return The built {@link DefaultInterfaceMethodHandler}.
     */
    public DefaultInterfaceMethodHandler create(Method defaultInterfaceMethod) {
        DefaultInterfaceMethodHandler cachedHandler = weakHandlerCache.get(defaultInterfaceMethod);
        if (cachedHandler != null) {
            return cachedHandler;
        }

        if (!defaultInterfaceMethod.isDefault()) {
            throw new IllegalArgumentException(
                defaultInterfaceMethod.toGenericString() + " is not a default interface method.");
        }

        /**
         * Note: We optimize for methods with up to 2 arguments. We create lambda functions for better
         * performance. This number may change in the future.
         */

        try {
            // Optimization for default interface methods that have no args.
            if (defaultInterfaceMethod.getParameterCount() == 0) {
                return cache(defaultInterfaceMethod, createNoArgLambda(defaultInterfaceMethod));
            }
            // Optimization for default interface methods that have one arg.
            else if (defaultInterfaceMethod.getParameterCount() == 1) {
                return cache(defaultInterfaceMethod, createOneArgLambda(defaultInterfaceMethod));
            }
            // Optimization for default interface methods that have two args.
            else if (defaultInterfaceMethod.getParameterCount() == 2) {
                return cache(defaultInterfaceMethod, createTwoArgsLambda(defaultInterfaceMethod));
            }
            else {
                // Fallback to using method handles.
                MethodHandle methodHandle = buildMethodHandleInternal(defaultInterfaceMethod)
                    .asSpreader(Object[].class, defaultInterfaceMethod.getParameterCount());
                
                return cache(defaultInterfaceMethod, (instance, args) -> {
                    try {
                        return methodHandle.invoke(instance, args);
                    }
                    catch (Throwable e) {
                        throw new ExternalizedPropertiesException(
                            String.format(
                                "Error occurred while invoking default interface method. " +
                                "Proxy method: %s.",
                                defaultInterfaceMethod.toGenericString()
                            ), 
                            e
                        );
                    }
                });
            }
        } catch (Throwable e) {
            throw new ExternalizedPropertiesException(
                "Error occurred while building default interface method handler", e);
        }
    }

    private DefaultInterfaceMethodHandler cache(
            Method defaultInterfaceMethod, DefaultInterfaceMethodHandler handler) {
        
        weakHandlerCache.put(defaultInterfaceMethod, handler);
        return handler;
    }

    private static DefaultInterfaceMethodHandler createNoArgLambda(
            Method defaultInterfaceMethod) throws Throwable {
        
        NoArgLambdaFunction lambda = LambdaFactory.createLambdaFunction(
            defaultInterfaceMethod, 
            NoArgLambdaFunction.class  
        );

        return (instance, args) -> {
            try {
                return lambda.invoke(instance);
            }
            catch (Throwable e) {
                throw new ExternalizedPropertiesException(
                    String.format(
                        "Error occurred while invoking default interface method. " +
                        "Proxy method: %s.",
                        defaultInterfaceMethod.toGenericString()
                    ), 
                    e
                );
            }
        };
    }

    private static DefaultInterfaceMethodHandler createOneArgLambda(
            Method defaultInterfaceMethod) throws Throwable {
        
        OneArgLambdaFunction lambda = LambdaFactory.createLambdaFunction(
            defaultInterfaceMethod, 
            OneArgLambdaFunction.class  
        );

        return (instance, args) -> {
            try {
                return lambda.invoke(instance, args[0]);
            }
            catch (Throwable e) {
                throw new ExternalizedPropertiesException(
                    String.format(
                        "Error occurred while invoking default interface method. " +
                        "Proxy method: %s.",
                        defaultInterfaceMethod.toGenericString()
                    ),
                    e
                );
            }
        };
    }

    private static DefaultInterfaceMethodHandler createTwoArgsLambda(
            Method defaultInterfaceMethod) throws Throwable {
        
        TwoArgsLambdaFunction lambda = LambdaFactory.createLambdaFunction(
            defaultInterfaceMethod, 
            TwoArgsLambdaFunction.class  
        );

        return (instance, args) -> {
            try {
                return lambda.invoke(instance, args[0], args[1]);
            }
            catch (Throwable e) {
                throw new ExternalizedPropertiesException(
                    String.format(
                        "Error occurred while invoking default interface method. " +
                        "Proxy method: %s.",
                        defaultInterfaceMethod.toGenericString()
                    ), 
                    e
                );
            }
        };
    }

    private static MethodHandle buildMethodHandleInternal(Method method) throws Throwable {
        if (IS_RUNNING_ON_JAVA_9_OR_LATER) {
            return Java9MethodHandleFactory.buildMethodHandle(method);
        }

        return Java8MethodHandleFactory.buildMethodHandle(method);
    }

    private static Lookup privateLookupIn(Class<?> classToLookup) throws Throwable {
        if (IS_RUNNING_ON_JAVA_9_OR_LATER) {
            return Java9MethodHandleFactory.privateLookupIn(classToLookup);
        }

        return Java8MethodHandleFactory.privateLookupIn(classToLookup);
    }

    private static class Java8MethodHandleFactory {
        private Java8MethodHandleFactory() {}

        private static MethodHandle buildMethodHandle(Method method) throws Throwable {
            final Lookup privateLookup = privateLookupIn(method.getDeclaringClass());
            return privateLookup.unreflectSpecial(method, method.getDeclaringClass());
        }

        private static Lookup privateLookupIn(Class<?> classToLookup) throws Throwable {
            // This will only work on Java 8.
            // For Java9+, the new private lookup API should be used.
            final Constructor<Lookup> constructor = Lookup.class
                .getDeclaredConstructor(Class.class);
            
            constructor.setAccessible(true);
            final Lookup lookup = constructor.newInstance(classToLookup);
            constructor.setAccessible(false);

            return lookup;
        }
    }

    private static class Java9MethodHandleFactory {
        // This will only work on Java 9+.
        // This method should be present in Java 9+.
        // Method handle for MethodHandles.privateLookupIn(...) method.
        private static final MethodHandle JAVA_9_MH_PRIVATE_LOOKUP_IN =
            privateLookupInMethodHandleOrThrow();

        private Java9MethodHandleFactory() {}
        
        private static MethodHandle buildMethodHandle(Method method) 
                throws Throwable 
        {
            Lookup privateLookup = privateLookupIn(method.getDeclaringClass());
            return privateLookup.unreflectSpecial(method, method.getDeclaringClass());
        }

        private static Lookup privateLookupIn(Class<?> classToLookup) 
                throws Throwable 
        {
            return (Lookup)JAVA_9_MH_PRIVATE_LOOKUP_IN.invokeWithArguments(
                classToLookup,
                MethodHandles.lookup()
            );
        }

        private static MethodHandle privateLookupInMethodHandleOrThrow() {
            try {
                Method privateLookupIn = MethodHandles.class.getDeclaredMethod(
                    "privateLookupIn", 
                    Class.class, 
                    Lookup.class
                );
                return MethodHandles.lookup().unreflect(privateLookupIn);
            } catch (Exception e) {
                throw new IllegalStateException(
                    "Unable to find MethodHandles.privateLookupIn method " + 
                    "while running on Java " + javaVersion() + ".", 
                    e
                );
            }
        }
    }

    private static String javaVersion() {
        return System.getProperty("java.specification.version");
    }

    /**
     * Handler for invoking default interface methods.
     */
    public static interface DefaultInterfaceMethodHandler {
        /**
         * Invoke the target default interface method.
         * 
         * @param instance The object whose class declares the default interface method.
         * @param args The method arguments.
         * @return The method result.
         */
        Object invoke(Object instance, Object... args);
    }

    /**
     * Interface used internally by the factory to generate a lambda function.
     */
    @FunctionalInterface
    public static interface NoArgLambdaFunction {
        /**
         * Invoke the target default interface method.
         * 
         * @param instance The object whose class declares the default interface method.
         * @return The method result.
         */
        Object invoke(Object instance);
    }

    /**
     * Interface used internally by the factory to generate a lambda function.
     */
    @FunctionalInterface
    public static interface OneArgLambdaFunction {
        /**
         * Invoke the target default interface method.
         * 
         * @param instance The object whose class declares the default interface method.
         * @param arg The method argument.
         * @return The method result.
         */
        Object invoke(Object instance, Object arg);
    }

    /**
     * Interface used internally by the factory to generate a lambda function.
     */
    @FunctionalInterface
    public static interface TwoArgsLambdaFunction {
        /**
         * Invoke the target default interface method.
         * 
         * @param instance The object whose class declares the default interface method.
         * @param arg1 The first method argument.
         * @param arg2 The second method argument.
         * @return The method result.
         */
        Object invoke(Object instance, Object arg1, Object arg2);
    }

    /**
     * Factory for lambda functions created via {@code LambdaMetafactory}.
     */
    static class LambdaFactory {
        private static final FunctionalInterfaceMethodMap FUNCTIONAL_INTERFACE_METHOD_MAP = 
            new FunctionalInterfaceMethodMap();

        private LambdaFactory() {}
    
        /**
         * Create a lambda function using {@code LambdaMetafactory}. This only supports
         * default interface methods.
         *
         * @param <T> The functional interface.
         * @param targetMethod The defautlt interface method which will be targeted by the lambda function.
         * @param functionalInterface The interface to serve as the functional interface.
         * @return The instantiated lambda function which targets the specified target method.
         */
        static <T> T createLambdaFunction(
                Method targetMethod, Class<T> functionalInterface) throws Throwable {
            if (!targetMethod.isDefault()) {
                throw new IllegalArgumentException(
                    "Target method " + targetMethod.toGenericString() 
                    + " is not a default interface method.");
            }
            Method samMethod = FUNCTIONAL_INTERFACE_METHOD_MAP.get(functionalInterface);

            Class<?> declaringClass = targetMethod.getDeclaringClass();

            MethodHandles.Lookup lookup = privateLookupIn(declaringClass);

            // unreflectSpecial for default interface methods.
            MethodHandle requestHandlerMethodHandle =
                lookup.unreflectSpecial(targetMethod, declaringClass);

            MethodType instantiatedMethodType =
                MethodType.methodType(
                    targetMethod.getReturnType(), declaringClass, targetMethod.getParameterTypes());

            MethodType samMethodType =
                MethodType.methodType(samMethod.getReturnType(), samMethod.getParameterTypes());

            CallSite callSite =
                LambdaMetafactory.metafactory(
                    lookup,
                    samMethod.getName(),
                    MethodType.methodType(functionalInterface),
                    samMethodType,
                    requestHandlerMethodHandle,
                    instantiatedMethodType);

            return (T)callSite.getTarget().invoke();
        }

        private static class FunctionalInterfaceMethodMap extends ClassValue<Method> {
            /** Get the single abstract method (SAM) of the functional interface. */
            @Override
            protected Method computeValue(Class<?> functionalInterface) {
                Method[] methods =
                    Stream.of(functionalInterface)
                        .filter(Class::isInterface)
                        .flatMap(m -> Stream.of(m.getMethods()))
                        .filter(m -> Modifier.isAbstract(m.getModifiers()))
                        .toArray(Method[]::new);
        
                if (methods.length != 1) {
                    throw new IllegalArgumentException(
                        "Class is not a functional interface: " + functionalInterface.getName());
                }
        
                return methods[0];
            }
        }
    }
}