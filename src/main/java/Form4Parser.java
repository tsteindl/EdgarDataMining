import csv.CSVTableBuilder;
import interfaces.FormParser;
import interfaces.FormConverter;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import util.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Form4Parser extends FormParser {
    //TODO: types (!!!!!)
    private static String XML_TAG = "XML";
    private static String WELL_FORMED_XML_TAG = "SEC-DOCUMENT";
    private static String XML_DOC_STARTING_TAG = "<?xml version";
    private String input;

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

    public Form4Parser(String input) {
        super("4");
        this.input = input;
        this.fields = new HashMap<>();
        //used LinkedLists here because mainly add operations are used and not random access
        this.reportingOwners = new LinkedList<>();
        this.nonDerivativeTransactions = new LinkedList<>();
        this.nonDerivativeHoldings = new LinkedList<>();
        this.derivativeTransactions = new LinkedList<>();
        this.derivativeHoldings = new LinkedList<>();
    }

//    /**
//     * inits outputter passed in constructor
//     * @throws InitException
//     */
//    @Override
//    public void init() throws InitException {
//        outputter.init();
//    }
    @Override
    public void parseForm() throws ParseFormException {
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
            this.input = input;
            xml = getXMLBody(xml);
            Element xmlRoot = getXMLTreeFromString(xml);

            parseXMLNodes(xmlRoot);
//            outputter.outputForm(); //TODO: maybe extract this into parent function
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    @Override
    public FormConverter output(String outputPath) throws OutputException {
        try {
            return new CSVTableBuilder(
                    outputPath,
                    ";",
                    fields,
                    Stream.of(reportingOwners, nonDerivativeTransactions, nonDerivativeHoldings, derivativeTransactions, derivativeHoldings)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList())
            );

/*
            return new CSVTableBuilder(
                    outputPath,
                    ";",
                    List.of(TABLE_NODE_TAGS),
                    CSV_DOCUMENT_ROOT,
                    List.of(EXCLUDE_TAGS),
                    FORM_PATH,
                    List.of(NULLABLE_TAGS)
            );
*/
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new OutputException(e.getMessage());
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
    private void parseXMLNodes(Node ownershipDocument) throws ParseFormException {
        //setup scanner
        this.scanner = new Scanner(ownershipDocument);
        //setup parser
        this.curr = ownershipDocument;
        this.nxt = this.scanner.next();
        scan();

        ownershipDocument();
        if (this.nxt != null)
            throw new ParseFormException(this.nxt);
        else
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
        if (nxtTag.equals("footnotes")) footnotes();
        if (nxtTag.equals("remarks")) remarks();
        ownerSignature();
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
            Element nxtEl = ((Element) this.nxt);
            if (nxtEl != null)
                this.nxtTag = nxtEl.getTagName();
            else this.nxtTag = "";
        } catch (ClassCastException e) {
            this.nxtTag = "";
        }
    }
    private void parseNode(Map<String, String> map, String key) {
        String tag = ((Element) this.nxt).getTagName();
        if (!key.equals(tag) && !tag.equals("value"))
            System.out.println("Tag should be: " + key + " but is: " + tag);
        map.put(key, getText(this.nxt));
        scan();
    }
    private void parseValueNode(Map<String, String> map, String key) {
        scan();
        parseNode(map, key);
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
        reportingOwnerId(result);
        if (nxtTag.equals("reportingOwnerAddress")) reportingOwnerAddress(result);
        reportingOwnerRelationship(result);
        this.reportingOwners.add(result);
    }

    private void reportingOwnerRelationship(Map<String, String> result) {
        reportingRelationship(result, "reportingOwnerRelationship");
    }

    private void reportingRelationship(Map<String, String> result, String tag) {
        scan(); //go inside <reportingOwnerRelationship>
        if (nxtTag.equals("isDirector")) parseNode(result, "isDirector");
        if (nxtTag.equals("isOfficer")) parseNode(result, "isOfficer");
        if (nxtTag.equals("isTenPercentOwner")) parseNode(result, "isTenPercentOwner");
        if (nxtTag.equals("isOther")) parseNode(result, "isOther");
        if (nxtTag.equals("officerTitle")) parseNode(result, "officerTitle");
        if (nxtTag.equals("otherText")) parseNode(result, "otherText");
    }

    private void reportingOwnerAddress(Map<String, String> result) {
        scan();
        reportingAddress(result);
    }

    private void reportingAddress(Map<String, String> result) {
        if (nxtTag.equals("rptOwnerStreet1"))
            parseNode(result, "rptOwnerStreet1");
        if (nxtTag.equals("rptOwnerStreet2"))
            parseNode(result, "rptOwnerStreet2");
        if (nxtTag.equals("rptOwnerCity"))
            parseNode(result, "rptOwnerCity");
        if (nxtTag.equals("rptOwnerState"))
            parseNode(result, "rptOwnerState");
        if (nxtTag.equals("rptOwnerZipCode"))
            parseNode(result, "rptOwnerZipCode");
        if (nxtTag.equals("rptOwnerStateDescription")) scan(); //skip
        if (nxtTag.equals("rptOwnerGoodAddress")) scan(); //skip
    }

    private void reportingOwnerId(Map<String, String> result) {
        scan(); //go inside <reportingOwner>
        reportingId(result);
    }

    private void reportingId(Map<String, String> result) {
        parseNode(result, "rptOwnerCik");
        if (nxtTag.equals("rptOwnerCik"))
            parseNode(result, "rptOwnerCcc");
        if (nxtTag.equals("rptOwnerName"))
            parseNode(result, "rptOwnerName");
    }

    private void nonDerivativeTable() {
        scan(); //go inside <nonDerivativeTable>
        while (nxtTag.equals("nonDerivativeTransaction") || nxtTag.equals("nonDerivativeHolding")) {
            if (nxtTag.equals("nonDerivativeTransaction")) nonDerivativeTransaction();
            else parseNonDerivativeHolding();
        }
    }

    private void nonDerivativeTransaction() {
        Map<String, String> result = new HashMap<>();
        scan(); //go inside <nonDerivativeTransaction>
        securityTitle(result, "securityTitle");
        transactionDate(result);
        if (nxtTag.equals("deemedExecutionDate")) deemedExecutionDate(result);
        if (nxtTag.equals("transactionCoding")) transactionCoding(result);
        if (nxtTag.equals("transactionTimeliness")) transactionTimeliness(result);
        nonDerivTransactAmounts(result);
        postTransactionAmounts(result);
        ownershipNature(result);
        this.nonDerivativeTransactions.add(result);
    }

    private void ownershipNature(Map<String, String> result) {
        scan(); //go inside ownershipNature
        directOrIndirectOwnership(result);
        if (nxtTag.equals("natureOfOwnership")) natureOfOwnership(result);
    }

    private void natureOfOwnership(Map<String, String> result) {
        indirectNature(result, "natureOfOwnership");
    }

    private void indirectNature(Map<String, String> result, String tag) {
        parseValueNode(result, tag);
        footnodeId();
    }

    private void directOrIndirectOwnership(Map<String, String> result) {
        ownershipType(result, "directOrIndirectOwnership");
    }

    private void ownershipType(Map<String, String> result, String key) {
        parseValueNode(result, key);
    }

    private void postTransactionAmounts(Map<String, String> result) {
        scan(); //go inside <postTransactionAmounts>
        if (nxtTag.equals("sharesOwnedFollowingTransaction"))
            sharesOwnedFollowingTransaction(result);
        else
            valueOwnedFollowingTransaction(result);
    }

    private void valueOwnedFollowingTransaction(Map<String, String> result) {
        numberWithFootnote(result, "valueOwnedFollowingTransaction");
    }

    private void sharesOwnedFollowingTransaction(Map<String, String> result) {
        numberWithFootnote(result, "sharesOwnedFollowingTransaction");
    }

    private void numberWithFootnote(Map<String, String> result, String tag) {
        parseValueNode(result, tag);
        while (nxtTag.equals("footnoteId")) footnodeId();
    }

    private void derivTransactAmounts(Map<String, String> result) {
        scan(); //go inside <transactionAmounts>
        if (nxtTag.equals("transactionShares"))
            transactionShares(result);
        else
            transactionTotalValue(result);
        transactionPricePerShare(result);
        derAcqDispCode(result, "transactionAcquiredDisposedCode");
    }

    private void transactionTotalValue(Map<String, String> result) {
        numberWithFootnote(result, "transactionTotalValue");
    }

    private void nonDerivTransactAmounts(Map<String, String> result) {
        scan(); //go inside <transactionAmounts>
        transactionShares(result);
        transactionPricePerShare(result);
        nonDerAcqDispCode(result, "transactionAcquiredDisposedCode");
    }

    private void nonDerAcqDispCode(Map<String, String> result, String tag) {
        parseValueNode(result, tag);
        if (nxtTag.equals("footnoteId"))
            footnodeId();
    }

    private void derAcqDispCode(Map<String, String> result, String tag) {
        parseValueNode(result, tag);
    }

    private void transactionPricePerShare(Map<String, String> result) {
        scan();
        optNumberWithFootnote(result, "transactionPricePerShare");
    }

    private void optNumberWithFootnote(Map<String, String> result, String tag) {
        if (nxtTag.equals("value")) parseNode(result, tag);
        else footnodeId();
        while (nxtTag.equals("footnoteId"))
            footnodeId();
    }

    private void transactionShares(Map<String, String> result) {
        numberWithFootnote(result, "transactionShares");
    }

    private void transactionTimeliness(Map<String, String> result) {
        transTimelyPicklist(result, "transactionTimeliness");
        if (nxtTag.equals("footnoteId")) footnodeId();
    }

    private void transTimelyPicklist(Map<String, String> result, String tag) {
        parseValueNode(result, tag);
    }

    private void transactionCoding(Map<String, String> result) {
        scan(); //go inside <transactionCoding>
        transactionFormType(result);
        transactionCode(result);
        equitySwapInvolved(result);
        if (nxtTag.equals("footnoteId")) footnodeId();
    }

    private void equitySwapInvolved(Map<String, String> result) {
        parseNode(result, "equitySwapInvolved"); //is boolean value
    }

    private void transactionCode(Map<String, String> result) {
        parseNode(result, "transactionCode");
    }

    private void transactionFormType(Map<String, String> result) {
        parseNode(result, "transactionFormType");
    }

    private void deemedExecutionDate(Map<String, String> result) {
        if (nxtTag.equals("value")) parseValueNode(result, "transactionDate");
        if (nxtTag.equals("footnoteId")) footnodeId();
    }

    private void footnodeId() {
        scan(); //skip
    }


    private void transactionDate(Map<String, String> result) {
        parseValueNode(result, "transactionDate");
        if (nxtTag.equals("footnoteId")) footnodeId();
    }

    private void securityTitle(Map<String, String> result, String tag) {
        parseValueNode(result, tag);
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
        scan(); //go inside
        securityTitle(result, "securityTitle");
        conversionOrExercisePrice(result);
        transactionDate(result);
        if (nxtTag.equals("deemedExecutionDate")) deemedExecutionDate(result);
        if (nxtTag.equals("transactionCoding")) transactionCoding(result);
        if (nxtTag.equals("transactionTimeliness")) transactionTimeliness(result);
        derivTransactAmounts(result);
        exerciseDate(result);
        expirationDate(result);
        underlyingSecurity(result);
        postTransactionAmounts(result);
        ownershipNature(result);
        this.derivativeTransactions.add(result);
    }

    private void underlyingSecurity(Map<String, String> result) {
        underlyingSecurityTitle(result);
        if (nxtTag.equals("underlyingSecurityShares")) underlyingSecurityShares(result);
        if (nxtTag.equals("underlyingSecurityValue")) underlyingSecurityValue(result);
    }

    private void underlyingSecurityValue(Map<String, String> result) {
        optNumberWithFootnote(result, "underlyingSecurityValue");
    }

    private void underlyingSecurityShares(Map<String, String> result) {
        optNumberWithFootnote(result, "underlyingSecurityShares");
    }

    private void underlyingSecurityTitle(Map<String, String> result) {
        securityTitle(result, "underlyingSecurityTitle");
    }

    private void expirationDate(Map<String, String> result) {
        optDateWithFootNote(result, "expirationDate");
    }

    private void exerciseDate(Map<String, String> result) {
        optDateWithFootNote(result, "exerciseDate");
    }

    private void optDateWithFootNote(Map<String, String> result, String tag) {
        if (nxtTag.equals("value")) parseValueNode(result, tag);
        else footnodeId();
        while (nxtTag.equals("footnoteId")) footnodeId();
    }

    private void conversionOrExercisePrice(Map<String, String> result) {
        optNumberWithFootnote(result, "conversionOrExercisePrice");
    }

    private void parseDerivativeHolding() {
        Map<String, String> result = new HashMap<>();
        scan(); //go inside
        securityTitle(result, "securityTitle");
        conversionOrExercisePrice(result);
        exerciseDate(result);
        expirationDate(result);
        underlyingSecurity(result);
        postTransactionAmounts(result);
        ownershipNature(result);
        this.derivativeHoldings.add(result);
    }

    private void ownerSignature() {
        scan(); //go inside
        signature();
    }

    private void signature() {
        scan(); //skip signatureName
        scan(); //skip signatureDate
    }

    private void remarks() {
        parseNode(this.fields, "remarks");
    }

    private void footnotes() {
        scan();
        while (nxtTag.equals("footnote")) footnote();
    }

    private void footnote() {
        scan(); //skip footnote
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
