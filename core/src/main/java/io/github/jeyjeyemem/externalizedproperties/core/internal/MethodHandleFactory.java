package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
    
    // This is null if not on Java 9+.
    // This method should be present in Java 9+.
    // Method handle for MethodHandles.privateLookupIn(...) method.
    private static MethodHandle JAVA_9_PRIVATE_LOOKUP_IN_MH;

    static {
        JAVA_9_PRIVATE_LOOKUP_IN_MH = java9PrivateLookupInMethodHandleOrNull();
    }

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
                return java9BuildMethodHandle(method);
            }

            return java8BuildMethodHandle(method);
        } catch (Exception ex) {
            throw new ExternalizedPropertiesException(
                "Error occurred while trying to build method handle for method: " +
                method.toGenericString(), 
                ex
            );
        }
    }

    private static MethodHandle java8BuildMethodHandle(Method method) 
            throws NoSuchMethodException, SecurityException, 
                InstantiationException, IllegalAccessException, 
                IllegalArgumentException, InvocationTargetException {
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

    private static MethodHandle java9BuildMethodHandle(Method method) 
            throws IllegalAccessException {
        Lookup privateLookup = java9PrivateLookup(method.getDeclaringClass());
        return privateLookup.in(method.getDeclaringClass())
            .unreflectSpecial(method, method.getDeclaringClass());
    }

    private static Lookup java9PrivateLookup(Class<?> classToLookup) {
        try {
            if (JAVA_9_PRIVATE_LOOKUP_IN_MH != null) {
                Lookup privateLookup = 
                    (Lookup)JAVA_9_PRIVATE_LOOKUP_IN_MH.invokeWithArguments(
                        classToLookup,
                        MethodHandles.lookup()
                    );
                
                if (privateLookup != null) {
                    return privateLookup;
                }
            }
        } catch (Throwable ex) {
            throw new IllegalStateException(
                "Error occurred while obtaining private lookup " + 
                "from Java 9+ MethodHandles.privateLookupIn method.", 
                ex
            );
        }
            
        throw new IllegalStateException(
            "Failed to obtain private lookup from Java 9+ MethodHandles.privateLookupIn method."
        );
    }

    private static MethodHandle java9PrivateLookupInMethodHandleOrNull() {
        try {
            if (IS_RUNNING_ON_JAVA_9_OR_LATER) {
                Method privateLookupIn = MethodHandles.class.getDeclaredMethod(
                    "privateLookupIn", 
                    Class.class, 
                    Lookup.class
                );

                return MethodHandles.lookup().unreflect(privateLookupIn);
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                "Unable to find MethodHandles.privateLookupIn method while running on Java 9+.", 
                e
            );
        }

        return null;
    }
}