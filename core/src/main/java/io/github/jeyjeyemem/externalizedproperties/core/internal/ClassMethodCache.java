// package io.github.jeyjeyemem.externalizedproperties.core.internal;

// import java.lang.reflect.Method;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.function.Function;

// /**
//  * Method cache that utilizes {@link ClassValue} to avoid strong references to methods
//  * when using them as map keys.
//  * 
//  * Each independent method declaring class will have its own cache.
//  */
// public class ClassMethodCache<T> extends ClassValue<Map<MethodKey, T>> {

//     /**
//      * Generate a new cache map for each declaring class of a method.
//      * 
//      * @param declaringClass The declaring class of a method.
//      * @return The cache map for the given declaring class.
//      */
//     @Override
//     protected Map<MethodKey, T> computeValue(Class<?> declaringClass) {
//         return new HashMap<>();
//     }

//     /**
//      * Convenience method to get cache value for the given method.
//      * 
//      * @param method The method to retrieve cache value for.
//      * @param mappingFunction The function to create a new cache value if no value is
//      * associated to the method is found.
//      * @return The cache value associated for the method.
//      */
//     public T getCacheValueForMethod(Method method, Function<MethodKey, T> mappingFunction) {
//         // Get method cache of declaring class.
//         Map<MethodKey, T> methodCache = getCacheForMethod(method);

//         return methodCache.computeIfAbsent(
//             new MethodKey(method), 
//             mappingFunction
//         );
//     }

//      /**
//      * Convenience method to get cache value for the given method.
//      * 
//      * @param method The method to retrieve cache value for.
//      * @return The cache value associated for the method.
//      */
//     public T getCacheValueForMethod(Method method) {
//         // Get method cache of declaring class.
//         Map<MethodKey, T> methodCache = getCacheForMethod(method);

//         return methodCache.get(new MethodKey(method));
//     }

//     /**
//      * Get the method cache for the method's declaring class.
//      * 
//      * @param method The method to retrieve cache for. Method's declaring class will
//      * be used to retrieve the cache instance.
//      * @return The method cache for the method's declaring class.
//      */
//     public Map<MethodKey, T> getCacheForMethod(Method method) {
//         return get(method.getDeclaringClass());
//     }
// }