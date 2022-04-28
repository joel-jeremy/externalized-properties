package io.github.joeljeremy7.externalizedproperties.core.variableexpansion;

import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpanderProvider;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

/**
 * A no-op {@link VariableExpander} implementation.
 * Use this if variable expansion feature is not required
 * and better performance is desired.
 */
public class NoOpVariableExpander implements VariableExpander {

    /**
     * Singleton instance.
     */
    public static final NoOpVariableExpander INSTANCE = Singleton.INSTANCE;

    private NoOpVariableExpander(){}

    /**
     * The {@link VariableExpanderProvider} for {@link NoOpVariableExpander}.
     * 
     * @return The {@link VariableExpanderProvider} for {@link NoOpVariableExpander}.
     */
    public static VariableExpanderProvider<NoOpVariableExpander> provider() {
        return Singleton.PROVIDER;
    }

    /** {@inheritDoc} */
    @Override
    public String expandVariables(ProxyMethod proxyMethod, String value) {
        return value;
    }
    
    /**
     * Singleton holder.
     */
    private static final class Singleton {
        private static final NoOpVariableExpander INSTANCE = new NoOpVariableExpander();
        private static final VariableExpanderProvider<NoOpVariableExpander> PROVIDER = 
            externalizedProperties -> INSTANCE;
    }
}
