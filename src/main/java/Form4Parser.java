import csv.CSVBuilder;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import util.DailyData;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import java.io.*;

import java.util.*;
import java.util.regex.Matcher;

public class Form4Parser{

    //TODO: use this folder path for outputting
    public static String FOLDER_PATH = "data/";

    public Form4Parser() {
    }


    public void saveFile(String dirPath, String returnData, DailyData dailyData) throws IOException {
        //TODO: see if this still works
        File dir = new File(FOLDER_PATH + File.separator + dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String dailyDirPath = FOLDER_PATH + File.separator + dirPath + File.separator + dailyData.folderPath();
        File dailyDir = new File(dailyDirPath);
        if (!dailyDir.exists()) {
            dailyDir.mkdirs();
        }
        String currFilePath = dailyDirPath + File.separator + dailyData.folderPath().replaceAll("/", Matcher.quoteReplacement("."));
        File currFile = new File(currFilePath);
        if (currFile.exists()) {
            return;
        } else {
            currFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(currFile);
        fileWriter.write(returnData);
        fileWriter.close();
    }

    public void parseForm4String(String input, CSVBuilder csvBuilder) {
        String xml = testXMLTag(input, "XML");
        if (xml == null) {
            xml = testXMLTag(input, "SEC-DOCUMENT");
            if (xml == null) {
                //TODO: parse no XML
                return;
            }
            try {
                parseWellFormedXML(xml);
                return;
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        }
        try {
            xml = xml.substring(xml.indexOf("<?xml version"), xml.indexOf("</XML>") - 1);
            parseXML(xml, csvBuilder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseXML(String xml, CSVBuilder csvBuilder) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource data = new InputSource(new StringReader(xml));
        Document doc = db.parse(data);

        doc.getDocumentElement().normalize();

        HashMap<String, String> parsedData = new HashMap<>();

        Element root = doc.getDocumentElement();
        //TODO: used linkedlist here because...
        parseXMLNodesRec(root, new LinkedList<>(), csvBuilder);
    }


    private void parseXMLNodesRec(Node node, List<String> currTag, CSVBuilder csvBuilder) {
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
                parseXMLNodesRec(childNodes.item(i), new LinkedList<>(currTag), csvBuilder);
        }
    }

    private static String getText(Node node) {
        return node.getTextContent().trim().replaceAll("[\\n\\t]", "");
    }


    public void parseWellFormedXML(String xml) throws ParserConfigurationException, IOException, SAXException {
        //TODO: use Jsoup
    }

    //returns tested substring, null if not correct
    public String testXMLTag(String input, String xmlTag) {
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

    public boolean isTextNode(Node n) {
        return n.getNodeName().equals("#text");
    }
}
