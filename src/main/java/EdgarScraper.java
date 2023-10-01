import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import util.DailyData;
import util.IndexFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class EdgarScraper {
    private int loadedIndexFiles = 0;
    private int failedIndexFiles = 0;
    public String FORM_TYPE;
    private final List<IndexFile> indexFiles;
    private final List<DailyData> dailyDataList;

    public EdgarScraper(String formType) {
        this.FORM_TYPE = formType;
        this.indexFiles = new ArrayList<>();
        this.dailyDataList = new ArrayList<>();
    }


    public void scrapeIndexFiles(String path, List<IndexFile> indexFiles, int delay) {
        //TODO: refactor
        String fileExt = getFileExtension(path);
        switch (fileExt) {
            case (".idx"):
                String[] urlSplit = path.split("/");
                urlSplit = Arrays.copyOfRange(urlSplit, urlSplit.length - 3, urlSplit.length);
                String indexType = urlSplit[urlSplit.length - 1].split("\\.")[0];

                if (indexType.equals("form")) {
                    try {
                        String requestData = FTPCommunicator.loadIndexFile(path, delay);
                        if (requestData == null) {
                            failedIndexFiles++;
//                            throw new Exception(".idx file not available");
                        }
                        this.loadedIndexFiles++;
                        System.out.println(" parsed .idx file number " + loadedIndexFiles);
                        indexFiles.add(new IndexFile(path, requestData));
                    } catch (Exception e) {
                        System.out.println(String.format("Url. %s not loadable", path));
                    }
                }
                break;
            case "":
                //traverse tree further
                try {
                    String indexData = FTPCommunicator.loadNavFile(path, delay);
                    if (indexData == null) {
                        failedIndexFiles++;
                    }
                    JsonObject indexJson = getJsonObjectFromString(indexData);
                    JsonArray itemArray = indexJson.get("directory").getAsJsonObject().get("item").getAsJsonArray();

                    for (JsonElement item : itemArray) {
                        JsonObject itemObj = item.getAsJsonObject();
                        String href = itemObj.get("href").getAsString();
                        scrapeIndexFiles(path + href, indexFiles, delay);
                    }
                } catch (Exception e) {
                    System.out.println(String.format("Url. %s not loadable", path));
                }
                break;
            default:
        }
    }

    public void scrapeIndexFilesConc(String path, List<Runnable> downloadQueue, List<IndexFile> indexFiles) {
        int delay = 0;
        //TODO: refactor
        String fileExt = getFileExtension(path);
        switch (fileExt) {
            case (".idx"):
                String[] urlSplit = path.split("/");
                urlSplit = Arrays.copyOfRange(urlSplit, urlSplit.length - 3, urlSplit.length);
                String indexType = urlSplit[urlSplit.length - 1].split("\\.")[0];

                if (indexType.equals("form")) {
                    downloadQueue.add(() -> { //TODO: check if this works without try/catch
                        try {
                            String requestData = null;
                            requestData = FTPCommunicator.loadIndexFile(path, delay);
                            if (requestData == null) {
                                failedIndexFiles++;
//                                throw new Exception(".idx file not available"); //TODO: return here
                            }
                            this.loadedIndexFiles++;
                            System.out.println(" parsed .idx file number " + loadedIndexFiles);
                            indexFiles.add(new IndexFile(path, requestData));
                        } catch (Exception e) {
                            System.out.println(String.format("Url. %s not loadable", path));
                        }
                    });
                }
                break;
            case "":
                //traverse tree further
                downloadQueue.add(() -> {
                    try {
                        String indexData = FTPCommunicator.loadNavFile(path, delay);
                        if (indexData == null) {
                            failedIndexFiles++;
                        }
                        JsonObject indexJson = getJsonObjectFromString(indexData);
                        JsonArray itemArray = indexJson.get("directory").getAsJsonObject().get("item").getAsJsonArray();

                        for (JsonElement item : itemArray) {
                            JsonObject itemObj = item.getAsJsonObject();
                            String href = itemObj.get("href").getAsString();
                            scrapeIndexFilesConc(path + href, downloadQueue, indexFiles);
                        }
                    } catch (Exception e) {
                        System.out.println(String.format("Url. %s not loadable", path));
                    }
                });
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


//    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.MILLISECONDS)
//    @Fork(1)
//    @Warmup(iterations = 3)
//    @Measurement(iterations = 5)
    public List<DailyData> parseIndexFile(IndexFile indexFile, int maxNoForms) { //TODO: optimize this it is taking extremely long
        if (indexFile == null) return null;
        String splitString = indexFile.data().substring(indexFile.data().lastIndexOf("---") + 4);
        String outputFolder = "data/" + indexFile.path().replace(".idx", "");

//        return extractDailyDataListFromResponseWithScanner(splitString, outputFolder);
        return extractDailyDataListFromResponseWithStreams(splitString, outputFolder, maxNoForms);
    }

    private List<DailyData> extractDailyDataListFromResponseWithStreams(String splitString, String outputFolder, int maxNoForms) {
        return splitString
                .lines()
                .filter(Objects::nonNull)
                .map(line -> line.trim().split("\\s{3,}"))//TODO: potentially unsafe regex (what if people use 3 or more spaces in their name"
                .filter(arr -> arr[0].equals(FORM_TYPE))
                .map(arr -> new DailyData(arr[0], arr[1], arr[2], arr[3], arr[4], outputFolder + "/" + arr[4].replace("/", "_").replace(".txt", "") + ".csv"))
                .limit(maxNoForms)
                .collect(Collectors.toList());
    }

    private List<DailyData> extractDailyDataListFromResponseWithScanner(String splitString, String outputFolder) {
        List<DailyData> dailyDataList = new ArrayList<>();
        try (Scanner scanner = new Scanner(new StringReader(splitString))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] arr = line.split("\\s{2,}");

                if (arr.length >= 5 && arr[0].equals(FORM_TYPE)) {
                    String csvFilePath = outputFolder + "/" + arr[4].replace("/", "_").replace(".txt", "") + ".csv";
                    dailyDataList.add(new DailyData(arr[0], arr[1], arr[2], arr[3], arr[4], csvFilePath));
                }
            }
        }
        return dailyDataList;
    }

    public static JsonObject getJsonObjectFromString(String string) {
        return new Gson().fromJson(string, JsonObject.class);
    }

    public void saveForm(String path, DailyData dailyData) throws IOException, InterruptedException {
        if (path == null || dailyData == null) return;
        String output = downloadData(dailyData, 100);
        if (output == null) return;
        try (Writer writer = new BufferedWriter(new FileWriter(path))) {
            writer.append(output);
        }
    }

    public String downloadData(DailyData dailyData, int delay) throws IOException, InterruptedException {
        return FTPCommunicator.loadForm(dailyData.folderPath(), delay);
    }

    public List<IndexFile> getIndexFiles() {
        return this.indexFiles;
    }

}