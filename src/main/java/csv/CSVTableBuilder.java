package csv;

import Form4Parser.FormTypes.TableType;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

//TODO: maybe refactor into composite builder with list of tablebuilders
//TODO: add support for multiple tables => derivativeTable
public class CSVTableBuilder extends CSVBuilder {

    static class Table<T> {
        private String id;
        public final List<String> tags;
        public final Map<String, T> map;

        Table(String id, TableType table) {
            this.id = id;
            List<String> keys = table.keys();
            List<Object> values = table.values();
            if (keys.size() != values.size()) {
                throw new IllegalArgumentException("Keys and values lists must have the same size");
            }
            map = new HashMap<>();

            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                T value = (T) values.get(i).toString();
                map.put(key, value);
            }
            this.tags = table.keys();
        }

        public String getId() {
            return this.id;
        }

        public List<String> getLine() {
//            return map.values().stream().map(v -> this.id + "_" + v).collect(Collectors.toList());
            return map.values().stream().map(v -> (String) v).collect(Collectors.toList()); //TODO: check if this is good code
        }
        public int getNoCols() {
            return this.tags.size();
        }
    }


    private final Map<String, Object> nonNestedTags;
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
                           Map<String, Object> nonNestedTags,
//                           Map<String, List<Map<String, String>>> tables
                           Map<String, List<? extends TableType>> tables
    ) throws ParserConfigurationException, IOException, SAXException {
        super(
                outputPath,
                sep,
                getAllTags(nonNestedTags, tables),
                getLines(tables, nonNestedTags)
        );
        this.nonNestedTags = nonNestedTags;
        this.tables = new ArrayList<>();
        tables.keySet().forEach(k -> tables.get(k).forEach(table -> this.tables.add(new Table<>(k, table))));

        System.out.println(this.lines);
    }

    /**
     * Gets all tags so they can be used as columns of the CSV, in tables it will prepend id of table
     * @param nonNestedTags
     * @param tables
     * @return
     */
    private static List<String> getAllTags(Map<String, Object> nonNestedTags, Map<String, List<? extends TableType>> tables) {
        List<String> result = new ArrayList<>();
        result.addAll(nonNestedTags.keySet());
        tables.keySet().forEach(k ->
                result.addAll(tables.get(k).get(0).keys().stream().map(key -> tables.get(k).get(0).getId()+ "_" + key).toList()));
        return result;
    }

    private static List<List<String>> getLines(Map<String, List<? extends TableType>> tables, Map<String, Object> nonNestedTags) {
        //get only values of tables
        List<List<String>> tableVals = tables.values().stream()
                .flatMap(List::stream)
                .map(t -> t.values().stream().map(Object::toString).collect(Collectors.toList()))
                .collect(Collectors.toList());
        List<List<String>> lines = computeCrossProduct(tableVals);
        lines.forEach(l -> l.addAll(nonNestedTags.values().stream().map(Object::toString).toList())); //add non nested tags values //TODO: maybe do this in recursive call so you dont need to iterate over everything again
        return lines;
    }

    /**
     * Method computes cross product, in this case used for cross product of tables, which is used to display tree-like datastructure (XML) as flat list (CSV)
     * This leads to an exponential space complexity which is not advisable (consider using a different way of converting Forms (eg database, hierarchical)
     * @param elems
     * @return
     */
    public static <T> List<List<T>> computeCrossProduct(List<List<T>> elems) {
        // if we have reached the end of the tables, add the current row to the cross product and return
        if (elems.size() == 1)
            return elems;

        List<List<T>> result = new ArrayList<>();

        List<T> firstElem = elems.remove(0);
        //elems only contain rest of elements without first element
        List<List<T>> restOfProduct = computeCrossProduct(elems);

        for (List<T> restElem : restOfProduct) {
            List<T> newLine = new ArrayList<T>();
            newLine.addAll(firstElem);
            newLine.addAll(restElem);
            result.add(newLine);
        }
        return result;
    }
}