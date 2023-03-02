package csv;

import com.google.common.collect.Streams;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: maybe refactor into composite builder with list of tablebuilders
//TODO: add support for multiple tables => derivativeTable
public class CSVTableBuilder extends CSVBuilder {

    private final Map<List<String>, String> repTags;
    private final Map<List<String>, String> tableTags;

    private final List<String> documentRoot;
    private final List<String[]> tableNodeTags;
    private final Map<String, String> currRepVals;
    private final Map<String, String> currTableVals;

    /**
     * Init CSV Builder with table. Repeating tags are the ones that are the same for each table entry: eg the reporter of a form
     * @param outputPath the output path of the generated output //TODO: change this for non CSV
     * @param sep separator
     * @param repTagNames mapping of the repeating tags to output document names
     * @param repTags repeating tags
     * @param tableNamesList mapping of table tags
     * @param tableTagsList table tags
     * @param tableNodeTags list of list of nodes for tables
     * @param documentRoot
     * @param notNullTags
     * Use Lists instead of arrays so concatenation is easier (Java doesn't offer native array concatenation
     */
    public CSVTableBuilder(String outputPath,
                           String sep,
                           List<String> repTagNames,
                           List<String[]> repTags,
                           List<String[]> tableNamesList,
                           List<String[][]> tableTagsList,
                           List<String[]> tableNodeTags,
                           List<String> documentRoot,
                           List<String> notNullTags
    ) {
        super(
                outputPath,
                sep,
                getNestedTableNames(repTagNames, tableNamesList),
                getNestedTableTags(repTags, tableTagsList),
                documentRoot,
                notNullTags
        );
        this.repTags = new HashMap<>();
        this.tableTags = new HashMap<>();

        this.documentRoot = documentRoot;
        this.tableNodeTags = tableNodeTags;
        getAllRepeatingTags(repTagNames, repTags, documentRoot);
        getAllTableTags(tableNamesList, tableTagsList);

        this.currRepVals = new HashMap<>();
        this.currTableVals = new HashMap<>();
        resetTableBuilder();
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
        Streams.forEachPair(repTagNames.stream(), repTags.stream(), (name, tags) -> this.repTags.put(Stream.concat(documentRoot.stream(), Arrays.stream(tags)).toList(), name));
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
        return this.repTags.keySet().size();
    }

    public int getTableTagsColSize() {
        return this.tableTags.keySet().size();
    }

    public void resetTableBuilder() {
        this.currRepVals.clear();
        this.currTableVals.clear();
        initMap(this.currRepVals, this.repTags.values());
        initMap(this.currTableVals, this.tableTags.values());
    }

    public void addEntryToCurrLine(List<String> tag, String value) {
        Map<List<String>, String> table = whichTableContains(tag);
        if (table == null) return;
        if (table == this.repTags)
            currRepVals.put(table.get(tag), value);
        else if (table == this.tableTags)
            currTableVals.put(table.get(tag), value);

        if (currLineFull())
            addCurrLine();
    }

    public boolean currLineFull() { //TODO: make sure this works if not all values are provided
        return mapFull(currRepVals, this.nullableTags)
            && mapFull(currTableVals, this.nullableTags);
    }

    public void addCurrLine() {
        addLine(this.currRepVals, this.currTableVals);
        initMap(this.currTableVals, this.tableTags.values());
    }

    private void addLine(Map<String, String> repVals, Map<String, String> tableVals) {
        Map<String, String> mergedMap = new HashMap<>(repVals);
        mergedMap.putAll(tableVals);
        addLine(mergedMap);
    }


    private Map<List<String>, String> whichTableContains(List<String> tag) {
        if (containsTag(this.repTags, tag)) return this.repTags;
        if (containsTag(this.tableTags, tag)) return this.tableTags;
        return null;
    }
}