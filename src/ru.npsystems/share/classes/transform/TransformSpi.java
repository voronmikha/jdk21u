package ru.npsystems.transform;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import ru.npsystems.transform.InvalidCanonicalizerException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;

public abstract class TransformSpi {

    /**
     * Constructor
     */

    public TransformSpi() {
    }

    /**
     * The mega method which MUST be implemented by the Transformation Algorithm.
     */
    protected abstract ru.npsystems.transform.XMLSignatureInput enginePerformTransform(
            ru.npsystems.transform.XMLSignatureInput input, OutputStream os, Element transformElement,
            String baseURI, boolean secureValidation
    ) throws IOException, ru.npsystems.transform.CanonicalizationException, InvalidCanonicalizerException,
            ru.npsystems.transform.TransformationException, ParserConfigurationException, SAXException;

    /**
     * Returns the URI representation of <code>Transformation algorithm</code>
     *
     * @return the URI representation of <code>Transformation algorithm</code>
     */
    protected abstract String engineGetURI();
}

