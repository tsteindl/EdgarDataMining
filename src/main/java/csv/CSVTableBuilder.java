package csv;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import util.OutputException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: maybe refactor into composite builder with list of tablebuilders
//TODO: add support for multiple tables => derivativeTable
public class CSVTableBuilder extends CSVBuilder {

    static class Table<T> {
        private String id;
        public final List<String> tags;
        public final Map<String, T> map;

        Table(String id, Map<String, T> map) {
            this.id = id;
            this.map = map;
            this.tags = new ArrayList<>(map.keySet());
        }

        public List<String> getLine() {
//            return map.values().stream().map(v -> this.id + "_" + v).collect(Collectors.toList());
            return map.values().stream().map(v -> (String) v).collect(Collectors.toList()); //TODO: check if this is good code
        }
        public int getNoCols() {
            return this.tags.size();
        }
    }


    private final Map<String, String> nonNestedTags;
    private final List<Table<String>> tables;


    /**
     * Init CSV Builder with table. Repeating tags are the ones that are the same for each table entry: eg the reporter of a form
     * @param outputPath the output path of the generated output //TODO: change this for non CSV
     * @param sep separator
     * @param tables list of nodes for tables
     * Use Lists instead of arrays so concatenation is easier (Java doesn't offer native array concatenation
     */
    public CSVTableBuilder(String outputPath,
                           String sep,
                           Map<String, String> nonNestedTags,
                           Map<String, List<Map<String, String>>> tables
    ) throws ParserConfigurationException, IOException, SAXException {
        super(
                outputPath,
                sep,
                getAllTags(nonNestedTags, tables)
        );
        this.nonNestedTags = nonNestedTags;
        this.tables = new ArrayList<>();
        tables.keySet().forEach(k -> tables.get(k).forEach(table -> this.tables.add(new Table<>(k, table))));
    }

    private static List<String> getAllTags(Map<String, String> nonNestedTags, Map<String, List<Map<String, String>>> tables) {
        List<String> result = new ArrayList<>();
        result.addAll(nonNestedTags.keySet());
        tables.keySet().forEach(k ->
                tables.get(k).forEach(t ->
                        result.addAll(t.keySet())));
        return result;
    }

    @Override
    public void outputForm() throws OutputException { //TODO: think abt implementation with one string instead of List<List<String>>, also measure the speedup
        List<List<String>> tables1 = tables.stream().map(t -> t.getLine()).collect(Collectors.toList());
        List<List<String>> lines = computeCrossProduct(tables1);
        lines.forEach(l -> l.addAll(nonNestedTags.values())); //add non nested tags values //TODO: maybe do this in recursive call so you dont need to iterate over everything again
        System.out.println(lines);
//        super.outputForm(lines);
    }

    /**
     * Method computes cross product, in this case used for cross product of tables, which is used to display tree-like datastructure (XML) as flat list (CSV)
     * This leads to an exponential space complexity which is not advisable (consider using a different way of converting Forms (eg database, hierarchical)
     * @param tables
     * @return
     */
    private <T> List<List<T>> computeCrossProduct(List<List<T>> elems) {
        if (elems.size() == 1)
            return elems;

        List<List<T>> result = new ArrayList<>();

        List<T> firstElem = elems.get(0);
        //elems only contain rest of elements without first element
        List<List<T>> restOfProduct = computeCrossProduct(elems.subList(1, elems.size()));

//        for (List<T> elem : elems) {
        for (List<T> restElem : restOfProduct) {
            List<T> newLine = new ArrayList<T>();
            newLine.addAll(firstElem);
            newLine.addAll(restElem);
            result.add(newLine);
        }
//        }

        return result;
    }

//    private List<List<String>> computeCrossProduct(List<List<String>> tables) {
//        if (tables.size() == 1) {
//            return tables;
//        }
//
//        List<List<String>> result = new ArrayList<>();
//
//        List<String> firstTable = tables.get(0);
//        List<List<String>> restOfTables = tables.subList(1, tables.size());
//
//        List<List<String>> restOfProduct = computeCrossProduct(restOfTables);
//
//
//        for (String element : firstTable) {
//            for (List<String> rest : restOfProduct) {
//                List<String> newSet = new ArrayList<>();
//                newSet.add(element);
//                newSet.addAll(rest);
//                result.add(newSet);
//            }
//        }
//        return result;
//    }
/*
    private void computeCrossProduct(List<Table<String>> tables, List<List<String>> crossProduct, ArrayList<String> currentRow, int index) {
        // if we have reached the end of the tables, add the current row to the cross product and return
        if (index == tables.size()) {
            crossProduct.add(new ArrayList<>(currentRow));
            return;
        }

        // iterate over each row in the current table
        for (String value : tables.get(index).getLine()) {
            // add the current value to the current row
            currentRow.add(value);
            // recursively call the function with the next table
            computeCrossProduct(tables, crossProduct, currentRow, index + 1);
            // remove the current value from the current row
            currentRow.remove(currentRow.size() - 1);
        }
    }
*/

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
        return cols.size();
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
        return containsTag(this.cols, tag);
    }


}