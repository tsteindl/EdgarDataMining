package csv;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CSVBuilder {
    private final Map<List<String>, String> tags;
    private final List<String> lines;
    final String sep;
    private final Map<String, String> currLine;

    public CSVBuilder(String sep, List<String> names, List<String[]> tags) {
        if (names.size() != tags.size()) throw new IllegalArgumentException("Length of names and tags arrays must match");
        this.sep = sep;
        //use linked list as dynamic access will not be needed but lots of items will be added
        this.lines = new LinkedList<>();
        this.tags = new HashMap<>();
        for (int i = 0; i < names.size(); i++)
            this.tags.put(Arrays.asList(tags.get(i)), names.get(i));
        this.currLine = new HashMap<>();
        for (String name : this.tags.values())
            currLine.put(name, null);
    }

    private String getHeader() {
        return this.tags.values().stream().collect(Collectors.joining(this.sep));
    }

    public int getColSize() {
        return this.tags.keySet().size();
    }

    public void addEntryToCurrLine(String key, String value) {
        this.currLine.put(key, value);
        if (currLineFull())
            addCurrLine();
    }

    public boolean currLineFull() {
        return this.currLine.keySet().stream().allMatch(key -> key != null);
    }

    public void addCurrLine() {
        addLine(this.currLine);
    }

    //Caution when using this method
    public void addLine(String input) {
        this.lines.add(input);
    }

    public void addLine(String[] input) {
        if (input.length != getColSize()) throw new IllegalArgumentException(String.format("Line to be added (size = %d) does not match size of CSV builder (size = %d)", input.length, getColSize()));
        this.lines.add(String.join(this.sep, input));
    }

    public void addLine(String[]... input) {
        String[] combined = Arrays.stream(input).flatMap(Arrays::stream).toArray(String[]::new);
        if (combined.length != getColSize()) throw new IllegalArgumentException(String.format("Line to be added (size = %d) does not match size of CSV builder (size = %d)", input.length, getColSize()));
        this.lines.add(String.join(this.sep, combined));
    }

    public void addLine(Map<String, String> input) {
        if (input.size() != getColSize()) throw new IllegalArgumentException(String.format("Line to be added (size = %d) does not match size of CSV builder (size = %d)", input.size(), getColSize()));
        StringBuilder sb = new StringBuilder();
        for (String tagName : this.tags.values()) {
            if (!input.containsKey(tagName)) throw new IllegalArgumentException(String.format("Tag %s not present in CSV", tagName));
            sb.append(input.get(tagName));
            sb.append(sep);
        }
        this.lines.add(sb.toString());
    }

    public String outputCsv(String path) {
        return this.getHeader() + this.lines.stream().collect(Collectors.joining());
    }

    public boolean containsTag(List<String> tag) {
        return this.tags.containsValue(tag);
    }

    public String getTagName(List<String> key) {
        return this.tags.get(key);
    }
}
