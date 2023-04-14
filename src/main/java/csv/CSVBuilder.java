package csv;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import interfaces.FormConverter;
import util.OutputException;

/*public class CSVBuilder implements FormConverter {
    private final String outputPath;
    private final Map<List<String>, String> tags;
    private final List<String> lines;
    final String sep;
    protected final List<String> nullableTags;
    private final Map<String, String> currLine;

    public CSVBuilder(String outputPath, String sep, List<String> names, List<String[]> tags, List<String> documentRoot, List<String> nullableTags) {
        this.outputPath = outputPath;
        if (names.size() != tags.size())
            throw new IllegalArgumentException("Length of names and tags arrays must match");
        this.sep = sep;
        //use linked list as dynamic access will not be needed but lots of items will be added
        this.lines = new LinkedList<>();
        this.tags = new LinkedHashMap<>();
        for (int i = 0; i < names.size(); i++) {
            List<String> tagsListWithDocumentRoot = Stream.concat(documentRoot.stream(), Arrays.asList(tags.get(i)).stream()).toList();
            this.tags.put(tagsListWithDocumentRoot, names.get(i));
        }
        this.nullableTags = nullableTags;
        this.currLine = new HashMap<>();
        reset();
    }

    public void init() throws InitException {
        try (Writer writer = new BufferedWriter(new FileWriter(this.outputPath))) {
            writer.write(this.getHeader());
        } catch (IOException e) {
            throw new InitException(e.getMessage());
        }
    }

    void initMap(Map<String, String> map, Collection<String> keySet) {
        for (String name : keySet)
            map.put(name, null);
    }

    public void reset() {
        currLine.clear();
        initMap(this.currLine, this.tags.values());
        this.lines.clear();
    }

    public String getHeader() {
        return this.tags.values().stream().collect(Collectors.joining(this.sep));
    }

    public int getColSize() {
        return this.tags.keySet().size();
    }

    public void addEntryToCurrLine(List<String> tag, String value) {
        if (!containsTag(this.tags, tag)) return;
        this.currLine.put(getTagName(tag), value);
        if (currLineFull()) addCurrLine();
    }

    public boolean currLineFull() {
        return mapFull(currLine, this.nullableTags);
    }

    public void addCurrLine() {
        addLine(this.currLine);
        initMap(this.currLine, this.tags.values());
    }

    //Caution when using this method
    public void addLine(String input) {
//        System.out.println(String.format("Line added:\n%s", input));
        this.lines.add(input);
    }

    public void addLine(String[] input) {
        if (input.length != getColSize())
            throw new IllegalArgumentException(String.format("Line to be added (size = %d) does not match size of CSV builder (size = %d)", input.length, getColSize()));
        addLine(String.join(this.sep, input));
    }

    public void addLine(String[]... input) {
        String[] combined = Arrays.stream(input).flatMap(Arrays::stream).toArray(String[]::new);
        if (combined.length != getColSize())
            throw new IllegalArgumentException(String.format("Line to be added (size = %d) does not match size of CSV builder (size = %d)", input.length, getColSize()));
        addLine(String.join(this.sep, combined));
    }

    public void addLine(Map<String, String> input) {
        if (input.size() != getColSize())
            throw new IllegalArgumentException(String.format("Line to be added (size = %d) does not match size of CSV builder (size = %d)", input.size(), getColSize()));
        StringBuilder sb = new StringBuilder();
        for (String tagName : this.tags.values()) {
            if (!input.containsKey(tagName))
                throw new IllegalArgumentException(String.format("Tag %s not present in CSV", tagName));
            String output = input.get(tagName);
            sb.append((output == null) ? "" : output);
            sb.append(sep);
        }
        addLine(sb.toString());
    }

    public void outputForm() throws OutputException {
        String output = outputCsv();
        if (output == null) return;
        try (Writer writer = new BufferedWriter(new FileWriter(outputPath, true))) {
            writer.append(output);
        } catch (IOException e) {
            throw new OutputException(e.getMessage());
        }
    }

    public String outputCsv() {
        if (this.lines.isEmpty()) return null;
        String result = this.lines.stream().collect(Collectors.joining("\n"));
        reset();
        return "\n" + result;
    }

    public boolean containsTag(Map<List<String>, String> tagList, List<String> tag) {
        if (tagList == null || tag == null) return false;
        return tagList.containsKey(tag);
    }

    protected boolean mapFull(Map<String, String> map, List<String> exceptions) {
        return (exceptions == null)
                ? map.keySet().stream()
                        .allMatch(key -> map.get(key) != null)
                : map.keySet().stream()
                        .filter(key -> !exceptions.contains(key))
                        .allMatch(key -> map.get(key) != null);
    }

    public String getTagName(List<String> key) {
        return this.tags.get(key);
    }
}*/

public class CSVBuilder implements FormConverter {
    private final String outputPath;
    protected final List<String> cols;
    protected final List<List<String>> lines;

    final String sep;

    /**
     * CSVBuilder builds CSV from XML, structure in form of nodes that should be parsed is initialized here
     *
     * @param outputPath   the output path of the generated output //TODO: change this for non CSV
     * @param sep          CSV separator
     * @param cols         XML tags
     */
    public CSVBuilder(String outputPath, String sep, List<String> cols, List<List<String>> lines) {
        this.outputPath = outputPath;
        this.sep = sep;
        //use linked list as dynamic access will not be needed but lots of items will be added
        this.cols = cols;
        this.lines = lines;
    }

    public String getHeader() {
        return String.join(this.sep, this.cols);
    }


    public String getBody(List<List<String>> lines) {
        if (lines.isEmpty()) return null;
        List<String> ls = lines.stream().map(l -> String.join(this.sep, l)).collect(Collectors.toList());
        String result = String.join("\n", ls);
        return result;
    }

    public void outputForm() throws OutputException {
        String output = getHeader();
        output += "\n";
        output += getBody(this.lines);
        if (output == null) return;
        try (Writer writer = new BufferedWriter(new FileWriter(outputPath, true))) {
            writer.append(output);
        } catch (IOException e) {
            throw new OutputException(e.getMessage());
        }
    }

    public boolean containsTag(List<String> tagList, String tag) {
        if (tagList == null || tag == null) return false;
        return tagList.contains(tag);
    }
}