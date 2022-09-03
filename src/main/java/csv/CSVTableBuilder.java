package csv;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
//TODO: add support for multiple tables => derivativeTable
public class CSVTableBuilder extends CSVBuilder {

    private final Map<String, List<String>> repTags;
    private final Map<String, List<String>> tableTags;

    private String tableNodeTag;
    private String[] currRepVals;

    public CSVTableBuilder(String sep, List<String> repTagNames, List<String[]> repTags, List<String> tableNames, List<String[]> tableTags, String tableNodeTag) {
        super(sep,
                Stream.concat(repTagNames.stream(), tableNames.stream())
                        .collect(Collectors.toList()),
                Stream.concat(repTags.stream(), tableTags.stream())
                        .collect(Collectors.toList())
        );
        this.repTags = new HashMap<>();
        this.tableTags = new HashMap<>();
        this.tableNodeTag = tableNodeTag;
        for (int i = 0; i < repTagNames.size(); i++)
            this.repTags.put(repTagNames.get(i), Arrays.asList(repTags.get(i)));
        for (int i = 0; i < repTagNames.size(); i++)
            this.tableTags.put(tableNames.get(i), Arrays.asList(tableTags.get(i)));
    }

    public int getRepTagsColSize() {
        return this.repTags.keySet().size();
    }

    public int getTableTagsColSize() {
        return this.tableTags.keySet().size();
    }

    public void setCurrRepVals(String[] input) {
        if (input.length != getRepTagsColSize())
            throw new IllegalArgumentException(String.format("Repeating values sizes differ %s != %s", input.length, getRepTagsColSize()));
        this.currRepVals = input;
    }

/*
    public void addEntryToCurrLine(String key, String value) {
        this.currLine.put(key, value);
        if (currLineFull())
            addCurrLine();
    }
*/


    public String[] getCurrRepVals() {
//        return Arrays.stream(this.currRepVals).collect(Collectors.joining(this.sep));
        return this.currRepVals;
    }

    public void addLine(String[] input) {
        if (getCurrRepVals() == null) throw new IllegalArgumentException("Repeating columns values must be set first");
        if (input.length != getTableTagsColSize()) throw new IllegalArgumentException(String.format("Table value sizes differ %s != %s", input.length, getTableTagsColSize()));
        super.addLine(getCurrRepVals(), input);
    }
}