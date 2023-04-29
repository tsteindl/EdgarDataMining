package Form4Parser;

import Form4Parser.Types.TableType;
import csv.CSVTableBuilder;
import db.DBOutputter;
import interfaces.FormParser;
import interfaces.FormConverter;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import util.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;

public class Form4Parser extends FormParser {
    //TODO: types (!!!!!)
    private static String XML_TAG = "XML";
    private static String WELL_FORMED_XML_TAG = "SEC-DOCUMENT";
    private static String XML_DOC_STARTING_TAG = "<?xml version";
    private String input;

    FormScanner scanner = null;
    private Node curr;
    private Node nxt;
    private Object nxtVal;
    private String nxtTag;

    //Parser fields
    private final Map<String, String> fields;
    private String schemaVersion;//TODO correct types
    private String documentType;
    private LocalDate periodOfReport;
    private boolean notSubjectToSection16;
    private String issuerCik;
    private String issuerName;
    private String issuerTradingSymbol;
    private String remarks;

    private final List<ReportingOwner> reportingOwners;
    private final List<NonDerivativeTransaction> nonDerivativeTransactions;
    private final List<NonDerivativeHolding> nonDerivativeHoldings;
    private final List<DerivativeTransaction> derivativeTransactions;
    private final List<DerivativeHolding> derivativeHoldings;
    public Form4Parser(String name, String input) {
        super(name, "4");
        this.input = input;
        this.fields = new HashMap<>();
        //used LinkedLists here because mainly add operations are used and not random access
        this.reportingOwners = new LinkedList<>();
        this.nonDerivativeTransactions = new LinkedList<>();
        this.nonDerivativeHoldings = new LinkedList<>();
        this.derivativeTransactions = new LinkedList<>();
        this.derivativeHoldings = new LinkedList<>();
    }

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
        } catch (ParserConfigurationException | IOException | SAXException | ParseFormException e) {
            e.printStackTrace();
        }
    }

    @Override
    public FormConverter configureOutputter(String outputPath, FormConverter.Outputter type) throws OutputException {
        return switch (type) {
            case DB -> new DBOutputter();
            default -> configureCSV(outputPath);
        };
    }

    public CSVTableBuilder configureCSV(String outputPath) throws OutputException {
        try {
            Map<String, List<? extends TableType>> tables = new HashMap<>();
            tables.put("reportingOwners", reportingOwners);
            tables.put("nonDerivativeTransactions", nonDerivativeTransactions);
            tables.put("nonDerivativeHoldings", nonDerivativeHoldings);
            tables.put("derivativeTransactions", derivativeTransactions);
            tables.put("derivativeHoldings", derivativeHoldings);
            return new CSVTableBuilder(
                    outputPath,
                    ";",
                    fields,
                    tables
            );
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
        this.scanner = new FormScanner(ownershipDocument);
        //setup parser
        this.curr = ownershipDocument;
        this.nxt = this.scanner.next();
        this.nxtVal = this.scanner.nextVal();
        ownershipDocument();
        if (this.nxt != null)
            throw new ParseFormException(this.name, this.nxt);
        else
            System.out.println("debug");
    }

    private void ownershipDocument() {
        scan();
        if (nxtTag.equals("schemaVersion")) parseNode(this, "schemaVersion");
        parseNode(this, "documentType");
        parseNode(this, "periodOfReport");
        if (nxtTag.equals("notSubjectToSection16")) parseNode(this, "notSubjectToSection16");
        issuer();
        reportingOwner();
        if (nxtTag.equals("nonDerivativeTable")) nonDerivativeTable();
        if (nxtTag.equals("derivativeTable")) derivativeTable();
        if (nxtTag.equals("footnotes")) footnotes();
        if (nxtTag.equals("remarks")) remarks();
        ownerSignature();
    }


    private void scan() {
        this.curr = this.nxt;
        this.nxt = this.scanner.next();
        this.nxtVal = this.scanner.nextVal();
        try {
            Element nxtEl = ((Element) this.nxt);
            if (nxtEl != null)
                this.nxtTag = nxtEl.getTagName();
            else this.nxtTag = "";
        } catch (ClassCastException e) {
            this.nxtTag = "";
        }
    }
//    private void parseNode(Map<String, String> map, String key) {
//        String tag = ((Element) this.nxt).getTagName();
//        if (!key.equals(tag) && !tag.equals("value"))
//            System.out.println("Tag should be: " + key + " but is: " + tag);
//        map.put(key, getText(this.nxt));
//        scan();
//    }
    private void parseNode(Object c, String key) {
        try {
            String tag = ((Element) this.nxt).getTagName();
            if (!key.equals(tag) && !tag.equals("value"))
                System.out.println("Tag should be: " + key + " but is: " + tag);
            Field fld = c.getClass().getDeclaredField(key);
            Class<?> type = fld.getType();
            Object castValue = type.cast(nxtVal);
            fld.setAccessible(true);
            fld.set(c, castValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.out.println(e.getMessage());
        }
        scan();


//        String tag = ((Element) this.nxt).getTagName();
//        if (!key.equals(tag) && !tag.equals("value"))
//            System.out.println("Tag should be: " + key + " but is: " + tag);
//        map.put(key, getText(this.nxt));
//        scan();
    }

//    private void parseValueNode(Map<String, String> map, String key) {
//        scan();
//        parseNode(map, key);
//    }

    private void parseValueNode(Object result, String key) {
        scan();
        parseNode(result, key);
    }

    private void issuer() {
        scan(); //go inside issuer tag
        parseNode(this, "issuerCik");
        if (nxtTag.equals("issuerName")) parseNode(this, "issuerName");
        parseNode(this, "issuerTradingSymbol");
    }
    private void reportingOwner() {
        ReportingOwner result = new ReportingOwner();
//        Map<String, String> result = new HashMap<>();
        scan(); //go inside <reportingOwner>
        reportingOwnerId(result);
        if (nxtTag.equals("reportingOwnerAddress")) reportingOwnerAddress(result);
        reportingOwnerRelationship(result);
        this.reportingOwners.add(result);
    }

    private void reportingOwnerRelationship(ReportingOwner result) {
        reportingRelationship(result, "reportingOwnerRelationship");
    }

    private void reportingRelationship(ReportingOwner result, String tag) {
        scan(); //go inside <reportingOwnerRelationship>
        if (nxtTag.equals("isDirector")) parseNode(result, "isDirector");
        if (nxtTag.equals("isOfficer")) parseNode(result, "isOfficer");
        if (nxtTag.equals("isTenPercentOwner")) parseNode(result, "isTenPercentOwner");
        if (nxtTag.equals("isOther")) parseNode(result, "isOther");
        if (nxtTag.equals("officerTitle")) parseNode(result, "officerTitle");
        if (nxtTag.equals("otherText")) parseNode(result, "otherText");
    }

    private void reportingOwnerAddress(ReportingOwner result) {
        scan();
        reportingAddress(result);
    }

    private void reportingAddress(ReportingOwner result) {
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

    private void reportingOwnerId(ReportingOwner result) {
        scan(); //go inside <reportingOwner>
        reportingId(result);
    }

    private void reportingId(ReportingOwner result) {
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
            else nonDerivativeHolding();
        }
    }

    private void nonDerivativeTransaction() {
//        Map<String, String> result = new HashMap<>();
        NonDerivativeTransaction result = new NonDerivativeTransaction();
        scan(); //go inside <nonDerivativeTransaction>
        securityTitle(result, "securityTitle"); //TODO revise if this is smart
        transactionDate(result);
        if (nxtTag.equals("deemedExecutionDate")) deemedExecutionDate(result);
        if (nxtTag.equals("transactionCoding")) transactionCoding(result);
        if (nxtTag.equals("transactionTimeliness")) transactionTimeliness(result);
        nonDerivTransactAmounts(result);
        postTransactionAmounts(result);
        ownershipNature(result);
        this.nonDerivativeTransactions.add(result);
    }

    private void ownershipNature(Object result) {
        scan(); //go inside ownershipNature
        directOrIndirectOwnership(result);
        if (nxtTag.equals("natureOfOwnership")) natureOfOwnership(result);
    }

    private void natureOfOwnership(Object result) {
        indirectNature(result, "natureOfOwnership");
    }

    private void indirectNature(Object result, String tag) {
        parseValueNode(result, tag);
        footnodeId();
    }

    private void directOrIndirectOwnership(Object result) {
        ownershipType(result, "directOrIndirectOwnership");
    }

    private void ownershipType(Object result, String key) {
        parseValueNode(result, key);
    }

    private void postTransactionAmounts(Object result) {
        scan(); //go inside <postTransactionAmounts>
        if (nxtTag.equals("sharesOwnedFollowingTransaction"))
            sharesOwnedFollowingTransaction(result);
        else
            valueOwnedFollowingTransaction(result);
    }

    private void valueOwnedFollowingTransaction(Object result) {
        numberWithFootnote(result, "valueOwnedFollowingTransaction");
    }

    private void sharesOwnedFollowingTransaction(Object result) {
        numberWithFootnote(result, "sharesOwnedFollowingTransaction");
    }

    private void numberWithFootnote(Object result, String tag) {
        parseValueNode(result, tag);
        while (nxtTag.equals("footnoteId")) footnodeId();
    }

    private void derivTransactAmounts(Object result) {
        scan(); //go inside <transactionAmounts>
        if (nxtTag.equals("transactionShares"))
            transactionShares(result);
        else
            transactionTotalValue(result);
        transactionPricePerShare(result);
        derAcqDispCode(result, "transactionAcquiredDisposedCode");
    }

    private void transactionTotalValue(Object result) {
        numberWithFootnote(result, "transactionTotalValue");
    }

    private void nonDerivTransactAmounts(NonDerivativeTransaction result) {
        scan(); //go inside <transactionAmounts>
        transactionShares(result);
        transactionPricePerShare(result);
        nonDerAcqDispCode(result, "transactionAcquiredDisposedCode");
    }

    private void nonDerAcqDispCode(Object result, String tag) {
        parseValueNode(result, tag);
        if (nxtTag.equals("footnoteId"))
            footnodeId();
    }

    private void derAcqDispCode(Object result, String tag) {
        parseValueNode(result, tag);
    }

    private void transactionPricePerShare(Object result) {
        scan();
        optNumberWithFootnote(result, "transactionPricePerShare");
    }

    private void optNumberWithFootnote(Object result, String tag) {
        if (nxtTag.equals("value")) parseNode(result, tag);
        else footnodeId();
        while (nxtTag.equals("footnoteId"))
            footnodeId();
    }

    private void transactionShares(Object result) {
        numberWithFootnote(result, "transactionShares");
    }

    private void transactionTimeliness(Object result) {
        transTimelyPicklist(result, "transactionTimeliness");
        if (nxtTag.equals("footnoteId")) footnodeId();
    }

    private void transTimelyPicklist(Object result, String tag) {
        parseValueNode(result, tag);
    }

    private void transactionCoding(Object result) {
        scan(); //go inside <transactionCoding>
        transactionFormType(result);
        transactionCode(result);
        equitySwapInvolved(result);
        if (nxtTag.equals("footnoteId")) footnodeId();
    }

    private void equitySwapInvolved(Object result) {
        parseNode(result, "equitySwapInvolved"); //is boolean value
    }

    private void transactionCode(Object result) {
        parseNode(result, "transactionCode");
    }

    private void transactionFormType(Object result) {
        parseNode(result, "transactionFormType");
    }

    private void deemedExecutionDate(Object result) {
        if (nxtTag.equals("value")) parseValueNode(result, "transactionDate");
        if (nxtTag.equals("footnoteId")) footnodeId();
    }

    private void footnodeId() {
        scan(); //skip
    }


    private void transactionDate(Object result) {
        parseValueNode(result, "transactionDate");
        if (nxtTag.equals("footnoteId")) footnodeId();
    }

    private void securityTitle(Object result, String tag) {
        parseValueNode(result, tag);
    }

    private void nonDerivativeHolding() {
//        Map<String, String> result = new HashMap<>();
        NonDerivativeHolding result = new NonDerivativeHolding();
        securityTitle(result, "securityTitle");
        postTransactionAmounts(result);
        ownershipNature(result);
        this.nonDerivativeHoldings.add(result);
    }

    private void derivativeTable() {
        scan(); //go inside <derivativeTable>
        while (nxtTag.equals("derivativeTransaction") || nxtTag.equals("derivativeHolding")) {
            if (nxtTag.equals("derivativeTransaction")) derivativeTransaction();
            else derivativeHolding();
        }
    }

    private void derivativeTransaction() {
//        Map<String, String> result = new HashMap<>();
        DerivativeTransaction result = new DerivativeTransaction();
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

    private void underlyingSecurity(Object result) {
        underlyingSecurityTitle(result);
        if (nxtTag.equals("underlyingSecurityShares")) underlyingSecurityShares(result);
        if (nxtTag.equals("underlyingSecurityValue")) underlyingSecurityValue(result);
    }

    private void underlyingSecurityValue(Object result) {
        optNumberWithFootnote(result, "underlyingSecurityValue");
    }

    private void underlyingSecurityShares(Object result) {
        optNumberWithFootnote(result, "underlyingSecurityShares");
    }

    private void underlyingSecurityTitle(Object result) {
        securityTitle(result, "underlyingSecurityTitle");
    }

    private void expirationDate(Object result) {
        optDateWithFootNote(result, "expirationDate");
    }

    private void exerciseDate(Object result) {
        optDateWithFootNote(result, "exerciseDate");
    }

    private void optDateWithFootNote(Object result, String tag) {
        if (nxtTag.equals("value")) parseValueNode(result, tag);
        else footnodeId();
        while (nxtTag.equals("footnoteId")) footnodeId();
    }

    private void conversionOrExercisePrice(Object result) {
        optNumberWithFootnote(result, "conversionOrExercisePrice");
    }

    private void derivativeHolding() {
//        Map<String, String> result = new HashMap<>();
        DerivativeHolding result = new DerivativeHolding();
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
        parseNode(this, "remarks");
    }

    private void footnotes() {
        scan();
        while (nxtTag.equals("footnote")) footnote();
    }

    private void footnote() {
        scan(); //skip footnote
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
