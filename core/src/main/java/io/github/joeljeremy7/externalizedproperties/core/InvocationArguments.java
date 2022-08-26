package io.github.joeljeremy7.externalizedproperties.core;

import java.util.Optional;

/**
 * An immutable copy of the proxy method invocation arguments.
 */
public interface InvocationArguments {
    /**
     * The number of proxy method invocation arguments.
     * 
     * @return The number of proxy method invocation arguments.
     */
    int count();
    
    /**
     * Get the proxy method invocation argument array.
     * 
     * @return The proxy method invocation argument array.
     */
    Object[] get();

    /**
     * Get the proxy method invocation argument at the specified index.
     * 
     * @param index The index of the invocation argument to get.
     * @return The proxy method invocation argument at the specified index.
     * Otherwise, an empty {@link Optional} if no invocation argument exists
     * at the specified index.
     */
    Optional<Object> get(int index);

    /**
     * Get the proxy method invocation argument at the specified index, or 
     * throw if no invocation argument exists at the specified index.
     * 
     * @param index The index of the invocation argument to get.
     * @return The proxy method invocation argument at the specified index.
     * @throws IndexOutOfBoundsException if no invocation argument exists at 
     * the specified index. 
     */
    Object getOrThrow(int index);
}
