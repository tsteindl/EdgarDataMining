package csv;

import com.google.common.collect.Streams;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: maybe refactor into composite builder with list of tablebuilders
//TODO: add support for multiple tables => derivativeTable
public class CSVTableBuilder /*extends CSVBuilder*/ {

    private String outputPath;
    private String sep;
    private final List<Table> tables;
    private final Map<List<String>, String> tags;
    private final String documentRoot;
    private final List<String> tableNodeTags;
    private List<String> excludeTags;


    /**
     * Init CSV Builder with table. Repeating tags are the ones that are the same for each table entry: eg the reporter of a form
     * @param outputPath the output path of the generated output //TODO: change this for non CSV
     * @param sep separator
     * @param tableNodeTags list of nodes for tables
     * @param documentRoot
     * @param excludeTags nodes that will not be parsed
     * Use Lists instead of arrays so concatenation is easier (Java doesn't offer native array concatenation
     */
    public CSVTableBuilder(String outputPath,
                           String sep,
                           List<String> tableNodeTags,
                           String documentRoot,
                           List<String> excludeTags) {
        this.tags = new HashMap<>();
        this.outputPath = outputPath;
        this.sep = sep;
        this.tableNodeTags = tableNodeTags;
        this.documentRoot = documentRoot;
        this.excludeTags = excludeTags;
//        getAllRepeatingTags(tags, documentRoot);
//        this.tables = initTables(tableNamesList, tableTagsList, this.tableNodeTags);
        this.tables = new ArrayList<Table>();
        resetTableBuilder();
    }

    class Table {
        private final Map<List<String>, String> tableTags;
        private final Map<String, String> currTableVals;


        Table(Map<List<String>, String> tableTags) {
            this.tableTags = tableTags;
            this.currTableVals = new HashMap<>();
        }

        private void clear() {
            this.currTableVals.clear();
        }

        private void reset() {
            this.currTableVals.clear();
            initMap(this.currTableVals, this.tableTags.values());
        }

        public int getTableTagsColSize() {
            return this.tableTags.keySet().size();
        }

    }

    private ArrayList<Table> initTables(List<String[]> tableNamesList, List<String[][]> tableTagsList, List<String[]> tableNodeTags) {
        ArrayList<Table> result = new ArrayList<>();
        for (int i = 0; i < tableNamesList.size(); i++) {
            Map<List<String>, String> tags = new HashMap<>();
            for (int j = 0; j < tableNamesList.get(i).length; j++)
                tags.put(Stream.concat(documentRoot.stream(), Stream.concat(Arrays.stream(tableNodeTags.get(i)), Arrays.stream(tableTagsList.get(i)[j]))).toList(), tableNamesList.get(i)[j]);
            result.add(new Table(tags));
        }
        return result;
    }

    //TODO: refactor this to be functional (return type)
    // refactor this to map collect https://stackoverflow.com/questions/14513475/how-do-i-iterate-over-multiple-lists-in-parallel-in-java
    // find way to use one generic function for both get All functions
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

    private void getAllRepeatingTags(List<String> repTagNames, List<String[]> repTags, List<String> documentRoot) {
        Streams.forEachPair(repTagNames.stream(), repTags.stream(), (name, tags) -> this.tags.put(Stream.concat(documentRoot.stream(), Arrays.stream(tags)).toList(), name));
    }

    //TODO: refactor these 2 as 1 fct?
    private static List<String> getNestedTableNames(List<String> repTagNames, List<String[]> tableNames) {
        return Stream.concat(repTagNames.stream(), tableNames.stream().map(Arrays::asList).flatMap(List::stream))
                .collect(Collectors.toList());
    }

    private static List<String[]> getNestedTableTags(List<String[]> repTags, List<String[][]> tableTags) {
        return Stream.concat(repTags.stream(),
                        tableTags.stream().map(Arrays::asList).flatMap(List::stream))
                .collect(Collectors.toList());
    }

    public int getRepTagsColSize() {
        return this.tags.keySet().size();
    }

    public void resetTableBuilder() {
        this.currRepVals.clear();
//        this.currTableVals.clear();
        initMap(this.currRepVals, this.tags.values());
        this.tables.forEach(Table::reset);
    }

    public void addEntryToCurrLine(List<String> tag, String value) {
        if (isRepeatingTag(tag))
            currRepVals.put(this.tags.get(tag), value);
        else {
            Optional<Table> table = getTable(tag);
            if (table.isEmpty()) return;
            table.get().currTableVals.put(table.get().tableTags.get(tag), value);
        }
        if (currLineFull())
            addCurrLine();
    }

    public boolean currLineFull() { //TODO: make sure this works if not all values are provided
        return mapFull(currRepVals, this.nullableTags)
            && this.tables.stream().allMatch(t -> mapFull(t.currTableVals, this.nullableTags));
    }

    public void addCurrLine() {
        this.tables.forEach(t -> {
            addLine(this.currRepVals, t.currTableVals);
            initMap(t.currTableVals, t.tableTags.values());
        });
    }

    private void addLine(Map<String, String> repVals, Map<String, String> tableVals) {
        Map<String, String> mergedMap = new HashMap<>(repVals);
        mergedMap.putAll(tableVals);
        addLine(mergedMap);
    }

    /**
     * Function that determines if tag is part of nested table or of repeating tags
     * @param tag
     * @return boolean
     */
    private boolean isRepeatingTag(List<String> tag) {
        return containsTag(this.tags, tag);
    }

    /**
     * Gets table that a tag belongs to (if there is one)
     * @param tag
     * @return Table
     */
    private Optional<Table> getTable(List<String> tag) {
        return this.tables.stream().filter((Table t) -> containsTag(t.tableTags, tag)).findFirst();
    }

}