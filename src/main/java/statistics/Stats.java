package statistics;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

public class Stats {

    private final Map<String, List<Long>> timeMap = new HashMap<>();

    public void execute(Callable fn, String fnName) throws Exception {
        Long startTime = System.nanoTime();
        fn.call();
        Long endTime = System.nanoTime();
        Long time = endTime - startTime;
        if (timeMap.containsKey(fnName))
            timeMap.get(fnName).add(time);
        else
            timeMap.put(fnName, new ArrayList<Long>(Arrays.asList(time)));
    }

    private Long total(List<Long> entries) {
        return entries.stream().reduce(0L, Long::sum);
    }
    private double avg(List<Long> entries) {
        return (double) total(entries) / entries.size();
    }
    private double nsToSec(Long ms) {
        return (double) ms/1000000;
    }
    private double nsToSec(double ms) {
        return ms/1000000;
    }
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("-----------------------------------------------\n");
        builder.append("Printing statistics for corresponding methods:\n");
        builder.append("-----------------------------------------------\n");
        for (String method : timeMap.keySet()) {
            builder.append(String.format("Method: %s\n", method));
            builder.append(String.format("Total: %.2fs\n", nsToSec(total(timeMap.get(method)))));
            builder.append(String.format("Average: %.2fs\n", nsToSec(avg(timeMap.get(method)))));
            builder.append("\n");
        }
        builder.append("-----------------------------------------------");
        return builder.toString();
    }

    public static void main(String[] args) throws Exception {
        Stats statClass = new Stats();
        statClass.execute(() -> {
           List<Integer> list = new ArrayList<>();
           IntStream.range(1, 10000).forEach(list::add);
           return null;
        }, "addFn");
        statClass.execute(() -> {
            List<Integer> list = new ArrayList<>();
            IntStream.range(50, 10000000).forEach(list::add);
            return null;
        }, "addFn2");
        statClass.execute(() -> {
            List<Integer> list = new ArrayList<>();
            IntStream.range(5, 100).forEach(list::add);
            return null;
        }, "addFn");

        System.out.println(statClass.toString());
    }

}

