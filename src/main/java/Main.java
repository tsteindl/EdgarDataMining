import util.Constants;
import csv.CSVBuilder;
import csv.CSVTableBuilder;
import util.DailyData;

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static util.Constants.*;

/**
 * @author Tobias Steindl tobias.steindl@gmx.net
 * @version 1
 * PROGRAM ARGS:
 * 1. path in EDGAR file system, suffix with "/" if there are subdirectories
 * 2. multithreading: -conc=true/false
 * 3. doStats: -stats=true/false time costly functions, print out statistics at end of parsing
 */
public class Main {

    public static void main(String[] args) throws IOException {
        //xml data from 2004 onwards
        //BASE PATH = https://www.sec.gov/Archives/

        //get program args
        String path = Constants.DEFAULT_YEAR;
        boolean conc = false;
        boolean doStats = false;

        if (args.length == 1) {
            path = args[0];
        }
        if (args.length == 2) {
            path = args[0];
            conc = args[1].equals("-conc=true") || args[1].equals("-conc=1");
        }
        if (args.length == 3) {
            path = args[0];
            conc = args[1].equals("-conc=true") || args[1].equals("-conc=1");
            doStats = args[2].equals("-stats=true") || args[2].equals("-stats=1");
        }

        System.out.println("----------------------------");
        System.out.println("Starting application with program args: ");
        System.out.println("Path: " + path);
        System.out.println("conc: " + conc);
        System.out.println("doStats: " + doStats);
        System.out.println("----------------------------");

        System.out.println("-------------------------------------------------");
        System.out.format("Mining data for path: %s %s.", path, (conc) ? "concurrently" : "sequentially.\n");
        System.out.println("-------------------------------------------------");
        if (conc)
            executeConcurrently(path);
        else
            executeSequentially(path);
        System.out.println("-------------------------------------------------");
        System.out.format("Application ended");
        System.out.println("-------------------------------------------------");
    }

    public static void executeSequentially(String path) {

        EdgarParser eParser = new EdgarParser("4");
        List<String> idxFilesList = new CopyOnWriteArrayList<>();

        Form4Parser parser = new Form4Parser();

        CSVBuilder csvTableBuilder = new CSVTableBuilder(
                ";",
                List.of(CSV_TAG_NAMES_REP),
                List.of(CSV_TAGS_REP),
                List.of(CSV_TAG_NAMES_TABLE),
                List.of(CSV_TAGS_TABLE),
                List.of(TABLE_NODE_TAGS),
                List.of(CSV_DOCUMENT_ROOT),
                List.of(NULLABLE_TAGS)
        );

        eParser.getIdxFiles(path, idxFilesList);

        List<DailyData> ddList = new CopyOnWriteArrayList<>();

        while (!idxFilesList.isEmpty()) {
            try {
                eParser.parseIndexFile(idxFilesList.remove(0), ddList); //TODO: multithreading here
                String outputPath = "data/output" + ddList.get(0).dateFiled() + ".csv";

                try (Writer writer = new BufferedWriter(new FileWriter(outputPath))) {
                    writer.write(csvTableBuilder.getHeader());
                }

                List<String> data = new CopyOnWriteArrayList<>();
                while (!ddList.isEmpty()) {
                    try {
                        eParser.downloadData(ddList.remove(0), data);
                        while (!data.isEmpty()) {
                            parser.parseForm4String(data.remove(0), csvTableBuilder);
                        }
                        String output = csvTableBuilder.outputCsv();
                        if (output == null) continue;
                        try (Writer writer = new BufferedWriter(new FileWriter(outputPath, true))) {
                            writer.append(output);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void executeConcurrently(String path) {


    }
}
