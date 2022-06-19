import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    /**
     *
     * PROGRAM ARGS:
     *  1. Sequential or concurrent execution, default = false: "-conc=true"
     *  2. programArgYear, default = current year: eg: "-year=2020"
     *  3. listToCurrentYear, default = false: "-to_current=true"
     *  4. include statistical evaluation, default=false: "-stats=true"
     *  5. onwards for singular file(s) from url - baseUrl
     *
     * @param args
     */

    public static void main(String[] args) {
        //xml data from 2004 onwards
        //BASE PATH = https://www.sec.gov/Archives/

        //get program args

        boolean executeConcurrently = false;
        int programArgYear = Calendar.getInstance().get(Calendar.YEAR);
        boolean listToCurrentYear = false;
        ArrayList<String> singleForms = new ArrayList<>();
        boolean onlyParseSingleForm = false;
        boolean doStats = false;
        String year = "";

        if (args.length == 1) {
            executeConcurrently = args[0].equals("-conc=true") || args[0].equals("-conc=1");
        }
        if (args.length == 2) {
            executeConcurrently = args[0].equals("-conc=true") || args[0].equals("-conc=1");
            try {
                programArgYear = Integer.parseInt(args[1].split("-year=")[1]);
            }
            catch (NumberFormatException e) {
                year = args[1].split("-year=")[1];
                listToCurrentYear = false;
            }
        } if (args.length == 3) {
            executeConcurrently = args[0].equals("-conc=true") || args[0].equals("-conc=1");
            listToCurrentYear = args[2].equals("-to_current=true") || args[2].equals("-to_current=1");
            try {
                programArgYear = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                year = args[1].split("-year=")[1];
                listToCurrentYear = false;
                System.out.println("Option listToCurrentYear not possible when no int is provided");
            }
        }
        if (args.length == 4) {
            executeConcurrently = args[0].equals("-conc=true") || args[0].equals("-conc=1");
            listToCurrentYear = args[2].equals("-to_current=true") || args[2].equals("-to_current=1");
            try {
                programArgYear = Integer.parseInt(args[1].split("-year=")[1]);
            }
            catch (NumberFormatException e) {
                year = args[1].split("-year=")[1];
                listToCurrentYear = false;
                System.out.println("Option listToCurrentYear not possible when no int is provided");
            }
            doStats = args[3].equals("-stats=true") || args[3].equals("-stats=1");
        } if (args.length >= 5) {
            executeConcurrently = args[0].equals("-conc=true") || args[0].equals("-conc=1");
            for (int i = 4; i < args.length; i++) {
                singleForms.add(args[i]);
            }
            onlyParseSingleForm = true;
        }

        double startTime = System.nanoTime();
        System.out.println("----------------------------");
        System.out.println("Starting application with program args: ");
        System.out.println("executeConcurrently: " + executeConcurrently);
        System.out.println("programArgYear: " + programArgYear);
        System.out.println("listToCurrentYear: " + listToCurrentYear);
        System.out.println("onlyParseSingleForm: " + onlyParseSingleForm);
        System.out.println("singleForms: " + singleForms.toString());
        System.out.println("doStats: " + doStats);
        System.out.println();
        if (doStats) {
            System.out.println("Starting application at time: " + startTime);
        }
        System.out.println("----------------------------");
        if (onlyParseSingleForm) {
            parseSingleForm(singleForms);
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
    }

    public static void execute(boolean listToCurrentYear, int programArgYear, String year, boolean executeConcurrently) {
        int endYear = listToCurrentYear ? Calendar.getInstance().get(Calendar.YEAR) : programArgYear;

        for (; programArgYear <= endYear; programArgYear++) {
            if (year.equals("")) {
                year = String.valueOf(programArgYear) + "/";
            }

            if (executeConcurrently) {
                int availableCores = Runtime.getRuntime().availableProcessors();

                System.out.println("-------------------------------------------------");
                System.out.println("Mining data for year: " + year + " concurrently with " + availableCores + " available cores.");
                System.out.println("-------------------------------------------------");

                ExecutorService executorService = Executors.newFixedThreadPool(availableCores);

                for (int i = 0; i < availableCores; i++) {
                    Runnable task = new ConcurrentThread(year);
                    executorService.submit(task);
                }

                executorService.shutdown();

                /*
                FTP ftp = new FTP();
                HashMap<String, ArrayList<DailyData>> hashMap = ftp.getDataHashMap(year);

                FTP.invokeConcurrentParsingClass(hashMap, year);

                 */

            } else {

                System.out.println("-------------------------------------------------");
                System.out.println("Mining data for year: " + year + " sequentially.");
                System.out.println("-------------------------------------------------");

                FTP ftp = new FTP();
                HashMap<String, ArrayList<DailyData>> hashMap = ftp.getDataHashMap(year);
                FTP.invokeParsingClass(hashMap, year);
            }
        }
    }


    public static void parseSingleForm(ArrayList<String> urls) {
        FTP ftp = new FTP();
        String dirPath = "singleFilesCSV";

        HashMap<String, ArrayList<DailyData>>  hashMap = new HashMap<>();
        ArrayList<DailyData> arrayList = new ArrayList();
        for (String url : urls) {
            arrayList.add(new DailyData("testurl","4", "testcompany", "testcik", "testDate", url));
        }

        hashMap.put("4", arrayList);

        Parser parser = new Form4Parser(hashMap.get("4"));

        if (parser != null) {
            parser.iterateDailyData(dirPath);
        }
    }
}
