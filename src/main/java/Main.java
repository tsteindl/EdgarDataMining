import Form4Parser.CSVForm4Parser;
import db.AppConfig;
import db.PSQLForm4Parser;
import interfaces.FormOutputter;
import interfaces.FormParser;
import statistics.Stats;
import util.*;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Tobias Steindl tobias.steindl@gmx.net
 * @version 1.0
 * PROGRAM ARGS:
 * 1. path in EDGAR file system, suffix with "/" if there are subdirectories: -path=...
 * 2. multithreading: -conc=true/false
 * example:
 */
public class Main {
    public static final int DELAY = 100;
    public static Stats stats;
    public static BigInteger startTime;
    public static double totalTimeTaken; //total time taken in seconds
    public static long nOForms = 0;
    public static List<String> failedForms = new ArrayList<>();

    private static final CountDownLatch stopParsingLatch = new CountDownLatch(1);

    public static <T extends FormParser & FormOutputter> void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Map<String, Object> argsMap = parseProgramArgs(args);
        String path = (String) argsMap.get("path");
        boolean conc = (boolean) argsMap.get("conc");
        boolean doStats = (boolean) argsMap.get("doStats");
        String output = (String) argsMap.get("output");
        int maxNoForms = -1;
        if (argsMap.get("n") != null)
            maxNoForms = (int) argsMap.get("n");

        String dbUrl = AppConfig.getDbUrl();
        String dbUsername = AppConfig.getDbUsername();
        String dbPassword = AppConfig.getDbPassword();

        if (output == null) {
            output = "csv";
        }

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            conn.setAutoCommit(true); //automatically commit transactions
            System.out.println("Connected to PostgreSQL!");

            if (output.equals("db")) {
                //Create tables
                String createIssuerTable = "CREATE TABLE IF NOT EXISTS issuer (\n" +
                                                "cik INTEGER PRIMARY KEY," +
                                                "issuerName VARCHAR(255)," +
                                                "issuerTradingSymbol VARCHAR(255)" +
                                            ");";

                String createReportingOwnerTable = "CREATE TABLE IF NOT EXISTS reporting_owner (\n" +
                                                        "cik INTEGER PRIMARY KEY,\n" +
                                                        "ccc VARCHAR(255),\n" +
                                                        "name VARCHAR(255),\n" +
                                                        "street1 VARCHAR(255),\n" +
                                                        "street2 VARCHAR(255),\n" +
                                                        "city VARCHAR(255),\n" +
                                                        "state VARCHAR(255),\n" +
                                                        "zipCode VARCHAR(255),\n" +
                                                        "isDirector BOOLEAN,\n" +
                                                        "isOfficer BOOLEAN,\n" +
                                                        "isTenPercentOwner BOOLEAN,\n" +
                                                        "isOther BOOLEAN,\n" +
                                                        "officerTitle VARCHAR(255),\n" +
                                                        "otherText VARCHAR(255)\n" +
                                                    ");";
                //schema with nested tables instead of relations, as transactions/holdings will mostly be accessed with form
                String createForm4Table = "CREATE TABLE IF NOT EXISTS form_4 (\n" +
                                                "id SERIAL PRIMARY KEY,\n" +
                                                "name VARCHAR(255) UNIQUE,\n" +
                                                "documentType VARCHAR(255),\n" +
                                                "periodOfReport DATE,\n" +
                                                "notSubjectToSection16 BOOLEAN,\n" +
                                                "issuer_cik INTEGER REFERENCES issuer ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                                "nonDerivativeTransactions JSON,\n" +
                                                "nonDerivativeHoldings JSON,\n" +
                                                "derivativeTransactions JSON,\n" +
                                                "derivativeHoldings JSON\n" +
                                            ");";

                String createReportingOwner_Form4Table = "CREATE TABLE IF NOT EXISTS reporting_owner_form_4 (\n" +
                                                            "owner_cik INTEGER REFERENCES reporting_owner (cik) ON UPDATE CASCADE ON DELETE CASCADE,\n" +
                                                            "form_4_id INTEGER REFERENCES form_4 (id) ON UPDATE CASCADE ON DELETE CASCADE, " +
                                                            "CONSTRAINT reporting_owner_form_4_pkey PRIMARY KEY (owner_cik, form_4_id)" +
                                                          ");";
                Statement stmt = conn.createStatement();
                stmt.addBatch(createIssuerTable);
                stmt.addBatch(createReportingOwnerTable);
                stmt.addBatch(createForm4Table);
                stmt.addBatch(createReportingOwner_Form4Table);
                stmt.executeBatch();
//                conn.commit();
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

            if (argsMap.get("files") != null) { //TODO: calculate delay
                executeFiles(((String) argsMap.get("files")).split(","), DELAY, output, conn);
            }
            if (conc)
                executeConcurrently(path, output, maxNoForms, conn);
            else
                executeSequentially(path, output, DELAY, maxNoForms, conn);

            totalTimeTaken = Stats.nsToSec(new BigInteger(Long.toString(System.nanoTime())).subtract(startTime));
            System.out.println("-------------------------------------------------");
            System.out.println("Application ended");
            System.out.println("Total time taken: " + totalTimeTaken + "s");
            System.out.println("Number of Forms parsed: " + nOForms);
            System.out.println("Avg seconds per form: " + ((double) totalTimeTaken / nOForms));
            System.out.println("Number of erroneous forms: " + failedForms.size());
            System.out.println("Erroneous forms: " + failedForms.toString());
            System.out.println("-------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Connection failed!");
            e.printStackTrace();
        }
    }

    private static <T extends FormParser & FormOutputter> void executeFiles(String[] files, int delay, String output, Connection connection) throws IOException, InterruptedException {
        EdgarScraper edgarScraper = new EdgarScraper("4");
        for (String file : files) {
            String outputPath = "data/output_" + file.replace("/", "_") + ".csv";
            DailyData dailyData = new DailyData("4", "", "", "", file, outputPath);
            try {
                if (nOForms >= 0) {
                    continue;
                }
                handleDailyData(delay, dailyData, edgarScraper, output, connection);
                nOForms++;
            } catch (ParseFormException | OutputException e) {
                e.printStackTrace();
                failedForms.add(((DailyData) null).folderPath());
            }
        }
    }

    private static Map<String, Object> parseProgramArgs(String[] args) {
        Map<String, Object> result = new HashMap<>();
        //Default values
        result.put("path", Constants.DEFAULT_YEAR);
        result.put("conc", false);
        result.put("doStats", false);
        List<String> as = new LinkedList<>(Arrays.asList(args)); //LinkedList supports faster remove than ArrayList
        String next;
        String next1;
        String next2 = null;
        while (!as.isEmpty()) {
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


    public static <T extends FormParser & FormOutputter> void executeSequentially(String path, String output, int delay, int maxNoForms, Connection connection) {
//        stats = new Stats();
        EdgarScraper edgarScraper = new EdgarScraper("4");
        //wait until all idx files are downloaded (TODO problem: recursion)
        edgarScraper.scrapeIndexFiles(path, edgarScraper.getIndexFiles(), delay);
        for (IndexFile idxFile : edgarScraper.getIndexFiles()) {
            try {
                List<DailyData> dailyDataList = edgarScraper.parseIndexFile(idxFile);
                for (DailyData dailyData : dailyDataList) {
                    try {
                        if (maxNoForms != -1 && nOForms >= maxNoForms) {
                            break;
                        }
                        handleDailyData(delay, dailyData, edgarScraper, output, connection);
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

    private static <T extends FormParser & FormOutputter> void handleDailyData(int delay, DailyData dailyData, EdgarScraper edgarScraper, String output, Connection connection) throws IOException, InterruptedException, ParseFormException, OutputException {
        String responseData = edgarScraper.downloadData(dailyData, delay);
        T formParser;
        switch (output) {
            case "db":
                formParser = (T) new PSQLForm4Parser(dailyData.folderPath(), responseData, connection);
                break;
            default: //case csv
                formParser = (T) new CSVForm4Parser(dailyData.folderPath(), responseData);
        }
        formParser.parseForm();
        formParser.outputForm(dailyData.outputPath());
    }


    public static <T extends FormParser & FormOutputter> void executeConcurrently(String path, String output, int maxNoForms, Connection connection) throws InterruptedException, ExecutionException {
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
                        T formParser;
                        switch (output) {
                            case "db":
                                formParser = (T) new PSQLForm4Parser(parsableForm.folderPath(), parsableForm.responseData(), connection);
                                break;
                            default: //case csv
                                formParser = (T) new CSVForm4Parser(parsableForm.folderPath(), parsableForm.responseData());
                        }
                        formParser.parseForm();
                        formParser.outputForm(parsableForm.outputPath());
                        nOForms++;
                        if (maxNoForms != -1 && nOForms >= maxNoForms) {
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
                long beforeRequest = System.nanoTime();
                downloadQueue.remove(0).run();
                long afterRequest = System.nanoTime();
                long alreadyWaited = (afterRequest - beforeRequest) / 1000000;
                System.out.println("Waiting for " + (alreadyWaited < DELAY ? DELAY - alreadyWaited : 0) + " ms");
                try {
                    Thread.sleep(alreadyWaited < DELAY ? DELAY - alreadyWaited : 0); //TODO: reconsider if this is safe
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).get();
        downloadExecutorService.shutdown();
        threadPool.shutdown();
    }

}
