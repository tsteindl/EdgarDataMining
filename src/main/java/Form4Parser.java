import interfaces.FormParser;
import interfaces.XMLConverter;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import util.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class Form4Parser extends FormParser {
    private static String XML_TAG = "XML";
    private static String WELL_FORMED_XML_TAG = "SEC-DOCUMENT";
    private static String XML_DOC_STARTING_TAG = "<?xml version";

    private Map<String, String> fields;
    private List<Map<String, String>> reportingOwners;
    private List<Map<String, String>> nonDerivativeTransactions;
    private List<Map<String, String>> nonDerivativeHoldings;
    private List<Map<String, String>> derivativeTransactions;
    private List<Map<String, String>> derivativeHoldings;
    Scanner scanner = null;
    private Node curr;
    private Node nxt;
    private String nxtTag;

    public Form4Parser(XMLConverter csvTableBuilder) {
        super("4", csvTableBuilder);
        this.fields = new HashMap<>();
        //used LinkedLists here because mainly add operations are used and not random access
        this.reportingOwners = new LinkedList<>();
        this.nonDerivativeTransactions = new LinkedList<>();
        this.nonDerivativeHoldings = new LinkedList<>();
        this.derivativeTransactions = new LinkedList<>();
        this.derivativeHoldings = new LinkedList<>();
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

            parseXMLNodes(xmlRoot);
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
     * Parses document as node-by-node parsing according to the provided schema
     * @param {Node} ownershipDocument root node of document
     */
    private void parseXMLNodes(Node ownershipDocument) {
        //setup scanner
        this.scanner = new Scanner(ownershipDocument);
        //setup parser
        this.curr = ownershipDocument;
        this.nxt = this.scanner.next();
        scan();

        ownershipDocument();
        System.out.println("debug");
    }

    /**
     * Class that scans XML Document and supplies parser with nodes.
     * DFS is used
     */
    class Scanner {
        Queue<Node> nodes;
        public Scanner(Node ownershipDocument) {
            nodes = new LinkedList<>();
            getNodesRec(ownershipDocument);
        }
        public void getNodesRec(Node node) {
            if (node == null)
                return;
            if (node.getNodeType() == Node.ELEMENT_NODE)
                nodes.add(node);
            if (node.hasChildNodes()) {
                NodeList children = node.getChildNodes();
                for (int i = 0; i < children.getLength(); i++)
                    getNodesRec(children.item(i));
            }
        }

        /*
        Node next = null;
        if (n.hasChildNodes()) {
            NodeList children = n.getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
                    return children.item(i);
        }
        next = n.getNextSibling(); //returns null if node doesnt have sibling
        while (next.getNodeType() != Node.ELEMENT_NODE)
            next = next.getNextSibling();
        return next;
         */
        public Node next() {
            return nodes.poll();
        }
    }

    private void ownershipDocument() {
        if (nxtTag.equals("schemaVersion")) parseNode(this.fields, "schemaVersion");
        parseNode(this.fields, "documentType");
        parseNode(this.fields, "periodOfReport");
        if (nxtTag.equals("notSubjectToSection16")) parseNode(this.fields, "notSubjectToSection16");
        issuer();
        reportingOwner();
        if (nxtTag.equals("nonDerivativeTable")) nonDerivativeTable();
        if (nxtTag.equals("derivativeTable")) derivativeTable();
        if (nxtTag.equals("footnotes")) scan(); //footnotes not parsed
        if (nxtTag.equals("remarks")) parseNode(this.fields, "remarks");
        //signature not parsed
        scan();
    }


    private Node next(Node n) {
        Node next = null;
        if (n.hasChildNodes()) {
            NodeList children = n.getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
                    return children.item(i);
        }
        next = n.getNextSibling(); //returns null if node doesnt have sibling
        while (next.getNodeType() != Node.ELEMENT_NODE)
            next = next.getNextSibling();
        return next;
    }

    private void scan() {
        this.curr = this.nxt;
        this.nxt = this.scanner.next();
//        while (getText(this.nxt).equals(""))
//            this.nxt = next(this.nxt);
        try {
            this.nxtTag = ((Element) this.nxt).getTagName();
        } catch (ClassCastException e) {
            this.nxtTag = "";
        }
    }
    private void parseNode(Map<String, String> map, String key) {
        map.put(key, getText(this.nxt));
        scan();
    }

    private void issuer() {
        scan(); //go inside issuer tag
        parseNode(this.fields, "issuerCik");
        if (nxtTag.equals("issuerName")) parseNode(this.fields, "issuerName");
        parseNode(this.fields, "issuerTradingSymbol");
    }
    private void reportingOwner() {
        Map<String, String> result = new HashMap<>();
        scan(); //go inside <reportingOwner>
        scan(); //go inside <reportingOwnerId>
        parseNode(result, "rptOwnerCik");
        parseNode(result, "rptOwnerCcc");
        if (nxtTag.equals("reportingOwnerAddress")) {
            scan(); //go inside <reportingOwnerAddress>
            parseNode(result, "rptOwnerStreet1");
            parseNode(result, "rptOwnerStreet2");
            parseNode(result, "rptOwnerCity");
            parseNode(result, "rptOwnerState");
            parseNode(result, "rptOwnerZipCode");
            if (nxtTag.equals("rptOwnerStateDescription")) scan(); //skip
            if (nxtTag.equals("rptOwnerGoodAddress")) scan(); //skip
        }
        scan(); //go inside <reportingOwnerRelationship>
        if (nxtTag.equals("isDirector")) parseNode(result, "isDirector");
        if (nxtTag.equals("isOfficer")) parseNode(result, "isOfficer");
        if (nxtTag.equals("isTenPercentOwner")) parseNode(result, "isTenPercentOwner");
        if (nxtTag.equals("isOther")) parseNode(result, "isOther");
        if (nxtTag.equals("officerTitle")) parseNode(result, "officerTitle");
        if (nxtTag.equals("otherText")) parseNode(result, "otherText");
        this.reportingOwners.add(result);
    }
    private void nonDerivativeTable() {
        scan(); //go inside <nonDerivativeTable>
        while (nxtTag.equals("nonDerivativeTransaction") || nxtTag.equals("nonDerivativeHolding")) {
            if (nxtTag.equals("nonDerivativeTransaction")) parseNonDerivativeTransaction();
            else parseNonDerivativeHolding();
        }
    }

    private void parseNonDerivativeTransaction() {
        Map<String, String> result = new HashMap<>();
        scan(); //go inside <nonDerivativeTransaction>
        parseNode(result, "securityTitle");
        parseNode(result, "transactionDate");
        if (nxtTag.equals("deemedExecutionDate")) parseNode(result, "deemedExecutionDate");
        if (nxtTag.equals("transactionCoding")) parseNode(result, "transactionCoding");
        if (nxtTag.equals("transactionTimeliness")) parseNode(result, "transactionTimeliness");
        parseNode(result, "transactionAmounts");
        parseNode(result, "postTransactionAmounts");
        parseNode(result, "ownershipNature");
        this.nonDerivativeTransactions.add(result);
    }

    private void parseNonDerivativeHolding() {
        Map<String, String> result = new HashMap<>();
        parseNode(result, "securityTitle");
        parseNode(result, "postTransactionAmounts");
        parseNode(result, "ownershipNature");
    }

    private void derivativeTable() {
        scan(); //go inside <derivativeTable>
        while (nxtTag.equals("derivativeTransaction") || nxtTag.equals("derivativeHolding")) {
            if (nxtTag.equals("derivativeTransaction")) parseDerivativeTransaction();
            else parseDerivativeHolding();
        }
    }

    private void parseDerivativeTransaction() {
        Map<String, String> result = new HashMap<>();
        parseNode(result, "securityTitle");
        parseNode(result, "conversionOrExercisePrice");
        parseNode(result, "transactionDate");
        if (nxtTag.equals("deemedExecutionDate")) parseNode(result, "deemedExecutionDate");
        if (nxtTag.equals("transactionCoding")) parseNode(result, "transactionCoding");
        if (nxtTag.equals("transactionTimeliness")) parseNode(result, "transactionTimeliness");
        parseNode(result, "transactionAmounts");
        parseNode(result, "exerciseDate");
        parseNode(result, "expirationDate");
        parseNode(result, "underlyingSecurity");
        parseNode(result, "postTransactionAmounts");
        parseNode(result, "ownershipNature");
        this.derivativeTransactions.add(result);
    }

    private void parseDerivativeHolding() {
        Map<String, String> result = new HashMap<>();
        parseNode(result, "securityTitle");
        parseNode(result, "conversionOrExercisePrice");
        parseNode(result, "exerciseDate");
        parseNode(result, "expirationDate");
        parseNode(result, "underlyingSecurity");
        parseNode(result, "postTransactionAmounts");
        parseNode(result, "ownershipNature");
        this.derivativeHoldings.add(result);
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
