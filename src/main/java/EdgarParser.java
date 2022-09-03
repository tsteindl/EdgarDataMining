import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class EdgarParser {
    public static String BASE_URL = "https://www.sec.gov/Archives/edgar/";
    private int loadedIdxFiles = 0;
    private int failedIndexFiles = 0;
    private int failedIdxFiles = 0;
    public String FORM_TYPE;

    public List<DailyDataRec> getDailyDataList(String path) {
        ArrayList<DailyDataRec> ddList = new ArrayList<>();



        getFileLocListRec(path, ddList);

        try {
//            getIdxFileRecursively(path, ddList);
            System.out.println("---------------------------------------------------");
            System.out.println("Parsed a total of " + loadedIdxFiles + " .idx files");
            System.out.println("Failed nO index json files: " + failedIndexFiles);
            System.out.println("Failed nO .idx files: " + failedIdxFiles);
            System.out.println("---------------------------------------------------");
        } catch (Exception e) {
            System.out.format("Exception: %s", e.getMessage());
        }
        return ddList;
    }

    private void getFileLocListRec(String path, List<DailyDataRec> list) {
        String fileExt = getFileExtension(path);
        switch (fileExt) {
            case (".idx"):
                getDailyDataRec(path, list); break;
            case "":
                traverseDirTreeFurther(path, list); break;
            default:
                return;
        }
    }


    public void getIdxFiles(String path, List<String> list) {
        //TODO: remove this
        if (this.loadedIdxFiles > 1) return;
        String fileExt = getFileExtension(path);
        switch (fileExt) {
            case (".idx"):
                String[] urlSplit = path.split("/");
                urlSplit = Arrays.copyOfRange(urlSplit, urlSplit.length - 3, urlSplit.length);
                String indexType = urlSplit[urlSplit.length - 1].split("\\.")[0];

                if (indexType.equals("form")) {
                    try {
                        String requestData = loadUrl("daily-index/" + path);
                        this.loadedIdxFiles++;
                        System.out.println(" parsed .idx file number " + loadedIdxFiles);
                        if (requestData == null) {
                            failedIdxFiles++;
                            throw new Exception(".idx file not available");
                        }
//                        parseIndexFile(requestData, list, urlSplit[urlSplit.length - 1]);
                        list.add(requestData);
                    } catch (Exception e) {
                        System.out.println(String.format("Url. %s not loadable", path));
                    }
                }
                break;
            case "":
                //traverse tree further
                try {
                    String indexData = loadUrl("daily-index/" + path + "index.json");
                    if (indexData == null) {
                        failedIndexFiles++;
                        throw new Exception("index json not available");
                    }
                    JsonObject indexJson = getJsonObjectFromString(indexData);
                    JsonArray itemArray = indexJson.get("directory").getAsJsonObject().get("item").getAsJsonArray();

                    for (JsonElement item : itemArray) {
                        JsonObject itemObj = item.getAsJsonObject();
                        String href = itemObj.get("href").getAsString();
                        getIdxFiles(path + href, list);
                    }
                } catch(Exception e) {
                    System.out.println(String.format("Url. %s not loadable", path));
                }
                break;
            default:
                return;
        }
    }

    private void traverseDirTreeFurther(String path, List<DailyDataRec> list) {
        //traverse tree further
        try {
            String indexData = loadUrl("daily-index/" + path + "index.json");
            if (indexData == null) {
                failedIndexFiles++;
                throw new Exception("index json not available");
            }
            JsonObject indexJson = getJsonObjectFromString(indexData);
            JsonArray itemArray = indexJson.get("directory").getAsJsonObject().get("item").getAsJsonArray();

            for (JsonElement item : itemArray) {
                JsonObject itemObj = item.getAsJsonObject();
                String href = itemObj.get("href").getAsString();
                getFileLocListRec(path + href, list);
            }
        } catch(Exception e) {
            System.out.println(String.format("Url. %s not loadable", path));
        }
    }

    private void getDailyDataRec(String path, List<DailyDataRec> list) {
        String[] urlSplit = path.split("/");
        urlSplit = Arrays.copyOfRange(urlSplit, urlSplit.length - 3, urlSplit.length);
        String indexType = urlSplit[urlSplit.length - 1].split("\\.")[0];

        if (indexType.equals("form")) {
            try {
//                        list.add("data/" + urlSplit[urlSplit.length - 1]);
//                String requestData = loadUrl("data/" + urlSplit[urlSplit.length - 1]);
                String requestData = loadUrl("daily-index/" + path);
                this.loadedIdxFiles++;
                System.out.println(" parsed .idx file number " + loadedIdxFiles);
                if (requestData == null) {
                    failedIdxFiles++;
                    throw new Exception(".idx file not available");
                }
                parseIndexFile(requestData, list, urlSplit[urlSplit.length - 1]);
            } catch (Exception e) {
                System.out.println(String.format("Url. %s not loadable", path));
            }
        }
    }

    private void getIdxFileRecursively(String path, List<DailyDataRec> ddList) {
        try {
//            List valid = Arrays.asList(path.split("/")[path.split("/").length - 1].split("\\."));
            switch (getFileExtension(path)) {
                case (".xml"):
                    return;
                case (".idx"):
                    String[] urlSplit = path.split("/");
                    urlSplit = Arrays.copyOfRange(urlSplit, urlSplit.length - 3, urlSplit.length);
                    String idxType = urlSplit[urlSplit.length - 1].split("\\.")[0];

                    if (idxType.equals("form")) {
                        String requestData = loadUrl("data/" + urlSplit[urlSplit.length - 1]);
                        this.loadedIdxFiles++;
                        System.out.println(" parsed .idx file number " + loadedIdxFiles);
                        if (requestData == null) {
                            failedIdxFiles++;
                            throw new Exception(".idx file not available");
                        }
                        parseIndexFile(requestData, ddList, urlSplit[urlSplit.length - 1]);
                    }
                    break;
                default:
                    //traverse tree further
                    String indexData = loadUrl("daily-index/" + path + "index.json");
                    if (indexData == null) {
                        failedIndexFiles++;
                        throw new Exception("index json not available");
                    }
                    JsonObject indexJson = getJsonObjectFromString(indexData);
                    JsonArray itemArray = indexJson.get("directory").getAsJsonObject().get("item").getAsJsonArray();

                    for (JsonElement item : itemArray) {
                        JsonObject itemObj = item.getAsJsonObject();
                        String href = itemObj.get("href").getAsString();
                        getIdxFileRecursively(path + href, ddList);
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
            return ""; // empty extension
        }
        return s.substring(lastIndexOf);
    }

    public static String loadUrl(String path) throws IOException, InterruptedException {
        //TODO: sleep thread
        //wait to not exceed 10 requests per second
        //TODO: change this (do this in controller)
        Thread.sleep(100);

        String url = BASE_URL + path;
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
        }
        return null;
    }

    private void parseIndexFile(String requestString, List<DailyDataRec> list, String fileName) throws IOException {
        //TODO: Test if thread is fast enough for 10 requests/s otherwhise multithreading
//        String splitString = requestString.substring(requestString.indexOf("Form Type", requestString.indexOf("Form Type") + 1));
        String splitString = requestString.substring(requestString.lastIndexOf("---") + 4);
        splitString.lines()
//                .skip(10)
                .forEach(line -> {
                    try {
                        String[] arr = line.trim().split("\\s{2,}");
                        DailyDataRec dailyData = new DailyDataRec(fileName, arr[0], arr[1], arr[2], arr[3], arr[4]);
                        //TODO: use constants for this
                        if (dailyData.formType().equals("4")) {
//                            if (!list.containsKey(dailyData.getFormType())) {
//                                ArrayList<DailyData> dailyDataList = new ArrayList<>();
//                                list.put(dailyData.getFormType(), dailyDataList);
//                            }
                            list.add(dailyData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
    }

    public static JsonObject getJsonObjectFromString(String string) {
        return new Gson().fromJson(string, JsonObject.class);
    }


    public void processIdxFile(String idxFile, List<DailyDataRec> outputList) throws IOException, InterruptedException {
//        String requestData = loadUrl("daily-index/" + path);
//        this.loadedIdxFiles++;
//        System.out.println(" parsed .idx file number " + loadedIdxFiles);
//        if (requestData == null) {
//            failedIdxFiles++;
//            throw new Exception(".idx file not available");
//        }
        parseIndexFile(idxFile, outputList, "");
    }

    public void downloadData(DailyDataRec dailyDataRec, List<String> outputList) throws IOException, InterruptedException {
        String returnData = FTP.loadUrl(dailyDataRec.folderPath());
        if (returnData == null) return;
        outputList.add(returnData);
    }
}
