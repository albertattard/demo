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
public final class Lolz {
    public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc2 = db.parse(new File("src/main/resources/lol2.xml"));
        Document doc3 = db.parse(new File("src/main/resources/lol3.xml"));
        Document doc4 = db.parse(new File("src/main/resources/lol4.xml"));

        System.out.println("lol_2 generated " + inventory(doc2) + " characters.");
        System.out.println("lol_3 generated " + inventory(doc3) + " characters.");
        System.out.println("lol_4 generated " + inventory(doc4) + " characters.");
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
