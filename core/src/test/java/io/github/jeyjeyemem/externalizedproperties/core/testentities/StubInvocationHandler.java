package io.github.jeyjeyemem.externalizedproperties.core.testentities;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * A stub {@link InvocationHandler} implementation.
 */
public class StubInvocationHandler implements InvocationHandler {
    
    public static final Function<Invocation, Object> DEFAULT_HANDLER =
        StubInvocationHandler::returnMethodName;

    public static final Function<Invocation, Object> NULL_HANDLER = 
        invocation -> null;

    private final List<Invocation> invocations = new ArrayList<>();
    private final Function<Invocation, Object> handler;

    public StubInvocationHandler() {
        // Always return the name of the invoked method.
        this(StubInvocationHandler::returnMethodName);
    }

    public StubInvocationHandler(Function<Invocation, Object> handler) {
        this.handler = handler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Invocation invocation = new Invocation(proxy, method, args);
        // Add for tracking.
        invocations.add(invocation);
        return handler.apply(invocation);
    }

    public List<Invocation> invocations() {
        return invocations;
    }

    private static Object returnMethodName(Invocation invocation) {
        return invocation.method().getName();
    }

    public static class Invocation {
        private final UUID id = UUID.randomUUID();
        private final Object proxy;
        private final Method method;
        private final Object[] args;

        public Invocation(Object proxy, Method method, Object[] args) {
            this.proxy = proxy;
            this.method = method;
            this.args = args;
        }

        public UUID id() {
            return id;
        }

        public Object proxy() {
            return proxy;
        }

        public Method method() {
            return method;
        }

        public Object[] args() {
            return args;
        }
    }
    
}
