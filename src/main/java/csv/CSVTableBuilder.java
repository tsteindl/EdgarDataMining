package csv;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: maybe refactor into composite builder with list of tablebuilders
//TODO: add support for multiple tables => derivativeTable
public class CSVTableBuilder extends CSVBuilder {

    private final Map<List<String>, String> repTags;
    private final Map<List<String>, String> tableTags;

    private final List<String> documentRoot;
    private final List<String> tableNodeTags;
    private final Map<String, String> currRepVals;
    private final Map<String, String> currTableVals;

    public CSVTableBuilder(String sep, List<String> repTagNames, List<String[]> repTags, List<String> tableNames, List<String[]> tableTags, List<String> tableNodeTags, List<String> documentRoot, List<String> notNullTags) {
        super(sep,
                Stream.concat(repTagNames.stream(), tableNames.stream())
                        .collect(Collectors.toList()),
                Stream.concat(repTags.stream(), tableTags.stream())
                        .collect(Collectors.toList()),
                documentRoot,
                notNullTags
        );
        this.repTags = new HashMap<>();
        this.tableTags = new HashMap<>();

        this.documentRoot = documentRoot;
        this.tableNodeTags = tableNodeTags;
        for (int i = 0; i < repTagNames.size(); i++) {
            List<String> tagsListWithDocumentRoot = Stream.concat(documentRoot.stream(), Arrays.asList(repTags.get(i)).stream()).toList();
            this.repTags.put(tagsListWithDocumentRoot, repTagNames.get(i));
        }
        for (int i = 0; i < tableNames.size(); i++) {
            List<String> tagsListWithDocumentRoot = Stream.of(documentRoot, tableNodeTags, Arrays.asList(tableTags.get(i))).flatMap(Collection::stream).collect(Collectors.toList());
            this.tableTags.put(tagsListWithDocumentRoot, tableNames.get(i));
        }
        this.currRepVals = new HashMap<>();
        this.currTableVals = new HashMap<>();
        resetTableBuilder();
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