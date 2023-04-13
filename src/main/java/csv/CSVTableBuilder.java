package csv;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: maybe refactor into composite builder with list of tablebuilders
//TODO: add support for multiple tables => derivativeTable
public class CSVTableBuilder extends CSVBuilder {

    private final List<Table> tables;


    /**
     * Init CSV Builder with table. Repeating tags are the ones that are the same for each table entry: eg the reporter of a form
     * @param outputPath the output path of the generated output //TODO: change this for non CSV
     * @param sep separator
     * @param tables list of nodes for tables
     * @param documentRoot
     * @param excludeTags nodes that will not be parsed
     * @param initFormPath path to form that has desired structure
     * Use Lists instead of arrays so concatenation is easier (Java doesn't offer native array concatenation
     */
    public CSVTableBuilder(String outputPath,
                           String sep,
                           Map<String, String> nonNestedTags,
                           List<Map<String, String>> tables
    ) throws ParserConfigurationException, IOException, SAXException {
        super(
                outputPath,
                sep,
                getAllTags(nonNestedTags, tables)
        );
        this.tables = tables.stream().map(m -> new Table(new ArrayList<>(m.values()))).collect(Collectors.toList());
        resetTableBuilder();
    }

    private static List<String> getAllTags(Map<String, String> nonNestedTags, List<Map<String, String>> tables) {
        return null;
    }


    /*private ArrayList<Table> initTables(List<String[]> tableNamesList, List<String[][]> tableTagsList, List<String[]> tableNodeTags) {
        ArrayList<Table> result = new ArrayList<>();
        for (int i = 0; i < tableNamesList.size(); i++) {
            Map<List<String>, String> tags = new HashMap<>();
            for (int j = 0; j < tableNamesList.get(i).length; j++)
                tags.put(Stream.concat(documentRoot.stream(), Stream.concat(Arrays.stream(tableNodeTags.get(i)), Arrays.stream(tableTagsList.get(i)[j]))).toList(), tableNamesList.get(i)[j]);
            result.add(new Table(tags));
        }
        return result;
    }*/

    //TODO: refactor this to be functional (return type)
    // refactor this to map collect https://stackoverflow.com/questions/14513475/how-do-i-iterate-over-multiple-lists-in-parallel-in-java
    // find way to use one generic function for both get All functions
/*
    private void getAllTableTags(List<String[]> tableNamesList, List<String[][]> tableTagsList) {
        for (int i = 0; i < tableNamesList.size(); i++)
            for(int j = 0; j < tableNamesList.get(i).length; j++)
                this.tableTags.put(Stream.concat(documentRoot.stream(), Stream.concat(Arrays.stream(tableNodeTags.get(i)), Arrays.stream(tableTagsList.get(i)[j]))).toList(), tableNamesList.get(i)[j]); //TODO: Stream concat twice is ugly => refactor
//        this.tableTags = Streams.zip(tableNamesList.stream(), tableTagsList.stream(), (tableNames, tableTags) -> {
//            return tableTags
//        }).collect(Collectors.toMap());
//        Streams.forEachPair(tableNamesList.stream(), tableTagsList.stream(),
//                (tableNames, tableTags) -> Streams.forEachPair(Arrays.stream(tableNames), Arrays.stream(tableTags),
//                                            (name, tags) -> this.tableTags.put(Stream.of(documentRoot, tableNodeTags, Arrays.stream(tags)), name))
//        );
    }
*/

    private void getAllRepeatingTags(List<String> repTagNames, List<String[]> repTags, List<String> documentRoot) {
//        Streams.forEachPair(repTagNames.stream(), repTags.stream(), (name, tags) -> this.tags.put(Stream.concat(documentRoot.stream(), Arrays.stream(tags)).toList(), name));
    }

    //TODO: refactor these 2 as 1 fct?
    private static List<String> getNestedTableNames(List<String> repTagNames, List<String[]> tableNames) {
        return Stream.concat(repTagNames.stream(), tableNames.stream().map(Arrays::asList).flatMap(List::stream))
                .collect(Collectors.toList());
    }

/*
    private static List<String> getAllTags(String formPath, List<String> tableTags, List<String> nullableTags) throws ParserConfigurationException, IOException, SAXException {
        String xml = Files.readString(Path.of(formPath));
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource data = new InputSource(new StringReader(xml));
        Document doc = db.parse(data);

        doc.getDocumentElement().normalize();

        Map<String, List<String>> nestedTableTags = new HashMap<>();
        Set<String> allTags = new HashSet<>();
        Element docEl = doc.getDocumentElement();
        getAllTagsRec(docEl, docEl.getTagName(),nestedTableTags, allTags,false, null, tableTags, nullableTags);
        return new ArrayList<>(allTags);
    }
*/

    /**
     * Fills map of tables with nested table tags and set of all tags
     */
    private static void getAllTagsRec(Node node, String tag, Map<String, List<String>> tables, Set<String> allTags, boolean inTable, String currTable, List<String> tableNodeTags, List<String> nullableTags) {
        if (node == null)
            return;
        if (isTextNode(node)) {
            if (nullableTags.contains(tag) || isEmpty(tag))
                return;
            if (inTable)
                tables.get(currTable).add(tag);
            allTags.add(tag);
        }
        else if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element nodeElem = (Element) node;
            NodeList childNodes = node.getChildNodes();
            if (tableNodeTags.contains(nodeElem.getTagName()))
                for (int i = 0; i < childNodes.getLength(); i++) {
                    String tagName = nodeElem.getTagName();
                    tables.put(tagName, new ArrayList<>());
//                    getTableTagsRec(childNodes.item(i), tables, allTags, true, nodeElem.getTagName(), tableNodeTags, nullableTags);
                    getAllTagsRec(childNodes.item(i), tagName, tables, allTags, true, nodeElem.getTagName(), tableNodeTags, nullableTags);
                }
            else
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Element nextEl = null;
                    try {
                        nextEl = (Element) childNodes.item(i);
                        if (nextEl.getTagName().equals("value"))
                            getAllTagsRec(childNodes.item(i), tag, tables, allTags, inTable, currTable, tableNodeTags, nullableTags);
                        else
                            getAllTagsRec(childNodes.item(i), nextEl.getTagName(), tables, allTags, inTable, currTable, tableNodeTags, nullableTags);
                    } catch (ClassCastException e) {}

                }
        }
    }

    private static void getTableTagsRec(Node node, Map<String, List<String>> tables, Set<String> allTags, boolean inTable, String currTable, List<String> tableNodeTags, List<String> nullableTags) {
        if (node == null)
            return;
        if (isTextNode(node)) {
            String tag = "";

        }
    }

    private static boolean isEmpty(String s) {
        return s.trim().isEmpty();
    }

    //TODO: copied method from FORM4Parser
    private static boolean isTextNode(Node n) {
//        return n.getNodeName().equals("#text");
        return n.getNodeName().equals("#text");
    }

    //TODO: copied method from FORM4Parser
    private static String getText(Node node) {
        return node.getTextContent().trim().replaceAll("[\\n\\t]", "");
    }

    public int getRepTagsColSize() {
        return this.tags.size();
    }

    public void resetTableBuilder() {
    }

//    public void addEntry(String tag, String value) {
//        if (isNestedTableTag(tag)) {//TODO: List access is O(n) => bad
//            Table table = getTable(tag).get();
//            table.
//        }
//    }

    public boolean currLineFull() { //TODO: make sure this works if not all values are provided
        return false;
    }

    public void addCurrLine() {
        this.tables.forEach(t -> {
        });
    }

    private void addLine(Map<String, String> tableVals) {
//        Map<String, String> mergedMap = new HashMap<>(repVals);
//        mergedMap.putAll(tableVals);
//        addLine(mergedMap);
    }

    /**
     * Function that determines if tag is part of nested table or of repeating tags
     * @param tag
     * @return boolean
     */
    private boolean isNestedTableTag(String tag) {
        return containsTag(this.tags, tag);
    }

    /**
     * Gets table that a tag belongs to (if there is one)
     * @param tag
     * @return Table
     */
    private Optional<Table> getTable(String tag) {
        return this.tables.stream().filter((Table t) -> containsTag(t.tableTags, tag)).findFirst();
    }

}