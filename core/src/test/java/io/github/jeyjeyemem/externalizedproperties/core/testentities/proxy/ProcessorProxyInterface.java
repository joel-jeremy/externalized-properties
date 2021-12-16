package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ProcessorClasses;
import io.github.jeyjeyemem.externalizedproperties.core.processing.Base64Decode;

public interface ProcessorProxyInterface {
    @ExternalizedProperty("test.base64Decode")
    @ProcessorClasses(Base64Decode.class)
    String base64Decode();

    @ExternalizedProperty("test.base64Decode.int")
    @ProcessorClasses(Base64Decode.class)
    int base64DecodeInt();
}
