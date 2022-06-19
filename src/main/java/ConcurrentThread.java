import java.util.ArrayList;
import java.util.HashMap;

public class ConcurrentThread implements Runnable{

    private FTP ftp;
    private String year;

    public ConcurrentThread(String year) {
        this.year = year;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
        System.out.print(" : " + year);
//        for(int i = 0; i < 10; i++) {
//            System.out.println(Thread.currentThread().getName() + "says " + i);
//        }

        FTP ftp = new FTP();
        HashMap<String, ArrayList<DailyData>> hashMap = ftp.getDataHashMap(year);
        FTP.invokeParsingClass(hashMap, year);

    }
}
