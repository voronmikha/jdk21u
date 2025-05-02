package ru.npsystems.transform;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * The XMLSignature Input
 */
public class XMLSignatureInput {
    /**
     * Construct a XMLSignatureInput
     */
    protected byte[] transformData;

    protected XMLSignatureInput(byte[] transformData) {
        this.transformData = transformData;
    }

    public InputStream getOctetStream() {
        return new ByteArrayInputStream(transformData);
    }
}
