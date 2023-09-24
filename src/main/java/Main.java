import Form4Parser.Form4Parser;
import Form4Parser.CSVForm4Parser;
import interfaces.FormConverter;
import org.apache.commons.lang3.time.StopWatch;
import statistics.Stats;
import util.*;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Tobias Steindl tobias.steindl@gmx.net
 * @version 1.0
 * PROGRAM ARGS:
 * 1. path in EDGAR file system, suffix with "/" if there are subdirectories: -path=...
 * 2. multithreading: -conc=true/false
 * 3. doStats: -stats=true/false time costly functions, print out statistics at end of parsing
 * example:
 */
public class Main {
    public static Stats stats;
    public static BigInteger startTime;
    public static double totalTimeTaken; //total time taken in seconds
    public static long nOForms = 0;
    public static List<String> failedForms = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        //xml data from 2004 onwards
        //BASE PATH = https://www.sec.gov/Archives/
        Map<String, Object> argsMap = parseProgramArgs(args);
        //get program args
        String path = (String) argsMap.get("path");
        boolean conc = (boolean) argsMap.get("conc");
        boolean doStats = (boolean) argsMap.get("doStats");
        if (argsMap.get("files") != null) {
            String[] files = ((String) argsMap.get("files")).split(",");
            System.out.println("----------------------------");
            System.out.println("Starting application with program args: ");
            System.out.println("Files: " + Arrays.toString(files));
            System.out.println("conc: " + conc);
            System.out.println("doStats: " + doStats);
            System.out.println("----------------------------");

            startTime = new BigInteger(Long.toString(System.nanoTime()));
            StopWatch watch = new StopWatch();
            watch.start();
            if (conc)
                return;
            else
                executeFilesSequentially(files, FormConverter.Outputter.CSV); //TODO: add program arg
            totalTimeTaken = Stats.nsToSec(new BigInteger(Long.toString(System.nanoTime())).subtract(startTime));
            watch.stop();
//        totalTimeTaken = watch.getTime();
            System.out.println("-------------------------------------------------");
            System.out.println("Application ended");
            System.out.println("Total time taken: " + totalTimeTaken + "s");
            System.out.println("Number of Forms parsed: " + nOForms);
            System.out.println("Avg seconds per form: " + ((double) totalTimeTaken / nOForms));
            System.out.println("Number of erroneous forms: " + failedForms.size());
            System.out.println("Erroneous forms: " + failedForms.toString());
            System.out.println("-------------------------------------------------");

            return;
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
        startTime = new BigInteger(Long.toString(System.nanoTime()));
        StopWatch watch = new StopWatch();
        watch.start();
        if (conc) {
            try {
                executeConcurrently(path, FormConverter.Outputter.CSV);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        else
            executeSequentially(path, FormConverter.Outputter.CSV); //TODO: add program arg
        totalTimeTaken = Stats.nsToSec(new BigInteger(Long.toString(System.nanoTime())).subtract(startTime));
        watch.stop();
//        totalTimeTaken = watch.getTime();
        System.out.println("-------------------------------------------------");
        System.out.println("Application ended");
        System.out.println("Total time taken: " + totalTimeTaken + "s");
        System.out.println("Number of Forms parsed: " + nOForms);
        System.out.println("Avg seconds per form: " + ((double) totalTimeTaken / nOForms));
        System.out.println("Number of erroneous forms: " + failedForms.size());
        System.out.println("Erroneous forms: " + failedForms.toString());
        System.out.println("-------------------------------------------------");
    }

    private static Map<String, Object> parseProgramArgs(String[] args) {
        Map<String, Object> result = new HashMap<>();
        //Default values
        result.put("path", Constants.DEFAULT_YEAR);
        result.put("conc", false);
        result.put("doStats", false);
        List<String> as = new LinkedList<String>(Arrays.asList(args)); //LinkedList supports faster remove than ArrayList
        String curr = null;
        String next = null;
        String next1 = null;
        String next2 = null;
        for (int i = 0; !as.isEmpty(); i++) {
            curr = next;
            next = as.remove(0);
            next1 = next;
            if (next.split("=").length >= 2) {
                next1 = next.split("=")[0];
                next2 = next.split("=")[1];
            }

            if (next1.equals("-path")) {
                result.put("path", next2);
            } else if (next1.equals("-conc")) {
                Boolean bool = cmdBool(next2);
                result.put("conc", bool);
            } else if (next1.equals("-stats")) {
                Boolean bool = cmdBool(next2);
                result.put("doStats", bool);
            } else if (next1.equals("-files")) {
                result.put("files", next2);
            }
        }
        return result;
    }

    private static boolean cmdBool(String bool) {
        return Boolean.parseBoolean(bool) || "1".equals(bool);
    }


    public static void executeSequentially(String path, FormConverter.Outputter outputType) {
//        stats = new Stats();
        EdgarScraper edgarScraper = new EdgarScraper("4");
        //wait until all idx files are downloaded (TODO problem: recursion)
        edgarScraper.scrapeIndexFiles(path, edgarScraper.getIndexFiles(), 100);
        for (IndexFile idxFile : edgarScraper.getIndexFiles()) {
            handleIndexFile(outputType, idxFile, edgarScraper);
        }
    }

    private static void handleIndexFile(FormConverter.Outputter outputType, IndexFile idxFile, EdgarScraper edgarScraper) {
        try {
            List<DailyData> dailyDataList = edgarScraper.parseIndexFile(idxFile);
            String outputFolder = "data/" + idxFile.path().replace(".idx", "");
            for (DailyData dailyData : dailyDataList) {
                String outputPath = outputFolder + "/" + dailyData.folderPath().replace("/", "_").replace(".txt", "") + ".csv"; //TODO: fix temporary solution
                try {
                    String responseData = edgarScraper.downloadData(dailyData, 100);
                    Form4Parser form4Parser = new CSVForm4Parser(dailyData.folderPath(), responseData);
                    form4Parser.parseForm();
                    FormConverter outputter = form4Parser.configureOutputter(outputPath, outputType);
                    outputter.outputForm();
                    nOForms++;
                } catch (ParseFormException e) {
                    e.printStackTrace();
                    failedForms.add(dailyData.folderPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void executeFilesSequentially(String[] files, FormConverter.Outputter outputType) {
        EdgarScraper edgarScraper = new EdgarScraper("4");
        for (String file : files) {
            DailyData dailyData = new DailyData("4", "", "", "", file);
            String outputPath = "data/output_" + file.replace("/", "_") + ".csv"; //TODO: fix temporary solution
            try {
                String responseData = edgarScraper.downloadData(dailyData, 100);
                Form4Parser form4Parser = new CSVForm4Parser(dailyData.folderPath(), responseData);
                form4Parser.parseForm();
                FormConverter outputter = form4Parser.configureOutputter(outputPath, outputType);
                outputter.outputForm();
                nOForms++;
            } catch (ParseFormException | IOException | InterruptedException | OutputException e) {
                e.printStackTrace();
                failedForms.add(dailyData.folderPath());
            }
        }
    }


    public static void executeConcurrently(String path, FormConverter.Outputter outputType) throws InterruptedException {
        EdgarScraper edgarScraper = new EdgarScraper("4");
        List<Runnable> downloadQueueIndexFile = new CopyOnWriteArrayList<>();
        ObservableCopyOnWriteArrayList<IndexFile> indexFiles = new ObservableCopyOnWriteArrayList<>();

        indexFiles.addListener(idxFile -> {
            System.out.println("Added idxFile");
            handleIndexFile(outputType, idxFile, edgarScraper);
        });

        edgarScraper.scrapeIndexFilesConc(path, downloadQueueIndexFile, indexFiles);

        System.out.println(downloadQueueIndexFile);

        downloadQueueIndexFile.forEach(runnable -> {
            runnable.run();
        });
    }
}
