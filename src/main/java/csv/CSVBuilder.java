package csv;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import interfaces.FormConverter;
import util.OutputException;

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
        return String.join("\n", ls);
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