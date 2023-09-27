import Form4Parser.CSVForm4Parser;
import Form4Parser.ConstructorWith2Args;
import db.AppConfig;
import db.PSQLForm4Parser;
import interfaces.FormOutputter;
import interfaces.FormParser;
import org.apache.commons.lang3.time.StopWatch;
import statistics.Stats;
import util.*;

import java.io.*;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

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
    public static final int PAUSE_MS = 100;
    public static Stats stats;
    public static BigInteger startTime;
    public static double totalTimeTaken; //total time taken in seconds
    public static long nOForms = 0;
    public static List<String> failedForms = new ArrayList<>();

    private static CountDownLatch stopParsingLatch = new CountDownLatch(1);

    public static <T extends FormParser & FormOutputter> void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        //xml data from 2004 onwards
        //BASE PATH = https://www.sec.gov/Archives/
        Map<String, Object> argsMap = parseProgramArgs(args);
        //get program args
        String path = (String) argsMap.get("path");
        boolean conc = (boolean) argsMap.get("conc");
        boolean doStats = (boolean) argsMap.get("doStats");
        String output = (String) argsMap.get("output");

        int maxNoForms = (int) argsMap.get("n");

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
                executeFilesSequentially(files); //TODO: add program arg
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

        switch (output) {
            case "csv":
                if (conc)
                    executeConcurrently(path, CSVForm4Parser::new, maxNoForms);
                else
                    executeSequentially(path, CSVForm4Parser::new, 100, maxNoForms);
                break;
            case "db":
                if (conc)
                    executeConcurrently(path, PSQLForm4Parser::new, maxNoForms);
                else
                    executeSequentially(path, PSQLForm4Parser::new, 100, maxNoForms);
                break;
//                    runWithDb(path, conc, doStats);
            default:
                return;
            }
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

    private static void runWithDb(String path, boolean conc, boolean doStats) {
        String dbUrl = AppConfig.getDbUrl();
        String dbUsername = AppConfig.getDbUsername();
        String dbPassword = AppConfig.getDbPassword();

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            System.out.println("Connected to PostgreSQL!");
        } catch (SQLException e) {
            System.err.println("Connection failed!");
            e.printStackTrace();
        }

    }

    private static Map<String, Object> parseProgramArgs(String[] args) {
        Map<String, Object> result = new HashMap<>();
        //Default values
        result.put("path", Constants.DEFAULT_YEAR);
        result.put("conc", false);
        result.put("doStats", false);
        List<String> as = new LinkedList<String>(Arrays.asList(args)); //LinkedList supports faster remove than ArrayList
        String next = null;
        String next1 = null;
        String next2 = null;
        for (int i = 0; !as.isEmpty(); i++) {
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
            } else if (next1.equals("-output")) {
                result.put("output", next2);
            } else if (next1.equals("-n")) {
                result.put("n", Integer.parseInt(next2));
            }
        }
        return result;
    }

    private static boolean cmdBool(String bool) {
        return Boolean.parseBoolean(bool) || "1".equals(bool);
    }


    public static <T extends FormParser & FormOutputter> void executeSequentially(String path, ConstructorWith2Args<T, String, String> parserConstructorSupplier, int delay, int maxNoForms) {
//        stats = new Stats();
        EdgarScraper edgarScraper = new EdgarScraper("4");
        //wait until all idx files are downloaded (TODO problem: recursion)
        edgarScraper.scrapeIndexFiles(path, edgarScraper.getIndexFiles(), delay);
        for (IndexFile idxFile : edgarScraper.getIndexFiles()) {
            try {
                List<DailyData> dailyDataList = edgarScraper.parseIndexFile(idxFile);
                for (DailyData dailyData : dailyDataList) {
                    try {
                        if (nOForms >= maxNoForms) {
                            break;
                        }
                        String responseData = edgarScraper.downloadData(dailyData, delay);
                        T formParser = parserConstructorSupplier.create(dailyData.folderPath(), responseData);
                        formParser.parseForm();
                        formParser.outputForm(dailyData.outputPath());
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
    }


    public static <T extends FormParser & FormOutputter> void executeConcurrently(String path, ConstructorWith2Args<T, String, String> parserConstructorSupplier, int maxNoForms) throws InterruptedException, ExecutionException {
        EdgarScraper edgarScraper = new EdgarScraper("4");
        List<Runnable> downloadQueue = new CopyOnWriteArrayList<>(); //TODO: maybe normal ArrayList suffices
        ObservableCopyOnWriteArrayList<IndexFile> indexFiles = new ObservableCopyOnWriteArrayList<>();
        ObservableCopyOnWriteArrayList<ParsableForm> parsableForms = new ObservableCopyOnWriteArrayList<>();
        int nThreads = Runtime.getRuntime().availableProcessors();

        ExecutorService threadPool = Executors.newFixedThreadPool(nThreads - 1); //reserve 1 thread for downloading
        ExecutorService downloadExecutorService = Executors.newSingleThreadExecutor();
        System.out.println("Starting concurrent program with " + nThreads + " threads");

        parsableForms.addListener((parsableForm, list, index) -> {
            threadPool.submit(() -> {
                try {
                    if (stopParsingLatch.getCount() > 0) {
                        T formParser = parserConstructorSupplier.create(parsableForm.folderPath(), parsableForm.responseData());
                        formParser.parseForm();
                        formParser.outputForm(parsableForm.outputPath());
                        nOForms++;
                        if (nOForms >= maxNoForms) {
                            // Signal the latch to stop parsing
                            stopParsingLatch.countDown();
                        }
                    }
                } catch (ParseFormException | OutputException e) {
                    e.printStackTrace();
                    failedForms.add(parsableForm.folderPath());
                }
            });
            list.remove(index); //remove element after use
        });

        indexFiles.addListener((idxFile, list, index) -> {
            if (stopParsingLatch.getCount() > 0) {

                List<DailyData> dailyDataList = edgarScraper.parseIndexFile(idxFile); //TODO: maybe do this in threadpool
                for (DailyData dailyData : dailyDataList) {
                    //add dailydata downloads to beginning, so they can start being processed
                    downloadQueue.add(0, () -> {
                        try {
                            if (stopParsingLatch.getCount() > 0) {

                                String responseData = edgarScraper.downloadData(dailyData, 0);
                                System.out.println("Loaded form: " + dailyData.folderPath());
                                parsableForms.add(new ParsableForm(responseData, dailyData.folderPath(), dailyData.outputPath()));
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                            failedForms.add(dailyData.folderPath());
                        }
                    });
                }
                list.remove(index); //remove element after use
            }
        });

        edgarScraper.scrapeIndexFilesConc(path, downloadQueue, indexFiles);

        //TODO: think of scheduling algorithm for this
        downloadExecutorService.submit(() -> {
            while (!downloadQueue.isEmpty() && stopParsingLatch.getCount() > 0) {
                downloadQueue.remove(0).run();
                try {
                    Thread.sleep(PAUSE_MS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).get();
        downloadExecutorService.shutdown();
        threadPool.shutdown();
    }

    public static void executeFilesSequentially(String[] files) {
        EdgarScraper edgarScraper = new EdgarScraper("4");
        for (String file : files) {
            String outputPath = "data/output_" + file.replace("/", "_") + ".csv"; //TODO: fix temporary solution
            DailyData dailyData = new DailyData("4", "", "", "", file, outputPath);
            try {
                String responseData = edgarScraper.downloadData(dailyData, 100);
                CSVForm4Parser form4Parser = new CSVForm4Parser(dailyData.folderPath(), responseData);
                form4Parser.parseForm();
                form4Parser.outputForm(outputPath);
                nOForms++;
            } catch (ParseFormException | IOException | InterruptedException | OutputException e) {
                e.printStackTrace();
                failedForms.add(dailyData.folderPath());
            }
        }
    }
}
