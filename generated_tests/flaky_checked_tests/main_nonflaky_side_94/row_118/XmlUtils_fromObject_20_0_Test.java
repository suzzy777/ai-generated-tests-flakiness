package com.intuit.karate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlUtils_fromObject_20_0_Test {

    @Test
    public void testFromObject() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Arrange
        String name = "root";
        Object object = "sampleValue";
        // Act
        Document result = XmlUtils.fromObject(name, object);
        // Assert
        assertNotNull(result);
        assertEquals(name, result.getDocumentElement().getTagName());
        // Using reflection to invoke the private method 'fromObject' to test its logic
        Method fromObjectMethod = XmlUtils.class.getDeclaredMethod("fromObject", Document.class, String.class, Object.class);
        fromObjectMethod.setAccessible(true);
        List<Element> elements = (List<Element>) fromObjectMethod.invoke(null, result, name, object);
        assertNotNull(elements);
        assertFalse(elements.isEmpty());
        assertEquals(name, elements.get(0).getTagName());
    }
}
