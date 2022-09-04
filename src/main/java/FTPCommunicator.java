import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
public class FTPCommunicator {

    public static String BASE_URL = "https://www.sec.gov/Archives/";
    private static String path = "data";

    public FTPCommunicator() {}

    public static String loadUrl(String path) throws IOException, InterruptedException {
        //wait to not exceed 10 requests per second
        //TODO: change this
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
            System.out.println("waiting for 11 minutes...");
            TimeUnit.MINUTES.sleep(11);
            status = con.getResponseCode();
        }
        if (status == 200) {
            return getResponseDataFromStream(con.getInputStream());
        }else {
            return null;
        }
    }

    private static String getResponseDataFromStream(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
            content.append("\n");
        }
        in.close();
        return content.toString();
    }
}



