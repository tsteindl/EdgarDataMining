import csv.CSVTableBuilder;
import interfaces.FormParser;
import interfaces.XMLConverter;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import util.InitException;
import util.OutputException;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import java.io.*;

import java.util.*;

public class Form4Parser extends FormParser {
    private static String XML_TAG = "XML";
    private static String WELL_FORMED_XML_TAG = "SEC-DOCUMENT";
    private static String XML_DOC_STARTING_TAG = "<?xml version";

    public Form4Parser(XMLConverter csvTableBuilder) {
        super("4", csvTableBuilder);
    }

    /**
     * inits outputter passed in constructor
     * @throws InitException
     */
    @Override
    public void init() throws InitException {
        outputter.init();
    }
    @Override
    public void parseForm(String input) throws OutputException {
        if (input == null) return;
        String xml = testXMLTag(input, XML_TAG);
        if (xml == null) {
            xml = testXMLTag(input, WELL_FORMED_XML_TAG);
            if (xml == null) {
                parseNoXML(xml);
                return;
            }
            parseWellFormedXML(xml);
        }
        try {
            xml = getXMLBody(xml);
            Element xmlRoot = getXMLTreeFromString(xml);

            parseXMLNodes(xmlRoot, new LinkedList<>(), (CSVTableBuilder) this.outputter);
            outputter.outputForm(); //TODO: maybe extract this into parent function
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    private static String getXMLBody(String xml) {
        return xml.substring(xml.indexOf(XML_DOC_STARTING_TAG), xml.indexOf("</" + XML_TAG+ ">") - 1);
    }

    public Element getXMLTreeFromString(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource data = new InputSource(new StringReader(xml));
        Document doc = db.parse(data);

        doc.getDocumentElement().normalize();

        return doc.getDocumentElement();
    }

/**
Used LinkedList here because only append operations are needed which are more efficient than on ArrayLists
 */
    private void parseXMLNodes(Node node, List<String> currTag, CSVTableBuilder csvBuilder) {
        if (node == null)
            return;
        if (isTextNode(node))
            csvBuilder.addEntryToCurrLine(currTag, getText(node));
        else if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element nodeElem = (Element) node;
            //don't include first tag in names
            currTag.add(nodeElem.getTagName());
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++)
                parseXMLNodes(childNodes.item(i), new LinkedList<>(currTag), csvBuilder);
        }
    }

    private static String getText(Node node) {
        return node.getTextContent().trim().replaceAll("[\\n\\t]", "");
    }

    private void parseWellFormedXML(String xml) {
        //TODO: use Jsoup
    }

    private void parseNoXML(String xml) {
        //TODO: implement
    }

    //returns tested substring, null if not correct
    private String testXMLTag(String input, String xmlTag) {
        String xml = input.trim();
        String startString = "<" + xmlTag + ">";
        String endString = "</" + xmlTag + ">";
        try {
            xml = xml.substring(xml.indexOf(startString), xml.indexOf(endString) + endString.length());
            if (xml.equals("")) {
                xml = null;
            }
        } catch (Exception e) {
            xml = null;
        }
        return xml;
    }

    private boolean isTextNode(Node n) {
        return n.getNodeName().equals("#text");
    }
}
