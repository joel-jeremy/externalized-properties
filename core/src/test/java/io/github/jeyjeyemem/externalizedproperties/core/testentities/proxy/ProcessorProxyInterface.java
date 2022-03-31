package io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.processing.Base64Decode;

public interface ProcessorProxyInterface {
    @ExternalizedProperty("test.base64Decode")
    @Base64Decode
    String base64Decode();

    @ExternalizedProperty("test.base64Decode.int")
    @Base64Decode
    int base64DecodeInt();
}
