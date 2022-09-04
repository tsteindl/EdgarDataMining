import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import util.DailyData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EdgarScraper {
    private int loadedIndexFiles = 0;
    private int failedIndexFiles = 0;
    public String FORM_TYPE;
    private final List<String> indexFiles;
    private final List<DailyData> dailyDataList;

    public EdgarScraper(String formType) {
        this.FORM_TYPE = formType;
        this.indexFiles = new ArrayList<>();
        this.dailyDataList = new ArrayList<>();
    }

    public void scrapeIndexFiles(String path) {
        //TODO: refactor
        String fileExt = getFileExtension(path);
        switch (fileExt) {
            case (".idx"):
                String[] urlSplit = path.split("/");
                urlSplit = Arrays.copyOfRange(urlSplit, urlSplit.length - 3, urlSplit.length);
                String indexType = urlSplit[urlSplit.length - 1].split("\\.")[0];

                if (indexType.equals("form")) {
                    try {
                        String requestData = FTPCommunicator.loadIndexFile(path);
                        if (requestData == null) {
                            failedIndexFiles++;
//                            throw new Exception(".idx file not available");
                        }
                        this.loadedIndexFiles++;
                        System.out.println(" parsed .idx file number " + loadedIndexFiles);
                        this.indexFiles.add(requestData);
                    } catch (Exception e) {
                        System.out.println(String.format("Url. %s not loadable", path));
                    }
                }
                break;
            case "":
                //traverse tree further
                try {
                    String indexData = FTPCommunicator.loadNavFile(path);
                    if (indexData == null) {
                        failedIndexFiles++;
//                        throw new Exception("index json not available");
                    }
                    JsonObject indexJson = getJsonObjectFromString(indexData);
                    JsonArray itemArray = indexJson.get("directory").getAsJsonObject().get("item").getAsJsonArray();

                    for (JsonElement item : itemArray) {
                        JsonObject itemObj = item.getAsJsonObject();
                        String href = itemObj.get("href").getAsString();
                        scrapeIndexFiles(path + href);
                    }
                } catch (Exception e) {
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


    public List<DailyData> parseIndexFile(String requestString) {
        if (requestString == null) return null;
        String splitString = requestString.substring(requestString.lastIndexOf("---") + 4);
        return splitString.lines()
                .map(line -> (line == null) ? null : line.trim().split("\\s{2,}"))
                .filter(arr -> arr != null)
                .filter(arr -> arr[0].equals(FORM_TYPE))
                .map(arr -> new DailyData(arr[0], arr[1], arr[2], arr[3], arr[4]))
                .collect(Collectors.toList());
    }

    public static JsonObject getJsonObjectFromString(String string) {
        return new Gson().fromJson(string, JsonObject.class);
    }

    public void saveForm(String path, DailyData dailyData) throws IOException, InterruptedException {
        if (path == null || dailyData == null) return;
        String output = downloadData(dailyData);
        if (output == null) return;
        try (Writer writer = new BufferedWriter(new FileWriter(path))) {
            writer.append(output);
        }
    }

    public String downloadData(DailyData dailyData) throws IOException, InterruptedException {
        return FTPCommunicator.loadForm(dailyData.folderPath());
    }

    public List<String> getIndexFiles() {
        return this.indexFiles;
    }

}