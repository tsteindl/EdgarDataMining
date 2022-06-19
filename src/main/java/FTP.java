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

    public HashMap<String, ArrayList<DailyData>> getDataHashMap(String urlApp) {
        String dataPath = path + File.separator + "indices";
        String url = "edgar/daily-index/";
        if (urlApp != null) url += urlApp;

        //TODO: compute dates

        HashMap<String, ArrayList<DailyData>> hashMap = new HashMap<>();

        try {
            getIdxFileRecursively(url, hashMap);
            System.out.println("---------------------------------------------------");
            System.out.println("Parsed a total of " + loadedIdxFiles + " .idx files");
            System.out.println("Failed nO index json files: " + failedIndexFiles);
            System.out.println("Failed nO .idx files: " + failedIdxFiles);
            System.out.println("---------------------------------------------------");
        } catch (Exception e) {
            System.out.println("Exception: %s".format(e.getMessage()));
        }
        return hashMap;
    }

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

    private void getIdxFileRecursively(String url, HashMap<String, ArrayList<DailyData>> hashMap){
        try {
            List valid = Arrays.asList(url.split("/")[url.split("/").length - 1].split("\\."));
            //TODO: include different types of indexes: eg XML
            if (valid.contains("xml")) {
                return;
            }
            if (valid.contains("idx")) {
                //TODO: parallelize this function and create own HashMap for every index file

                String[] urlSplit = url.split("/");
                urlSplit = Arrays.copyOfRange(urlSplit, urlSplit.length - 3, urlSplit.length);
                String idxType = urlSplit[urlSplit.length - 1].split("\\.")[0];

                if (idxType.equals("form")) {
                    String requestData = loadUrl(url);
                    this.loadedIdxFiles++;
                    System.out.println(" parsed .idx file number " + loadedIdxFiles);
                    if (requestData == null) {
                        failedIdxFiles++;
                        throw new Exception(".idx file not available");
                    }
                    parseIndexFile(requestData, hashMap, urlSplit[urlSplit.length - 1]);
                }
            } else {
                String indexData = loadUrl(url + "index.json");
                if (indexData == null) {
                    failedIndexFiles++;
                    throw new Exception("index json not available");
                }
                JsonObject indexJson = getJsonObjectFromString(indexData);
                JsonArray itemArray = indexJson.get("directory").getAsJsonObject().get("item").getAsJsonArray();

                for (JsonElement item : itemArray) {
//                    if (url.equals("edgar/daily-index/"))
//                    int year = Integer.parseInt(url.substring(url.indexOf("daily-index") + 13));
//                    if (year > 2010) {
                    JsonObject itemObj = item.getAsJsonObject();
                    String href = itemObj.get("href").getAsString();
                    getIdxFileRecursively(url + href, hashMap);
//                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("skipped url: " + url);
        }
    }

    private void parseIndexFile(String requestString, HashMap<String, ArrayList<DailyData>> hashMap, String fileName) throws IOException {
//        String splitString = requestString.substring(requestString.indexOf("Form Type", requestString.indexOf("Form Type") + 1));
        String splitString = requestString.substring(requestString.lastIndexOf("---") + 4);
        splitString.lines()
//                .skip(10)
                .forEach(line -> {
                    try {
                        DailyData dailyData = new DailyData(fileName, line.trim().split("\\s{2,}"));
                        if (dailyData.getFormType().equals("4")) {
                            if (!hashMap.containsKey(dailyData.getFormType())) {
                                ArrayList<DailyData> dailyDataList = new ArrayList<>();
                                hashMap.put(dailyData.getFormType(), dailyDataList);
                            }
                            hashMap.get(dailyData.getFormType()).add(dailyData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

        });
    }

    public static void invokeParsingClass(HashMap<String, ArrayList<DailyData>> hashMap, String dirPath) {
        //TODO: add other parsers and iterate over hashmap
        if (hashMap == null) return;
        for (String formType : hashMap.keySet()) {
            Parser parser = null;
            if (formType.equals("4")) {
                parser = new Form4Parser(hashMap.get(formType));
            }
            if (parser != null) {
                parser.iterateDailyData(dirPath);
            }
        }
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



