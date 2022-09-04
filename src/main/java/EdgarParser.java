import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import util.DailyData;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class EdgarParser {
    private int loadedIdxFiles = 0;
    private int failedIndexFiles = 0;
    private int failedIdxFiles = 0;
    public String FORM_TYPE;

    public EdgarParser(String formType) {
        this.FORM_TYPE = formType;
    }

    public void getIdxFiles(String path, List<String> list) {
        //TODO: remove this
        if (this.loadedIdxFiles > 2) return;
        String fileExt = getFileExtension(path);
        switch (fileExt) {
            case (".idx"):
                String[] urlSplit = path.split("/");
                urlSplit = Arrays.copyOfRange(urlSplit, urlSplit.length - 3, urlSplit.length);
                String indexType = urlSplit[urlSplit.length - 1].split("\\.")[0];

                if (indexType.equals("form")) {
                    try {
                        String requestData = FTPCommunicator.loadUrl("daily-index/" + path);
                        if (requestData == null) {
                            failedIdxFiles++;
//                            throw new Exception(".idx file not available");
                        }
                        this.loadedIdxFiles++;
                        System.out.println(" parsed .idx file number " + loadedIdxFiles);
                        list.add(requestData);
                    } catch (Exception e) {
                        System.out.println(String.format("Url. %s not loadable", path));
                    }
                }
                break;
            case "":
                //traverse tree further
                try {
                    String indexData = FTPCommunicator.loadUrl("daily-index/" + path + "index.json");
                    if (indexData == null) {
                        failedIndexFiles++;
//                        throw new Exception("index json not available");
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
        }
    }

    private String getFileExtension(String s) {
        int lastIndexOf = s.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return s.substring(lastIndexOf);
    }

    public void parseIndexFile(String requestString, List<DailyData> list) {
        //TODO: Test if thread is fast enough for 10 requests/s otherwhise multithreading
        String splitString = requestString.substring(requestString.lastIndexOf("---") + 4);
        splitString.lines()
                .forEach(line -> {
                    try {
                        String[] arr = line.trim().split("\\s{2,}");
                        DailyData dailyData = new DailyData(arr[0], arr[1], arr[2], arr[3], arr[4]);
                        if (dailyData.formType().equals(FORM_TYPE)) {
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

    public void downloadData(DailyData dailyData, List<String> outputList) throws IOException, InterruptedException {
        String returnData = FTPCommunicator.loadUrl(dailyData.folderPath());
        if (returnData == null) return;
        outputList.add(returnData);
    }
}