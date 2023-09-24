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
     *
     * @param outputPath the output path of the generated output //TODO: change this for non CSV
     * @param sep        separator
     * @param tables     list of nodes for tables
     *                   Use Lists instead of arrays so concatenation is easier (Java doesn't offer native array concatenation
     */
    public CSVTableBuilder(String outputPath,
                           String sep,
                           LinkedHashMap<String, Object> nonNestedTags,
                           LinkedHashMap<String, List<? extends TableType>> tables
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
     *
     * @param nonNestedTags
     * @param tables
     * @return
     */
    private static List<String> getAllTags(Map<String, Object> nonNestedTags, Map<String, List<? extends TableType>> tables) {
        List<String> result = new ArrayList<>();
        result.addAll(nonNestedTags.keySet());
        tables.keySet().forEach(k -> {
            if (!tables.get(k).isEmpty()) {
                TableType table = tables.get(k).get(0);
                result.addAll(table.keys().stream().map(key -> table.getId() + "_" + key).toList());
            }
        });
        return result;
    }

    /**
     * Get all lines of the CSV document, computes the cross product among all tables for a lossless (but redundant) representation.
     * Only required because CSV cannot have hierarchical structure or relations to other tables
     *
     * @param tables
     * @param nonNestedTags
     * @return
     */
    private static List<List<String>> getLines(LinkedHashMap<String, List<? extends TableType>> tables, Map<String, Object> nonNestedTags) {
        //get only values of tables
        List<List<String>> tableVals = tables.values().stream()
                .flatMap(List::stream)
                .map(t -> t.values().stream().map(Object::toString).collect(Collectors.toList()))
                .collect(Collectors.toList());
//        LinkedHashMap<String, List<? extends TableType>> tablees = tables.values().stream().map(table -> table.stream().map(elem -> elem.toString()).toList()).toList();

        //TODO: do this with lambda
        List<List<List<String>>> tablesMapped = new ArrayList<>();
        for (String key : tables.keySet()) {
            List<List<String>> tableList = new ArrayList<>();
            for (TableType table : tables.get(key)) {
                List<String> tableMap = new ArrayList<>();
                for (Object o : table.values()) {
                    tableMap.add(o.toString());
                }
                tableList.add(tableMap);
            }
            tablesMapped.add(tableList);
        }
        List<List<String>> lines = computeCartesianProduct(tablesMapped);
        //DO NOT CHANGE THE FOLLOWING LINE
        lines.forEach(l -> l.addAll(0, nonNestedTags.values().stream().map(v -> (v == null) ? "" : v.toString()).toList())); //add non nested tags values //TODO: maybe do this in recursive call so you dont need to iterate over everything again, a lot of array operations are used here
        return lines;
    }

    public static List<List<String>> computeCartesianProduct(List<List<List<String>>> tables) {
        List<List<String>> result = new ArrayList<>();
        computeCartesianProductRecursively(tables, 0, new ArrayList<>(), result);
        return result;
    }

    /**
     * Method recursively computes cross product, in this case used for cross product of tables, which is used to display tree-like datastructure (XML) as flat list (CSV)
     * This leads to an exponential space complexity which is not advisable (consider using a different way of converting Forms (eg database, hierarchical)
     * @param tables
     * @param index
     * @param current
     * @param result
     */
    //TODO: use generics, use streams
    public static void computeCartesianProductRecursively(List<List<List<String>>> tables, int index, List<List<String>> current, List<List<String>> result) {
        if (index == tables.size() || tables.get(index) == null || tables.get(index).isEmpty()) {
            List<String> appendRow = new ArrayList<>(); //TODO: make this more efficient
            for (List<String> row : current) {
                for (String col : row) {
                    appendRow.add(col);
                }
            }
            result.add(appendRow);
            return;
        }
        List<List<String>> currentTable = tables.get(index);
        for (List<String> row : currentTable) {
            current.add(row);
            computeCartesianProductRecursively(tables, index + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
}