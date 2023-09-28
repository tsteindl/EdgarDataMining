package Form4Parser;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Class that scans XML Document and supplies parser with nodes.
 * DFS is used
 */
class FormScanner {
    //TODO: aggregate into MyNode that has these as fields
    Queue<Object> vals;
    Queue<Node> nodes;

    public FormScanner(Node ownershipDocument) {
        nodes = new LinkedList<>();
        vals = new LinkedList<>();
        getNodesRec(ownershipDocument);
    }

    private void getNodesRec(Node node) {
        if (node == null)
            return;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            nodes.add(node);
            vals.add(getValue(node));
        }
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
                getNodesRec(children.item(i));
        }
    }

    /**
     * Method parses node and maps xml types to java types
     * @param node
     * @return
     */
    private Object getValue(Node node) {
        String name = node.getNodeName();
        String text = getText(node);
        if (name.equals("value")) {
            return switch (node.getParentNode().getNodeName()) {
                case "transactionDate", "deemedExecutionDate", "exerciseDate", "expirationDate", "signatureDate" ->
                        parseDate(text);
                case "conversionOrExercisePrice", "sharesOwnedFollowingTransaction", "valueOwnedFollowingTransaction", "transactionShares", "transactionTotalValue", "transactionPricePerShare", "underlyingSecurityShares", "underlyingSecurityValue" ->
                        Double.parseDouble(text);
                default -> text;
            };
        }
        return switch(name) {
            case "issuerCik", "rptOwnerCik", "transactionFormType" -> Integer.parseInt(text);
            case "documentType", "schemaVersion", "issuerName", "issuerTradingSymbol", "rptOwnerCcc", "rptOwnerName", "rptOwnerStreet1", "rptOwnerStreet2", "rptOwnerCity", "rptOwnerState", "rptOwnerZipCode", "rptOwnerStateDescription", "officerTitle", "otherText", "securityTitle", "transactionCode", "transactionTimeliness", "transactionAcquiredDisposedCode", "directOrIndirectOwnership", "ownershipNature", "underlyingSecurity", "remarks", "signatureName" -> text;
            case "periodOfReport" -> parseDate(text);
            case "notSubjectToSection16", "rptOwnerGoodAddress", "isDirector", "isOfficer", "isTenPercentOwner", "isOther", "equitySwapInvolved" ->  Boolean.valueOf(Boolean.parseBoolean(text) || "1".equals(text));
            case "remove" -> Double.parseDouble(text);
            case "footnoteId", "footnote" -> {
                Attr idAttr = ((Element) node).getAttributeNode("id");
                yield idAttr.getValue();
            }
            default -> null;
        };
    }

    private LocalDate parseDate(String text) {
        LocalDate result = null;
        try {
            result = LocalDate.parse(text);
        } catch(DateTimeParseException e) {
            LocalDateTime dt = null;
            try {
                dt = LocalDateTime.parse(text);
            } catch (DateTimeParseException ex) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");
                dt = LocalDateTime.parse(text,
                        formatter);
            }
            result = dt.toLocalDate();
        }
        return result;
    }

    private String getText(Node node) {
        return node.getTextContent().trim().replaceAll("[\\n\\t]", "");
    }

    public Node next() {
        return nodes.poll();
    }
    public Object nextVal() {
        return vals.poll();
    }
}
