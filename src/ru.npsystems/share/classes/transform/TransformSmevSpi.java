package ru.npsystems.transform;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import ru.npsystems.transform.CanonicalizationException;
import ru.npsystems.transform.InvalidCanonicalizerException;
import ru.npsystems.transform.TransformationException;
import ru.npsystems.transform.XMLSignatureInput;

import javax.xml.crypto.dsig.Transform;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Class implementing the urn://smev-gov-ru/xmldsig/transform transformation
 */

public class TransformSmevSpi extends ru.npsystems.transform.TransformSpi {

    public static final String ALGORITHM_URN = "urn://smev-gov-ru/xmldsig/transform";
    private static final String ENCODING_UTF_8 = "UTF-8";

    private static final AttributeSortingComparator attributeSortingComparator = new AttributeSortingComparator();
    private static final ThreadLocal<XMLInputFactory> inputFactory =
            ThreadLocal.withInitial(() -> XMLInputFactory.newInstance());

    private static final ThreadLocal<XMLOutputFactory> outputFactory =
            new ThreadLocal<XMLOutputFactory>() {
                @Override
                protected XMLOutputFactory initialValue() {
                    return XMLOutputFactory.newInstance();
                }
            };

    private static final ThreadLocal<XMLEventFactory> eventFactory =
            new ThreadLocal<XMLEventFactory>() {
                @Override
                protected XMLEventFactory initialValue() {
                    return XMLEventFactory.newInstance();
                }
            };

    /**
     * Constructor
     */
    private TransformSmevSpi() {
    }

    @Override
    protected String engineGetURI() {
        return ALGORITHM_URN;
    }

    @Override
    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput input, OutputStream os, Element transformElement, String baseURI, boolean secureValidation) throws IOException, CanonicalizationException, InvalidCanonicalizerException, TransformationException, ParserConfigurationException, SAXException {
        return null;
    }

    protected XMLSignatureInput enginePerformTransform(XMLSignatureInput argInput)
            throws IOException, TransformationException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        process(argInput.getOctetStream(), result);
        byte[] postTransformData = result.toByteArray();

        return new XMLSignatureInput(postTransformData);
    }

    /**
     * Transformation method
     *
     * @param argSrc InputStream
     * @param argDst OutputStream
     * @throws TransformationException
     */
    public void process(InputStream argSrc, OutputStream argDst) throws TransformationException {

        var prefixMappingStack = new Stack<List<Namespace>>();
        XMLEventReader src = null;
        XMLEventWriter dst = null;
        try {
            src = inputFactory.get().createXMLEventReader(argSrc, ENCODING_UTF_8);
            dst = outputFactory.get().createXMLEventWriter(argDst, ENCODING_UTF_8);
            XMLEventFactory factory = eventFactory.get();

            int prefixCnt = 1;
            while (src.hasNext()) {
                XMLEvent event = src.nextEvent();

                if (event.isCharacters()) {
                    String data = event.asCharacters().getData();
                    if (!data.trim().isEmpty()) {
                        dst.add(event);
                    }
                    continue;
                } else if (event.isStartElement()) {
                    List<Namespace> myPrefixMappings = new LinkedList<>();
                    prefixMappingStack.push(myPrefixMappings);

                    StartElement srcEvent = (StartElement) event;
                    String nsURI = srcEvent.getName().getNamespaceURI();
                    String prefix = findPrefix(nsURI, prefixMappingStack);

                    if (prefix == null) {
                        prefix = "ns" + String.valueOf(prefixCnt++);
                        myPrefixMappings.add(factory.createNamespace(prefix, nsURI));
                    }
                    StartElement dstEvent = factory.createStartElement(prefix, nsURI,
                            srcEvent.getName().getLocalPart());
                    dst.add(dstEvent);

                    Iterator<Attribute> srcAttributeIterator = srcEvent.getAttributes();
                    List<Attribute> srcAttributeList = new LinkedList<>();
                    while (srcAttributeIterator.hasNext()) {
                        srcAttributeList.add(srcAttributeIterator.next());
                    }
                    Collections.sort(srcAttributeList, attributeSortingComparator);

                    List<Attribute> dstAttributeList = new LinkedList<>();
                    for (Attribute srcAttribute : srcAttributeList) {
                        String attributeNsURI = srcAttribute.getName().getNamespaceURI();
                        String attributeLocalName = srcAttribute.getName().getLocalPart();
                        String value = srcAttribute.getValue();
                        Attribute dstAttribute;
                        if (attributeNsURI != null && !"".equals(attributeNsURI)) {
                            String attributePrefix = findPrefix(attributeNsURI, prefixMappingStack);
                            if (attributePrefix == null) {
                                attributePrefix = "ns" + String.valueOf(prefixCnt++);
                                myPrefixMappings.add(factory.createNamespace(attributePrefix, attributeNsURI));
                            }
                            dstAttribute = factory.createAttribute(attributePrefix, attributeNsURI,
                                    attributeLocalName, value);
                        } else {
                            dstAttribute = factory.createAttribute(attributeLocalName, value);
                        }
                        dstAttributeList.add(dstAttribute);
                    }
                    for (Namespace mapping : myPrefixMappings) {
                        dst.add(mapping);
                    }
                    for (Attribute attr : dstAttributeList) {
                        dst.add(attr);
                    }
                    continue;
                } else if (event.isEndElement()) {
                    dst.add(eventFactory.get().createSpace(""));
                    EndElement srcEvent = (EndElement) event;
                    String nsURI = srcEvent.getName().getNamespaceURI();
                    String prefix = findPrefix(nsURI, prefixMappingStack);
                    if (prefix == null) {
                        throw new TransformationException();
                    }

                    EndElement dstEvent = eventFactory.get()
                            .createEndElement(prefix, nsURI, srcEvent.getName().getLocalPart());
                    dst.add(dstEvent);

                    prefixMappingStack.pop();
                    continue;
                } else if (event.isAttribute()) {
                    continue;
                }
            }
        } catch (XMLStreamException e) {
            Object[] exArgs = {e.getMessage()};
            throw new TransformationException();
        } finally {
            if (src != null) {
                try {
                    src.close();
                } catch (XMLStreamException e) {
                }
            }
            if (dst != null) {
                try {
                    dst.close();
                } catch (XMLStreamException e) {
                }
            }
            try {
                argSrc.close();
            } catch (IOException e) {
            }
            if (argDst != null) {
                try {
                    argDst.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static String findPrefix(String argNamespaceURI, Stack<List<Namespace>> argMappingStack) {
        if (argNamespaceURI == null) {
            throw new IllegalArgumentException("No namespace elements are not supported");
        }

        for (List<Namespace> elementMappingList : argMappingStack) {
            for (Namespace mapping : elementMappingList) {
                if (argNamespaceURI.equals(mapping.getNamespaceURI())) {
                    return mapping.getPrefix();
                }
            }
        }
        return null;
    }


    private static class AttributeSortingComparator implements Comparator<Attribute> {

        @Override
        public int compare(Attribute x, Attribute y) {
            String xNS = x.getName().getNamespaceURI();
            String xLocal = x.getName().getLocalPart();
            String yNS = y.getName().getNamespaceURI();
            String yLocal = y.getName().getLocalPart();

            if (empty(xNS) && empty(yNS)) {
                return xLocal.compareTo(yLocal);
            }

            if (!empty(xNS) && !empty(yNS)) {
                int nsComparisonResult = xNS.compareTo(yNS);
                if (nsComparisonResult != 0) {
                    return nsComparisonResult;
                } else {
                    return xLocal.compareTo(yLocal);
                }
            }

            if (empty(xNS)) {
                return 1;
            } else {
                return -1;
            }
        }

        private static boolean empty(String arg) {
            return arg == null || "".equals(arg);
        }
    }
}
