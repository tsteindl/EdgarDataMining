import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
//        int endYear = listToCurrentYear ? Calendar.getInstance().get(Calendar.YEAR) : path;

//        for (; path <= endYear; path++) {
//            if (path.equals("")) {
//                path = String.valueOf(path) + "/";
//            }

        if (executeConcurrently) {
            int availableCores = Runtime.getRuntime().availableProcessors();

            System.out.println("-------------------------------------------------");
            System.out.println("Mining data for path: " + path + " concurrently with " + availableCores + " available cores.");
            System.out.println("-------------------------------------------------");

            ExecutorService executorService = Executors.newFixedThreadPool(availableCores);

            for (int i = 0; i < availableCores; i++) {
                Runnable task = new ConcurrentThread(path);
                executorService.submit(task);
            }

            executorService.shutdown();


        } else {

            System.out.println("-------------------------------------------------");
            System.out.println("Mining data for path: " + path + " sequentially.");
            System.out.println("-------------------------------------------------");

/*
            FTP ftp = new FTP();
            HashMap<String, ArrayList<DailyData>> hashMap = ftp.getDataHashMap(path);
            FTP.invokeParsingClass(hashMap, path);
*/
            EdgarParser eParser = new EdgarForm4Parser();
            List<DailyData> ddList = eParser.getDailyDataList(path);


        }
//        }
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
