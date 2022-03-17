// package io.github.jeyjeyemem.externalizedproperties.core.processing;

// import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

// public class ProcessingResult {
//     /**
//      * Singleton instance returned for every {@link ProcessingResult#skip()} 
//      * invocations.
//      */
//     private static final ProcessingResult SKIP = new ProcessingResult();

//     private final String value;
    
//     /** For {@link ProcessingResult#SKIP}. */
//     private ProcessingResult() {
//         this.value = null;
//     }

//     /**
//      * Constructor.
//      * 
//      * @param value The result value.
//      */
//     private ProcessingResult(String value) {
//         this.value = requireNonNull(value, "value");
//     }

//     /**
//      * The processing result value. 
//      * 
//      * @apiNote If invoked in an instance that was obtained from 
//      * {@link ProcessingResult#skip()} factory method, this method will throw an 
//      * {@link IllegalStateException}. One can check if instance is a skip result by 
//      * checking reference against {@link ProcessingResult#skip()} e.g. 
//      * {@code ProcessingResult.skip() == processingResult} or 
//      * {@code ProcessingResult.skip().equals(processingResult)}.
//      * 
//      * @return The processing result value. Otherwise, an {@link IllegalStateException} 
//      * is thrown.
//      * @throws IllegalStateException if instance was obtained from 
//      * {@link ProcessingResult#skip()} factory method and have no value.
//      */
//     public String value() {
//         if (value == null) {
//             throw new IllegalStateException(
//                 "Processing result does not contain a valid value."
//             );
//         }
//         return value;
//     }

//     /**
//      * Create an instance of {@link ProcessingResult} with the given value.
//      * 
//      * @param <T> The type of the result value.
//      * @param value The result value.
//      * @return The processing result containing the given value.
//      */
//     public static <T> ProcessingResult of(String value) {
//         return new ProcessingResult(value);
//     }

//     /**
//      * Returns a singleton instance of {@link ProcessingResult} which indicates that
//      * the processing handler is unsuccessful in processing and that the processor should 
//      * skip/move to the next registered processing handler in the processing pipeline.
//      * 
//      * @implSpec This always return the same object reference which means that
//      * {@code ProcessingResult.skip() == ProcessingResult.skip()} should evaluate to
//      * {@code true}.
//      * 
//      * @param <T> The type of the result value.
//      * @return A singleton instance of {@link ProcessingResult} which indicates that
//      * the processing handler is unsuccessful in processing and that the processor should 
//      * skip/move to the next registered processing handler in the processing pipeline.
//      */
//     public static <T> ProcessingResult skip() {
//         return SKIP;
//     };
// }
