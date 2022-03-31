package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Method handle factory to creating {@link MethodHandle} for methods.
 * This factory changes implementation based on the detected java version.
 * 
 * This should continue working even when running under Java 9+.
 */
public class MethodHandleFactory {
    // Not Java 1.7, 1.8, etc.
    private static final boolean IS_RUNNING_ON_JAVA_9_OR_LATER = 
        !System.getProperty("java.specification.version").startsWith("1.");

    private final Map<Method, MethodHandle> weakMethodHandleCache = new WeakHashMap<>(); 

    /**
     * Build a method handle from the given target and method.
     * 
     * @param method The method to build the method handle from.
     * @return The generated {@link MethodHandle} for the method. 
     * This method handle has been binded to the target object. 
     */
    public MethodHandle createMethodHandle(Method method) {
        MethodHandle methodHandle = weakMethodHandleCache.get(method);
        if (methodHandle == null) {
            methodHandle = buildMethodHandleInternal(method);
            weakMethodHandleCache.putIfAbsent(method, methodHandle);
        }
        return methodHandle;
    }

    private static MethodHandle buildMethodHandleInternal(Method method) {
        try {
            if (IS_RUNNING_ON_JAVA_9_OR_LATER) {
                return Java9MethodHandleFactory.buildMethodHandle(method);
            }

            return Java8MethodHandlerFactory.buildMethodHandle(method);
        } catch (Throwable ex) {
            throw new ExternalizedPropertiesException(
                "Error occurred while trying to build method handle for method: " +
                method.toGenericString(), 
                ex
            );
        }
    }

    private static class Java8MethodHandlerFactory {
        private static MethodHandle buildMethodHandle(Method method) 
                throws Exception 
        {
            // This will only work on Java 8.
            // For Java9+, the new private lookup API should be used.
            final Constructor<Lookup> constructor = Lookup.class
                .getDeclaredConstructor(Class.class);
            
            constructor.setAccessible(true);
            final Lookup lookup = constructor.newInstance(method.getDeclaringClass());
            constructor.setAccessible(false);
            
            return lookup.in(method.getDeclaringClass())
                .unreflectSpecial(method, method.getDeclaringClass());
        }
    }

    private static class Java9MethodHandleFactory {
        // This is null if not on Java 9+.
        // This method should be present in Java 9+.
        // Method handle for MethodHandles.privateLookupIn(...) method.
        private final static MethodHandle JAVA_9_PRIVATE_LOOKUP_IN_MH =
            privateLookupInMethodHandleOrThrow();
        
        private static MethodHandle buildMethodHandle(Method method) 
                throws Throwable 
        {
            Lookup privateLookup = privateLookupIn(method.getDeclaringClass());
            return privateLookup.in(method.getDeclaringClass())
                .unreflectSpecial(method, method.getDeclaringClass());
        }

        private static Lookup privateLookupIn(Class<?> classToLookup) 
                throws Throwable 
        {
            return (Lookup)JAVA_9_PRIVATE_LOOKUP_IN_MH.invokeWithArguments(
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
                    "while running on Java 9+.", 
                    e
                );
            }
        }
    }
}