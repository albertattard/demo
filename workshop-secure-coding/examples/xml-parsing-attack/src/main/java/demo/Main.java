package demo;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public final class Main {

    static void main(final String[] args) throws SAXException, IOException, ParserConfigurationException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();

        if (args.length == 1 && "all".equalsIgnoreCase(args[0])) {
            final Document doc = db.parse(new File("src/main/resources/lol.xml"));
            System.out.println("lol generated " + inventory(doc) + " characters.");
        } else {
            final Document doc2 = db.parse(new File("src/main/resources/lol2.xml"));
            System.out.println("lol_2 generated " + inventory(doc2) + " characters.");

            final Document doc3 = db.parse(new File("src/main/resources/lol3.xml"));
            System.out.println("lol_3 generated " + inventory(doc3) + " characters.");

            final Document doc4 = db.parse(new File("src/main/resources/lol4.xml"));
            System.out.println("lol_4 generated " + inventory(doc4) + " characters.");
        }
    }

    // depth-first traversal of the resulting document tree, using recursion.
    private static int inventory(final Node document) {
        int n = 0;
        for (int i = 0; i < document.getChildNodes().getLength(); ++i) {
            final Node node = document.getChildNodes().item(i);
            if (node.getTextContent() != null) {
                n += node.getTextContent().length();
            }
            n += inventory(node);
        }
        return n;
    }
}
