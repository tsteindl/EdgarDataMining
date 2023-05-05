import Form4Parser.Form4Parser;
import interfaces.FormConverter;
import org.apache.commons.lang3.time.StopWatch;
import statistics.Stats;
import util.Constants;
import util.DailyData;

import java.io.*;
import java.math.BigInteger;
import java.util.List;

/**
 * @author Tobias Steindl tobias.steindl@gmx.net
 * @version 1.0
 * PROGRAM ARGS:
 * 1. path in EDGAR file system, suffix with "/" if there are subdirectories
 * 2. multithreading: -conc=true/false
 * 3. doStats: -stats=true/false time costly functions, print out statistics at end of parsing
 * example:
 */
public class Main {
    public static Stats stats;
    public static BigInteger startTime;
    public static double totalTimeTaken; //total time taken in seconds
    public static long nOForms = 0;

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
        startTime = new BigInteger(Long.toString(System.nanoTime()));
        StopWatch watch = new StopWatch();
        watch.start();
        if (conc)
            executeConcurrently(path);
        else
            executeSequentially(path, FormConverter.Outputter.CSV); //TODO: add program arg
        totalTimeTaken = Stats.nsToSec(new BigInteger(Long.toString(System.nanoTime())).subtract(startTime));
        watch.stop();
//        totalTimeTaken = watch.getTime();
        System.out.println("-------------------------------------------------");
        System.out.println("Application ended");
        System.out.println("Total time taken: " + totalTimeTaken + "s");
        System.out.println("Number of Forms parsed: " + nOForms);
        System.out.println("Avg seconds per form: " + ((double) totalTimeTaken/nOForms));
        System.out.println("-------------------------------------------------");
    }

    public static void executeSequentially(String path, FormConverter.Outputter outputType) {
        stats = new Stats();
        EdgarScraper edgarScraper = new EdgarScraper("4");
        //wait until all idx files are downloaded (problem: recursion)
        edgarScraper.scrapeIndexFiles(path);
        for (String idxFile : edgarScraper.getIndexFiles()) {
            try {
                List<DailyData> dailyDataList = edgarScraper.parseIndexFile(idxFile);
                String outputPath = "data/output" + dailyDataList.get(0).dateFiled() + ".csv"; //TODO: fix temporary solution
                for (DailyData dailyData : dailyDataList) {
                    String responseData = edgarScraper.downloadData(dailyData);
                    Form4Parser form4Parser = new Form4Parser(dailyData.folderPath(), responseData);
                    form4Parser.parseForm();
                    FormConverter outputter = form4Parser.configureOutputter(outputPath, outputType);
                    outputter.outputForm();
                    nOForms++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void executeConcurrently(String path) {
    }
}
