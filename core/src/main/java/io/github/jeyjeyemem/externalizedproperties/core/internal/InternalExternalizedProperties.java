package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.Processor;
import io.github.jeyjeyemem.externalizedproperties.core.Processors;
import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.TypeReference;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ProcessorClasses;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ProcessingException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.processing.ProcessorRegistry;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.InvocationHandlerFactory;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethodInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The default {@link ExternalizedProperties} implementation.
 */
public class InternalExternalizedProperties implements ExternalizedProperties {

    private final Resolver resolver;
    private final ProcessorRegistry processorRegistry;
    private final Converter converter;
    private final VariableExpander variableExpander;
    private final InvocationHandlerFactory invocationHandlerFactory;

    /**
     * Constructor.
     * 
     * @param resolver The resolver.
     * @param processorRegistry The processor registry.
     * @param converter The converter.
     * @param variableExpander The variable expander.
     * @param invocationHandlerFactory The invocation handler factory.
     */
    public InternalExternalizedProperties(
            Resolver resolver,
            ProcessorRegistry processorRegistry,
            Converter converter,
            VariableExpander variableExpander,
            InvocationHandlerFactory invocationHandlerFactory
    ) {
        this.resolver = requireNonNull(resolver, "resolver");
        this.converter = requireNonNull(converter, "converter");
        this.processorRegistry = requireNonNull(processorRegistry, "processorRegistry");
        this.variableExpander = requireNonNull(variableExpander, "variableExpander");
        this.invocationHandlerFactory = requireNonNull(
            invocationHandlerFactory, 
            "invocationHandlerFactory"
        );
    }

    /** {@inheritDoc} */
    @Override
    public <T> T proxy(Class<T> proxyInterface) {
        requireNonNull(proxyInterface, "proxyInterface");
        return proxy(proxyInterface, proxyInterface.getClassLoader());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T proxy(Class<T> proxyInterface, ClassLoader classLoader) {
        requireNonNull(proxyInterface, "proxyInterface");
        requireNonNull(classLoader, "classLoader");

        validate(proxyInterface);
        
        return (T) Proxy.newProxyInstance(
            classLoader, 
            new Class<?>[] { proxyInterface },
            invocationHandlerFactory.createInvocationHandler(this, proxyInterface)
        );
    }

    /** {@inheritDoc} */
    @Override
    public Optional<?> resolveProperty(ProxyMethodInfo proxyMethodInfo) {
        requireNonNull(proxyMethodInfo, "proxyMethodInfo");
        
        String propertyName = proxyMethodInfo.externalizedPropertyName().orElseThrow(
            () -> new IllegalArgumentException(
                "Proxy method info externalized property name cannot be determined."
            )
        );

        Optional<ProcessorClasses> processorsClassesAnnotation = 
            proxyMethodInfo.findAnnotation(ProcessorClasses.class);

        Optional<String> resolved;
        if (processorsClassesAnnotation.isPresent()) {
            resolved = resolveProperty(
                propertyName, 
                Processors.of(processorsClassesAnnotation.get())
            );
        } else {
            resolved = resolveProperty(propertyName);
        }

        return resolved.map(resolvedValue -> converter.convert(
            new ConversionContext(
                converter, 
                proxyMethodInfo, 
                resolvedValue
            )
        ));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> resolveProperty(String propertyName) {
        return resolver.resolve(propertyName);
    }
    
    /** {@inheritDoc} */
    @Override
    public Optional<String> resolveProperty(
            String propertyName,
            Processors processors
    ) {
        requireNonNull(processors, "processors");

        if (processors == Processors.NONE) {
            return resolveProperty(propertyName);
        }

        return resolveProperty(propertyName).map(resolvedValue -> 
            processProperty(resolvedValue, processors)
        );
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolveProperty(
            String propertyName,
            Class<T> targetType
    ) {
        return (Optional<T>)resolveProperty(propertyName, (Type)targetType);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolveProperty(
            String propertyName,
            Processors processors,
            Class<T> targetType
    ) {
        return (Optional<T>)resolveProperty(
            propertyName,
            processors,
            (Type)targetType
        );
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolveProperty(
            String propertyName,
            TypeReference<T> targetType
    ) {
        return (Optional<T>)resolveProperty(
            propertyName,
            requireNonNull(targetType, "targetType").type()
        );
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolveProperty(
            String propertyName,
            Processors processors,
            TypeReference<T> targetType
    ) {
        return (Optional<T>)resolveProperty(
            propertyName,
            processors,
            requireNonNull(targetType, "targetType").type()
        );
    }

    /** {@inheritDoc} */
    @Override
    public Optional<?> resolveProperty(
            String propertyName,
            Type targetType
    ) {
        return resolveProperty(propertyName).map(resolvedValue -> 
            converter.convert(new ConversionContext(
                converter,
                targetType,
                resolvedValue
            ))
        );
    }

    /** {@inheritDoc} */
    @Override
    public Optional<?> resolveProperty(
            String propertyName,
            Processors processors,
            Type targetType
    ) {
        Optional<String> resolved = resolveProperty(propertyName, processors);

        if (String.class.equals(targetType)) {
            return resolved;
        }

        return resolved.map(resolvedValue -> converter.convert(
            new ConversionContext(
                converter,
                targetType,
                resolvedValue
            )
        ));
    }

    /** {@inheritDoc} */
    @Override
    public String expandVariables(String source) {
        return variableExpander.expandVariables(source);
    }

    private String processProperty(
        String property, 
        Processors processors
    ) {
        try {
            List<Processor> processorInstances = 
                processorRegistry.getProcessors(processors);
            
            String processed = property;
            for (Processor processor : processorInstances) {
                processed = processor.processProperty(property);
            }
            return processed;
        } catch (Exception ex) {
            throw new ProcessingException("Error occurred during processing.", ex);
        }
    }

    private <T> void validate(Class<T> proxyInterface) {
        requireNoVoidReturningMethods(proxyInterface);
    }

    private <T> void requireNoVoidReturningMethods(Class<T> proxyInterface) {
        List<String> voidReturningMethods = Arrays.stream(proxyInterface.getMethods())
            .filter(m -> m.getReturnType().equals(Void.TYPE))
            .map(Method::toGenericString)
            .collect(Collectors.toList());

        if (!voidReturningMethods.isEmpty()) {
            throw new IllegalArgumentException(
                "Proxy interface methods must not return void. " +
                "Invalid Methods: " + voidReturningMethods
            );
        }
    }
}
