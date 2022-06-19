import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsClass {

    public static void main(String[] args) {
        String edgarDirPath = "2021/QTR1/form.20210104.idx";

        long[] sequentialTime = new long[2];
        long[] concurrentTime = new long[2];

        for (int i = 0; i < sequentialTime.length; i++) {
//            sequentialTime[i] = testSequentialCode(edgarDirPath);
            concurrentTime[i] = testConcurrentCode(edgarDirPath);
        }

        System.out.println("\n--------------------------------------");
        System.out.println("Test finished: \n");
        System.out.println("Sequential times : " + Arrays.toString(sequentialTime));
        System.out.println("Concurrent times : " + Arrays.toString(concurrentTime));
        System.out.println("Average sequential time: " + getAverage(sequentialTime));
        System.out.println("Average concurrent time: " + getAverage(concurrentTime));
        System.out.println("Variance sequential time: " + getStandardDeviation(sequentialTime));
        System.out.println("Variance concurrent time: " + getStandardDeviation(concurrentTime));

    }

    public static long testSequentialCode(String edgarDirPath) {
        long startTime = System.nanoTime();

        FTP ftp = new FTP();
        HashMap<String, ArrayList<DailyData>> hashMap = ftp.getDataHashMap(edgarDirPath);

        FTP.invokeParsingClass(hashMap, edgarDirPath);

        long endTime = System.nanoTime();

        return endTime - startTime;
    }
    public static long testConcurrentCode(String edgarDirPath) {
        long startTime = System.nanoTime();

        int availableCores = Runtime.getRuntime().availableProcessors();

        System.out.println("-------------------------------------------------");
        System.out.println("Mining data for year: " + edgarDirPath + "with " + availableCores + "available cores" );
        System.out.println("-------------------------------------------------");

        FTP ftp = new FTP();
        HashMap<String, ArrayList<DailyData>> hashMap = ftp.getDataHashMap(edgarDirPath);

        FTP.invokeConcurrentParsingClass(hashMap, edgarDirPath);

        long endTime = System.nanoTime();

        return endTime - startTime;
    }

    public static double getAverage(long[] values) {
        double sum = Arrays.stream(values).asDoubleStream()
                .reduce(0, Double::sum);
        return sum/(values.length);
    }

    public static double getStandardDeviation(long[] values) {
        double mean = getAverage(values);
        double squaredDifference = Arrays.stream(values).asDoubleStream()
                .map(v -> Math.pow((v - mean), 2))
                .reduce(0, Double::sum);
        return Math.sqrt(squaredDifference/(values.length - 1));
    }

}
