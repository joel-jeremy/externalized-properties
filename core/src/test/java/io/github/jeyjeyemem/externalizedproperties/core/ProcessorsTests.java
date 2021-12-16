package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ProcessorClasses;
import io.github.jeyjeyemem.externalizedproperties.core.processing.Base64Decode;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubProxyMethodInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProcessorsTests {
    @Nested
    class OfMethodWithOneProcessorClassOverload {
        @Test
        @DisplayName("should throw when processorClass argument is null.")
        public void oneProcessorTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of((Class<? extends Processor>)null)
            );
        }
    }

    @Nested
    class OfMethodWithTwoProcessorClassesOverload {
        @Test
        @DisplayName("should throw when first argument is null.")
        public void twoProcessorsTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(null, SecondProcessor.class)
            );
        }

        @Test
        @DisplayName("should throw when second argument is null.")
        public void twoProcessorsTest2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(FirstProcessor.class, null)
            );
        }
    }

    @Nested
    class OfMethodWithThreeProcessorClassesOverload {
        @Test
        @DisplayName("should throw when first argument is null.")
        public void threeProcessorsTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(null, SecondProcessor.class, ThirdProcessor.class)
            );
        }

        @Test
        @DisplayName("should throw when second argument is null.")
        public void threeProcessorsTest2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(FirstProcessor.class, null, ThirdProcessor.class)
            );
        }

        @Test
        @DisplayName("should throw when third argument is null.")
        public void threeProcessorsTest3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(FirstProcessor.class, SecondProcessor.class, null)
            );
        }
    }

    @Nested
    class OfMethodWithFourProcessorClassesOverload {
        @Test
        @DisplayName("should throw when first argument is null.")
        public void fourProcessorsTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(
                    null,
                    SecondProcessor.class,
                    ThirdProcessor.class,
                    FourthProcessor.class
                )
            );
        }

        @Test
        @DisplayName("should throw when second argument is null.")
        public void fourProcessorsTest2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(
                    FirstProcessor.class,
                    null,
                    ThirdProcessor.class,
                    FourthProcessor.class
                )
            );
        }

        @Test
        @DisplayName("should throw when third argument is null.")
        public void fourProcessorsTest3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(
                    FirstProcessor.class, 
                    SecondProcessor.class, 
                    null,
                    FourthProcessor.class
                )
            );
        }

        @Test
        @DisplayName("should throw when fourth argument is null.")
        public void fourProcessorsTest4() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(
                    FirstProcessor.class, 
                    SecondProcessor.class,
                    ThirdProcessor.class, 
                    null
                )
            );
        }
    }

    @Nested
    class OfMethodWithFiveProcessorClassesOverload {
        @Test
        @DisplayName("should throw when first argument is null.")
        public void fiveProcessorsTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(
                    null,
                    SecondProcessor.class,
                    ThirdProcessor.class,
                    FourthProcessor.class,
                    FifthProcessor.class
                )
            );
        }

        @Test
        @DisplayName("should throw when second argument is null.")
        public void fiveProcessorsTest2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(
                    FirstProcessor.class,
                    null,
                    ThirdProcessor.class,
                    FourthProcessor.class,
                    FifthProcessor.class
                )
            );
        }

        @Test
        @DisplayName("should throw when third argument is null.")
        public void fiveProcessorsTest3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(
                    FirstProcessor.class, 
                    SecondProcessor.class, 
                    null,
                    FourthProcessor.class,
                    FifthProcessor.class
                )
            );
        }

        @Test
        @DisplayName("should throw when fourth argument is null.")
        public void fiveProcessorsTest4() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(
                    FirstProcessor.class, 
                    SecondProcessor.class,
                    ThirdProcessor.class, 
                    null,
                    FifthProcessor.class
                )
            );
        }

        @Test
        @DisplayName("should throw when fifth argument is null.")
        public void fiveProcessorsTest5() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(
                    FirstProcessor.class, 
                    SecondProcessor.class,
                    ThirdProcessor.class, 
                    FourthProcessor.class,
                    null
                )
            );
        }
    }

    @Nested
    class OfMethodWithProcessorClassesOverload {
        @Test
        @DisplayName("should throw when processor classes argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of((ProcessorClasses)null)
            );
        }

        @Test
        @DisplayName("should return Processors.NONE when list argument is empty.")
        public void test2() {
            StubProxyMethodInfo methodInfo = StubProxyMethodInfo.fromMethod(
                ProcessorInterface.class, 
                "testEmptyProcessors"
            );

            ProcessorClasses processorClasses = 
                methodInfo.findAnnotation(ProcessorClasses.class)
                    .orElseThrow(() -> 
                        new IllegalStateException("No ProcessorClasses annotation.")
                    );
            
            Processors processors = Processors.of(processorClasses);
            assertSame(Processors.NONE, processors);
        }
    }

    @Nested
    class OfMethodWithListOverload {
        @Test
        @DisplayName("should throw when list argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Processors.of(
                    (List<Class<? extends Processor>>)null
                )
            );
        }

        @Test
        @DisplayName("should return Processors.NONE when list argument is empty.")
        public void test2() {
            Processors processors = Processors.of(Collections.emptyList());
            assertSame(Processors.NONE, processors);
        }
    }

    @Nested
    class ListMethod {
        @Test
        @DisplayName("should return processor class list in the same order.")
        public void oneProcessorClassTest() {
            Processors processors = Processors.of(FirstProcessor.class);

            List<Class<? extends Processor>> list = 
                processors.list();

            assertEquals(1, list.size());
            assertEquals(FirstProcessor.class, list.get(0));
        }

        @Test
        @DisplayName("should return processor class list in the same order.")
        public void twoProcessorClassesTest() {
            Processors processors = Processors.of(
                FirstProcessor.class,
                SecondProcessor.class
            );

            List<Class<? extends Processor>> list = 
                processors.list();

            assertEquals(2, list.size());
            assertEquals(FirstProcessor.class, list.get(0));
            assertEquals(SecondProcessor.class, list.get(1));
        }

        @Test
        @DisplayName("should return processor class list in the same order.")
        public void threeProcessorClassesTest() {
            Processors processors = Processors.of(
                FirstProcessor.class,
                SecondProcessor.class,
                ThirdProcessor.class
            );

            List<Class<? extends Processor>> list = 
                processors.list();

            assertEquals(3, list.size());
            assertEquals(FirstProcessor.class, list.get(0));
            assertEquals(SecondProcessor.class, list.get(1));
            assertEquals(ThirdProcessor.class, list.get(2));
        }

        @Test
        @DisplayName("should return processor class list in the same order.")
        public void fourProcessorClassesTest() {
            Processors processors = Processors.of(
                FirstProcessor.class,
                SecondProcessor.class,
                ThirdProcessor.class,
                FourthProcessor.class
            );

            List<Class<? extends Processor>> list = 
                processors.list();

            assertEquals(4, list.size());
            assertEquals(FirstProcessor.class, list.get(0));
            assertEquals(SecondProcessor.class, list.get(1));
            assertEquals(ThirdProcessor.class, list.get(2));
            assertEquals(FourthProcessor.class, list.get(3));
        }

        @Test
        @DisplayName("should return processor class list in the same order.")
        public void fiveProcessorClassesTest() {
            Processors processors = Processors.of(
                FirstProcessor.class,
                SecondProcessor.class,
                ThirdProcessor.class,
                FourthProcessor.class,
                FifthProcessor.class
            );

            List<Class<? extends Processor>> list = 
                processors.list();

            assertEquals(5, list.size());
            assertEquals(FirstProcessor.class, list.get(0));
            assertEquals(SecondProcessor.class, list.get(1));
            assertEquals(ThirdProcessor.class, list.get(2));
            assertEquals(FourthProcessor.class, list.get(3));
            assertEquals(FifthProcessor.class, list.get(4));
        }

        @Test
        @DisplayName("should return processor class list in the same order.")
        public void processorClassesAnnotationTest() {
            StubProxyMethodInfo methodInfo = StubProxyMethodInfo.fromMethod(
                ProcessorInterface.class, 
                "testProcessors"
            );

            ProcessorClasses processorClasses = 
                methodInfo.findAnnotation(ProcessorClasses.class)
                    .orElseThrow(() -> 
                        new IllegalStateException("No ProcessorClasses annotation.")
                    );
                
            Processors processors = Processors.of(processorClasses);

            List<Class<? extends Processor>> list = 
                processors.list();

            assertEquals(5, list.size());
            assertEquals(FirstProcessor.class, list.get(0));
            assertEquals(SecondProcessor.class, list.get(1));
            assertEquals(ThirdProcessor.class, list.get(2));
            assertEquals(FourthProcessor.class, list.get(3));
            assertEquals(FifthProcessor.class, list.get(4));
        }

        @Test
        @DisplayName("should return processor class list in the same order.")
        public void listProcessorClassesTest() {
            Processors processors = Processors.of(
                Arrays.asList(
                    FirstProcessor.class,
                    SecondProcessor.class,
                    ThirdProcessor.class,
                    FourthProcessor.class,
                    FifthProcessor.class,
                    Base64Decode.class
                )
            );

            List<Class<? extends Processor>> list = 
                processors.list();

            assertEquals(6, list.size());
            assertEquals(FirstProcessor.class, list.get(0));
            assertEquals(SecondProcessor.class, list.get(1));
            assertEquals(ThirdProcessor.class, list.get(2));
            assertEquals(FourthProcessor.class, list.get(3));
            assertEquals(FifthProcessor.class, list.get(4));
            assertEquals(Base64Decode.class, list.get(5));
        }

        @Test
        @DisplayName("should return unmodifiable list.")
        public void unmodifiableTest() {
            Processors processors = Processors.of(
                Arrays.asList(
                    FirstProcessor.class,
                    SecondProcessor.class,
                    ThirdProcessor.class,
                    FourthProcessor.class,
                    FifthProcessor.class,
                    Base64Decode.class
                )
            );

            List<Class<? extends Processor>> list = 
                processors.list();

            verifyUnmodifiableCollection(list, () -> Base64Decode.class);
        }
    }

    @Nested
    class ToStringMethod {
        @Test
        @DisplayName("should return list toString result.")
        public void test1() {
            List<Class<? extends Processor>> processorClasses =
                Arrays.asList(
                    FirstProcessor.class,
                    SecondProcessor.class,
                    ThirdProcessor.class,
                    FourthProcessor.class,
                    FifthProcessor.class
                );
            
            Processors processors = Processors.of(processorClasses);

            assertEquals(processorClasses.toString(), processors.toString());
        }
    }

    private <T> void verifyUnmodifiableCollection(
            Collection<T> collectionToVerify, 
            Supplier<T> itemSupplier
    ) {
        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.add(itemSupplier.get())
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.remove(itemSupplier.get())
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.addAll(
                Collections.singletonList(itemSupplier.get())
            )
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.clear()
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.removeAll(
                Collections.singletonList(itemSupplier.get())
            )
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.removeIf(r -> true)
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.retainAll(
                Collections.singletonList(itemSupplier.get())
            )
        );

        assertThrows(
            UnsupportedOperationException.class,
            () -> collectionToVerify.iterator().remove()
        );
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

    public static interface ProcessorInterface {
        @ExternalizedProperty("test")
        @ProcessorClasses({
            FirstProcessor.class,
            SecondProcessor.class,
            ThirdProcessor.class,
            FourthProcessor.class,
            FifthProcessor.class
        })
        String testProcessors();

        @ExternalizedProperty("test")
        @ProcessorClasses({}) // Empty array.
        String testEmptyProcessors();
    }
}
