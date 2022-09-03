import csv.CSVBuilder;
import csv.CSVTableBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * PROGRAM ARGS:
 * only one program argument => singular file starting from data/....txt
 * 1. Sequential or concurrent execution, default = false: "-conc=true"
 * 2. programArgYear, default = current year: eg: "-year=daily-index/2020/"
 * 4. include statistical evaluation, default=false: "-stats=true"
 */
public class Main {

    public static void main(String[] args) {
        //xml data from 2004 onwards
        //BASE PATH = https://www.sec.gov/Archives/

        //get program args

        boolean executeConcurrently = false;
        String year = Constants.DEFAULT_YEAR;
        String singleForm = "";
        boolean onlyParseSingleForm = false;
        boolean doStats = false;

        if (args.length == 1) {
            singleForm = args[0];
            onlyParseSingleForm = true;
        }
        if (args.length == 2) {
            executeConcurrently = args[0].equals("-conc=true") || args[0].equals("-conc=1");
            year = args[1].split("-year=")[1];
        }
        if (args.length == 3) {
            executeConcurrently = args[0].equals("-conc=true") || args[0].equals("-conc=1");
            year = args[1].split("-year=")[1];
            doStats = args[2].equals("-stats=true") || args[2].equals("-stats=1");
        }

        long startTime = System.nanoTime();
        System.out.println("----------------------------");
        System.out.println("Starting application with program args: ");
        System.out.println("executeConcurrently: " + executeConcurrently);
        System.out.println("onlyParseSingleForm: " + onlyParseSingleForm);
        System.out.println("singleForm: " + singleForm.toString());
        System.out.println("doStats: " + doStats);
        System.out.println();
        if (doStats) {
            System.out.println("Starting application at time: " + startTime);
        }
        System.out.println("----------------------------");

        execute((onlyParseSingleForm) ? singleForm : year, executeConcurrently);
        long endTime = System.nanoTime();
        System.out.println("----------------------------");
        System.out.println("Application ended");
        if (doStats) {
            System.out.println("Application ended at time: " + endTime);
            System.out.println("Application took: " + (endTime - startTime) + " nanoseconds!");
        }
        System.out.println("----------------------------");
/*
        if (onlyParseSingleForm) {
            parseSingleForm(singleForm);
        } else {
            execute(listToCurrentYear, programArgYear, year, executeConcurrently);
            double endTime = System.nanoTime();
            System.out.println("----------------------------");
            System.out.println("Application ended");
            if (doStats) {
                System.out.println("Application ended at time: " + endTime);
                System.out.println("Application took: " + (endTime - startTime) + " nanoseconds!");
            }
            System.out.println("----------------------------");
        }
*/
    }

    public static void execute(String path, boolean executeConcurrently) {

        System.out.println("-------------------------------------------------");
        System.out.println("Mining data for path: " + path + " sequentially.");
        System.out.println("-------------------------------------------------");

        EdgarParser eParser = new EdgarForm4Parser();
        //TODO: maybe use concurrent datasource for this, put the getDailyDataList into Monolithic core that starts it while other workers start working
        List<String> idxFilesList = new CopyOnWriteArrayList<>();

        Form4Parser parser = new Form4Parser(null);

        CSVBuilder csvTableBuilder = new CSVTableBuilder(";", List.of(Constants.CSV_TAG_NAMES_REP), List.of(Constants.CSV_TAGS_REP), List.of(Constants.CSV_TAG_NAMES_TABLE), List.of(Constants.CSV_TAGS_TABLE), "nonDerivativeTable");

//            Thread downloadIdxFilesThread = new Thread(() -> eParser.getIdxFiles(path, idxFilesList));
        eParser.getIdxFiles(path, idxFilesList);

        List<DailyDataRec> ddList = new CopyOnWriteArrayList<>();

        while (!idxFilesList.isEmpty()) {
            try {
                eParser.processIdxFile(idxFilesList.remove(0), ddList); //TODO: multithreading here
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<String> data = new CopyOnWriteArrayList<>();
        while (!ddList.isEmpty() && data.size() < 100) {
            try {
                eParser.downloadData(ddList.remove(0), data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        while (!data.isEmpty()) {
            parser.parseForm4String(data.remove(0), csvTableBuilder);
        }

        String output = csvTableBuilder.outputCsv("");


//            List<DailyDataRec> ddList = eParser.getDailyDataList(path);

//            List<String> data = new CopyOnWriteArrayList<>();

//            downloadData(ddList, data);

        System.out.println("ddList" + ddList.toString());


    }


    public static void parseTestForm(String url) {
        FTP ftp = new FTP();
        String dirPath = "singleFilesCSV";

        HashMap<String, ArrayList<DailyData>> hashMap = new HashMap<>();
        ArrayList<DailyData> arrayList = new ArrayList<>();
        arrayList.add(new DailyData("testurl", "4", "testcompany", "testcik", "testDate", url));

        hashMap.put("4", arrayList);

        Parser parser = new Form4Parser(hashMap.get("4"));

        if (parser != null) {
            parser.iterateDailyData(dirPath);
        }
    }
}
