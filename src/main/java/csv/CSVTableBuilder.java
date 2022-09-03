package csv;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: maybe refactor into composite builder with list of tablebuilders
//TODO: add support for multiple tables => derivativeTable
public class CSVTableBuilder extends CSVBuilder {

    private final Map<List<String>, String> repTags;
    private final Map<List<String>, String> tableTags;

    private List<String> documentRoot;
    private List<String> tableNodeTags;
    private Map<String, String> currRepVals;
    private Map<String, String> currTableVals;

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

        currRepVals = new HashMap<>();
        currTableVals = new HashMap<>();
        resetMap(this.currRepVals, this.repTags.values());
        resetMap(this.currTableVals, this.tableTags.values());
    }

    public int getRepTagsColSize() {
        return this.repTags.keySet().size();
    }

    public int getTableTagsColSize() {
        return this.tableTags.keySet().size();
    }

/*
    public void setCurrRepVals(String[] input) {
        if (input.length != getRepTagsColSize())
            throw new IllegalArgumentException(String.format("Repeating values sizes differ %s != %s", input.length, getRepTagsColSize()));
        this.currRepVals = input;
    }
*/

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
        resetMap(this.currTableVals, this.tableTags.values());
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

    public Map<String, String> getCurrRepVals() {
//        return Arrays.stream(this.currRepVals).collect(Collectors.joining(this.sep));
        return this.currRepVals;
    }

/*
    public void addLine(String[] input) {
        if (getCurrRepVals() == null) throw new IllegalArgumentException("Repeating columns values must be set first");
        if (input.length != getTableTagsColSize())
            throw new IllegalArgumentException(String.format("Table value sizes differ %s != %s", input.length, getTableTagsColSize()));
        super.addLine(getCurrRepVals(), input);
    }
*/
}