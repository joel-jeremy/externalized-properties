package io.github.jeyjeyemem.externalizedproperties.core.internal.processing;

import io.github.jeyjeyemem.externalizedproperties.core.Processor;
import io.github.jeyjeyemem.externalizedproperties.core.Processors;
import io.github.jeyjeyemem.externalizedproperties.core.processing.Base64Decode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProcessorRegistryTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when processors argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new ProcessorRegistry(null)
            );
        }
    }

    @Nested
    class GetProcessorsMethod {
        @Test
        @DisplayName("should throw when processors argument is null")
        public void test1() {
            List<Processor> processors = Arrays.asList(
                new FirstProcessor(),
                new SecondProcessor(),
                new ThirdProcessor(),
                new FourthProcessor(),
                new FifthProcessor(),
                new Base64Decode()
            );

            ProcessorRegistry registry = 
                new ProcessorRegistry(processors);
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> registry.getProcessors(null)
            );
        }

        @Test
        @DisplayName("should throw when processors argument is null")
        public void test2() {
            List<Processor> processors = Arrays.asList(
                new FirstProcessor(),
                new SecondProcessor(),
                new ThirdProcessor(),
                new FourthProcessor(),
                new FifthProcessor(),
                new Base64Decode()
            );

            ProcessorRegistry registry = 
                new ProcessorRegistry(processors);
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> registry.getProcessors(null)
            );
        }

        @Test
        @DisplayName("should return processors that matches processor classes")
        public void test3() {
            List<Processor> processors = Arrays.asList(
                new FirstProcessor(),
                new SecondProcessor(),
                new ThirdProcessor(),
                new FourthProcessor(),
                new FifthProcessor(),
                new Base64Decode()
            );

            ProcessorRegistry registry = 
                new ProcessorRegistry(processors);
            
            List<Processor> result = 
                registry.getProcessors(Processors.of(Arrays.asList(
                    FirstProcessor.class,
                    SecondProcessor.class,
                    ThirdProcessor.class,
                    FourthProcessor.class,
                    FifthProcessor.class,
                    Base64Decode.class
                )));

            assertEquals(6, result.size());
            assertIterableEquals(processors, result);
        }

        @Test
        @DisplayName(
            "should return empty list when processors argument is Processors.NONE"
        )
        public void test4() {
            // No Base64Decode registered.
            List<Processor> processors = Arrays.asList(
                new FirstProcessor(),
                new SecondProcessor(),
                new ThirdProcessor(),
                new FourthProcessor(),
                new FifthProcessor()
            );

            ProcessorRegistry registry = 
                new ProcessorRegistry(processors);
            
            
            List<Processor> result = 
                registry.getProcessors(Processors.NONE);
            
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName(
            "should throw when a processor class does not match a registered processor"
        )
        public void test5() {
            // No Base64Decode registered.
            List<Processor> processors = Arrays.asList(
                new FirstProcessor(),
                new SecondProcessor(),
                new ThirdProcessor(),
                new FourthProcessor(),
                new FifthProcessor()
            );

            ProcessorRegistry registry = 
                new ProcessorRegistry(processors);
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> registry.getProcessors(Processors.of(Base64Decode.class))
            );
        }
    }

    public static class FirstProcessor extends ProcessorBase {}
    public static class SecondProcessor extends ProcessorBase {}
    public static class ThirdProcessor extends ProcessorBase {}
    public static class FourthProcessor extends ProcessorBase {}
    public static class FifthProcessor extends ProcessorBase {}

    public static class ProcessorBase implements Processor {
        @Override
        public String processProperty(String property) {
            return property + " (" + getClass().getTypeName() + ")";
        }
    }
}
