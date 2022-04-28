package io.github.joeljeremy7.externalizedproperties.core.testentities.proxy;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.processing.Base64Decode;

public interface ProcessorProxyInterface {
    @ExternalizedProperty("test.base64Decode")
    @Base64Decode
    String base64Decode();

    @ExternalizedProperty("test.base64Decode.int")
    @Base64Decode
    int base64DecodeInt();

    @ExternalizedProperty("test.base64Decode.mime")
    @Base64Decode(encoding = "mime")
    String base64DecodeMime();


    @ExternalizedProperty("test.base64Decode.url")
    @Base64Decode(encoding = "url")
    String base64DecodeUrl();

    @ExternalizedProperty("test.base64Decode.basic")
    @Base64Decode(encoding = "basic")
    String base64DecodeBasic();

    @ExternalizedProperty("test.base64Decode.mime")
    @Base64Decode(charset = "UTF-16")
    String base64DecodeUtf16();
}
