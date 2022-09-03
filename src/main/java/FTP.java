import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

import com.google.gson.*;


//calculate dates to be calculated
//establish connection to FTP server
//download index files
//generate csvs from index files
//save csvs in directory
public class FTP {

    public static String baseUrl = "https://www.sec.gov/Archives/";
    private static String path = "data";
    private int loadedIdxFiles = 0;
    private int failedIndexFiles = 0;
    private int failedIdxFiles = 0;

    public FTP() {}

    public static String loadUrl(String url) throws IOException, InterruptedException {
        //wait to not exceed 10 requests per second
        Thread.sleep(100);

        url = baseUrl + url;
        System.out.println("load url: " + url);
        URL u = new URL(url);
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("GET");
//                User Agent for Bot request headers:
//                Sample Company Name AdminContact@<sample company domain>.com);
        con.setRequestProperty("User-Agent", "WUTIS tobias.steindl@gmx.net");

        int status = con.getResponseCode();

        if (status == 429) {
            System.out.println("traffic limit exceeded");
            System.out.println("waiting for 10 minutes...");
            TimeUnit.MINUTES.sleep(11);
            status = con.getResponseCode();
        }
        if (status == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
                content.append("\n");
            }
            in.close();

            return content.toString();
        }else {
            return null;
        }
    }

    public static JsonObject getJsonObjectFromString(String string) {
        return new Gson().fromJson(string, JsonObject.class);
    }

    public static void invokeConcurrentParsingClass(HashMap<String, ArrayList<DailyData>> hashMap, String dirPath) {
        if (hashMap == null) return;
        for (String formType : hashMap.keySet()) {
            Parser parser = null;
            if (formType.equals("4")) {
                parser = new Form4Parser(hashMap.get(formType));
            }
            if (parser != null) {
                parser.iterateDailyDataConcurrently(dirPath);
            }
        }
    }
}



