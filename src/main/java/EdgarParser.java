import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class EdgarParser {
    public static String BASE_URL = "https://www.sec.gov/Archives/edgar/";
    private int loadedIdxFiles = 0;
    private int failedIndexFiles = 0;
    private int failedIdxFiles = 0;
    public String FORM_TYPE;

    public List<DailyData> getDailyDataList(String path) {
        ArrayList<DailyData> ddList = new ArrayList<>();
        try {
            getIdxFileRecursively(path, ddList);
            System.out.println("---------------------------------------------------");
            System.out.println("Parsed a total of " + loadedIdxFiles + " .idx files");
            System.out.println("Failed nO index json files: " + failedIndexFiles);
            System.out.println("Failed nO .idx files: " + failedIdxFiles);
            System.out.println("---------------------------------------------------");
        } catch (Exception e) {
            System.out.println("Exception: %s".format(e.getMessage()));
        }
        return ddList;
    }

    private void getIdxFileRecursively(String path, List<DailyData> ddList){
        try {
//            List valid = Arrays.asList(path.split("/")[path.split("/").length - 1].split("\\."));
            String fileExt = getFileExtension(path);
            if (fileExt == null
                || fileExt.equals("xml"))
                return;
            if (fileExt.equals("idx")) {

                String[] urlSplit = path.split("/");
                urlSplit = Arrays.copyOfRange(urlSplit, urlSplit.length - 3, urlSplit.length);
                String idxType = urlSplit[urlSplit.length - 1].split("\\.")[0];

                if (idxType.equals("form")) {
                    String requestData = loadUrl(path);
                    this.loadedIdxFiles++;
                    System.out.println(" parsed .idx file number " + loadedIdxFiles);
                    if (requestData == null) {
                        failedIdxFiles++;
                        throw new Exception(".idx file not available");
                    }
                    parseIndexFile(requestData, ddList, urlSplit[urlSplit.length - 1]);
                }
            } else {
                String indexData = loadUrl(path + "index.json");
                if (indexData == null) {
                    failedIndexFiles++;
                    throw new Exception("index json not available");
                }
                JsonObject indexJson = getJsonObjectFromString(indexData);
                JsonArray itemArray = indexJson.get("directory").getAsJsonObject().get("item").getAsJsonArray();

                for (JsonElement item : itemArray) {
//                    if (path.equals("edgar/daily-index/"))
//                    int year = Integer.parseInt(path.substring(path.indexOf("daily-index") + 13));
//                    if (year > 2010) {
                    JsonObject itemObj = item.getAsJsonObject();
                    String href = itemObj.get("href").getAsString();
                    getIdxFileRecursively(path + href, ddList);
//                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("skipped path: " + path);
        }
    }

    private String getFileExtension(String s) {
        int lastIndexOf = s.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return null; // empty extension
        }
        return s.substring(lastIndexOf);
    }

    public static String loadUrl(String url) throws IOException, InterruptedException {
        //wait to not exceed 10 requests per second
        Thread.sleep(100);

        url = BASE_URL + url;
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

    private void parseIndexFile(String requestString, List<DailyData> ddList, String fileName) throws IOException {
//        String splitString = requestString.substring(requestString.indexOf("Form Type", requestString.indexOf("Form Type") + 1));
        String splitString = requestString.substring(requestString.lastIndexOf("---") + 4);
        splitString.lines()
//                .skip(10)
                .forEach(line -> {
                    try {
                        DailyData dailyData = new DailyData(fileName, line.trim().split("\\s{2,}"));
                        if (dailyData.getFormType().equals("4")) {
//                            if (!ddList.containsKey(dailyData.getFormType())) {
//                                ArrayList<DailyData> dailyDataList = new ArrayList<>();
//                                ddList.put(dailyData.getFormType(), dailyDataList);
//                            }
//                            ddList.get(dailyData.getFormType()).add(dailyData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
    }

    public static JsonObject getJsonObjectFromString(String string) {
        return new Gson().fromJson(string, JsonObject.class);
    }



}
