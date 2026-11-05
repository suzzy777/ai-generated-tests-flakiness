package com.intuit.karate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import java.util.List;
import java.util.Map;
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
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class XmlUtils_setByPath_12_0_Test {

    @Test
    void testSetByPath() throws Exception {
        // Create a sample XML document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        // Create root element
        Element root = doc.createElement("root");
        doc.appendChild(root);
        // Create a child element
        Element child = doc.createElement("child");
        child.setTextContent("original");
        root.appendChild(child);
        // Create a new node to insert
        Element newNode = doc.createElement("newChild");
        newNode.setTextContent("newContent");
        // Use reflection to call the private method
        java.lang.reflect.Method method = XmlUtils.class.getDeclaredMethod("setByPath", Document.class, String.class, Node.class);
        method.setAccessible(true);
        // Test replacing existing child node
        method.invoke(null, doc, "/root/child", newNode);
        // Verify that the child node was replaced
        Node replacedNode = doc.getElementsByTagName("child").item(0);
        assertNotNull(replacedNode);
        assertEquals("newContent", replacedNode.getTextContent());
        // Test appending to a node without text
        Element anotherChild = doc.createElement("anotherChild");
        root.appendChild(anotherChild);
        Element appendedNode = doc.createElement("appendedChild");
        appendedNode.setTextContent("appendedContent");
        method.invoke(null, doc, "/root/anotherChild", appendedNode);
        // Verify that the new node was appended
        Node appendedResultNode = doc.getElementsByTagName("appendedChild").item(0);
        assertNotNull(appendedResultNode);
        assertEquals("appendedContent", appendedResultNode.getTextContent());
    }
}
