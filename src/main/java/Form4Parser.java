import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import csv.CSVBuilder;
import csv.CSVTableBuilder;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import java.io.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class Form4Parser extends Parser {

    public static final String FOLDER_PATH = "data" + File.separator + "forms";

    public static final String PATH_META_TABLE_FORM4 = "data/meta_table_form4.csv";
    public HashMap<String, String> FORM_4_SET;

    public Form4Parser(ArrayList<DailyData> dailyDataList) {
        super(dailyDataList);
        //----------
        /*
        try {
            FileReader filereader = null;
            try {
                filereader = new FileReader(PATH_META_TABLE_FORM4);
                CSVReader csvReader = new CSVReader(filereader);
                String[] record;
                FORM_4_SET = new HashMap<>();
                for (int i = 0; (record = csvReader.readNext()) != null; i++) {
                    if (i == 0) continue;
                    FORM_4_SET.put(record[0].split(";")[1], record[0].split(";")[0]);
                }
            } catch (CsvValidationException | IOException e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
         */
        FORM_4_SET = new HashMap<>();
        //reset meta table
        FORM_4_SET.clear();
        FORM_4_SET.put("form_folder_path", "form_folder_path");
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

    //TODO: reduce amount of functions called, choose better names for functions
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

    public HashMap<String, String> parseXML(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
//             parse XML file
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource data = new InputSource(new StringReader(xml));
        Document doc = db.parse(data);

        doc.getDocumentElement().normalize();

        HashMap<String, String> parsedData = new HashMap<>();

        Element root = doc.getDocumentElement();


        getXMLNodesRecursively(parsedData, FORM_4_SET, root, null);

        /*
        for (String xmlTag : FORM_4_SET.keySet()) {

//            Node n = (Node) doc.getElementsByTagName(xmlTag).item(0);
            NodeList nl = doc.getElementsByTagName(xmlTag);
            int nodeLength = nl.getLength();
            //iterate over nodelist if it is a list (length > 1)
            for (int i = 0; i < nodeLength; i++) {

            }
            Node n = (Node) doc.getElementsByTagName(xmlTag);


            if (n == null) {
                parsedData.put(FORM_4_SET.get(xmlTag), "");
            } else {
                String temp = n.getTextContent();
                parsedData.put(FORM_4_SET.get(xmlTag), temp.trim().replaceAll("[\\n\\t]", ""));
            }
        }

         */
        return parsedData;
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


    private void getXMLNodesRecursively(HashMap<String, String> parsedData, HashMap<String, String> FORM_4_SET, Node node, String currentTag) {
        if (node == null) {
            return;
        }
        if (isTextNode(node)) {
            String text = node.getTextContent().trim().replaceAll("[\\n\\t]", "");
            if (!text.equals("")) {
                //if node has same name make it distinguishable
                String tag = currentTag;
                int n = 1;
                while (parsedData.containsKey(tag)) {
                    tag = currentTag + n;
                    n++;
                }
                if (!FORM_4_SET.containsKey(tag)) FORM_4_SET.put(tag, tag);

                parsedData.put(tag, text);
            }
        }
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element nodeElem = (Element) node;
            //don't include first tag in names
            currentTag = (currentTag == null) ? "" : currentTag + ">" + nodeElem.getTagName();
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node n = childNodes.item(i);
                getXMLNodesRecursively(parsedData, FORM_4_SET, n, currentTag);
            }
        }
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

    public static void createOutPutCsv(HashMap<String, String> csvDataHeader, String dirPath) {
        try {
            dirPath = dirPath.replaceAll("/", "");
            File csvFile = new File("data" + File.separator + dirPath + ".csv");

            FileWriter outputFile = new FileWriter(csvFile, true);

            CSVWriter writer = new CSVWriter(outputFile);

            List<String[]> data = new ArrayList<>();

            //include header
            data.add(csvDataHeader.keySet().toArray(new String[0]));

            writer.writeAll(data);
            writer.close();

            System.out.println("\n------------------------------------");
            System.out.println("Created csv: " + "data" + File.separator + dirPath + ".csv");
            System.out.println("------------------------------------");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendToOutPutCSV(ArrayList<HashMap<String, String>> csvData, HashMap<String, String> metaTable, String addPath) {
        try {
            addPath = addPath.replaceAll("/", "");
            File csvFile = new File("data" + File.separator + addPath + ".csv");

            FileWriter outputFile = new FileWriter(csvFile, true);
            CSVWriter writer = new CSVWriter(outputFile);

            List<String[]> data = new ArrayList<>();

            /*
            int nOColumns = csvData.get(0).keySet().size();
            for (HashMap<String, String> record : csvData) {
                String[] output = new String[nOColumns];
                for (int i = 0; i < nOColumns; i++) {
                    String column = record.keySet().toArray(new String[0])[i];
                    output[i] = record.get(column);
                }
                data.add(output);
            }

             */

            int nOColumns = metaTable.keySet().size();
            for (HashMap<String, String> record : csvData) {
                String[] output = new String[nOColumns];
                for (int i = 0; i < nOColumns; i++) {
                    String column = metaTable.keySet().toArray(new String[0])[i];
                    output[i] = record.get(column);
                }
                data.add(output);
            }

            writer.writeAll(data);
            writer.close();

            System.out.println("\n------------------------------------");
            System.out.println("Appended to csv: " + "data" + File.separator + addPath + ".csv");
            System.out.println("with " + data.size() + " lines of data and " + nOColumns + " columns");
            System.out.println("------------------------------------");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isTextNode(Node n) {
        return n.getNodeName().equals("#text");
    }
}
