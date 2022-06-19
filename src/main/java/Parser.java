import java.util.ArrayList;

public class Parser {
    ArrayList<DailyData> dailyDataList;
    public Parser(ArrayList<DailyData> dailyDataList) {
        if (dailyDataList == null) throw new IllegalArgumentException();
        this.dailyDataList = dailyDataList;
    }

    public void iterateDailyData(String dirPath) {}
    public void iterateDailyDataConcurrently(String dirPath) {}
}
