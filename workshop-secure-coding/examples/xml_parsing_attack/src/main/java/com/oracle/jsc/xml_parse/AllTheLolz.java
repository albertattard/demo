package com.oracle.jsc.xml_parse;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Hello world!
 *
 */
public final class AllTheLolz {
    public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(new File("src/main/resources/lol.xml"));
        System.out.println("lol generated " + inventory(doc) + " characters.");
    }

    // depth-first traversal of the resulting document tree, using recursion.
    private static int inventory(Node document) {
        int n = 0;
        for (int i = 0; i < document.getChildNodes().getLength(); ++i) {
            Node node = document.getChildNodes().item(i);
            if (node.getTextContent() != null) {
                n += node.getTextContent().length();
            }
            n += inventory(node);
        }
        return n;
    }
}
