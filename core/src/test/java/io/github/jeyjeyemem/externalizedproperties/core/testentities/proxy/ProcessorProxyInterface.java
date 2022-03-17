package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ProcessingContext;
import io.github.jeyjeyemem.externalizedproperties.core.Processor;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ProcessorAttribute;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ProcessorClasses;
import io.github.jeyjeyemem.externalizedproperties.core.processing.Base64Decode;

public interface ProcessorProxyInterface {
    @ExternalizedProperty("test.base64Decode")
    @ProcessorClasses(Base64Decode.class)
    String base64Decode();

    @ExternalizedProperty("test.base64Decode.int")
    @ProcessorClasses(Base64Decode.class)
    int base64DecodeInt();

    @ExternalizedProperty("test.base64Decode.attributes")
    @ProcessorClasses(
        value = Base64Decode.class,
        attributes = {
            @ProcessorAttribute(
                name = "Base64Decode.testAttribute",
                value = "Base64Decode.testAttributeValue",
                forProcessors = Base64Decode.class
            )
        }
    )
    String base64DecodeWithAttribute();

    @ExternalizedProperty("test.base64Decode.attributes.specific")
    @ProcessorClasses(
        value = {
            Base64Decode.class,
            TestProcessor.class
        },
        attributes = {
            @ProcessorAttribute(
                name = "Base64Decode.testAttribute",
                value = "Base64Decode.testAttributeValue",
                forProcessors = Base64Decode.class
            ),
            @ProcessorAttribute(
                name = "TestProcessor.testAttribute",
                value = "TestProcessor.testAttributeValue",
                forProcessors = TestProcessor.class
            ),
            @ProcessorAttribute(
                name = "Shared.testAttribute",
                value = "Shared.testAttributeValue"
            )
        }
    )
    String specificAttributes();
    
    public static class TestProcessor implements Processor {
        @Override
        public String process(ProcessingContext context) {
            return getClass().getName() + "|" + context.value();
        }
    }
}
