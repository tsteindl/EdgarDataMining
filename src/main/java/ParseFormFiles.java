import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ParseFormFiles {

    public static void main(String[] args) {
        String[] dates = {
                "20190102",
                "20190103",
                "20190104",
                "20190107",
        };

        for (String date : dates) {
            String year = "2019" + File.separator + date;
            final String dirPath = "data" + File.separator + "forms" + File.separator + year;

            HashMap<String, String> FORM_4_SET = null;

            String PATH_META_TABLE_FORM4 = "data/meta_table_form4.csv";
            FileReader filereader = null;
            try {
                filereader = new FileReader(PATH_META_TABLE_FORM4);
                CSVReader csvReader = new CSVReader(filereader);
                String[] record;
                FORM_4_SET = new HashMap<>();
                for (int i = 0; (record = csvReader.readNext()) != null; i++) {
                    if (i == 0) continue;
                    FORM_4_SET.put(record[0].split(";")[1], record[0].split(";")[0]);
                }
            } catch (CsvValidationException | IOException e) {
                e.printStackTrace();
            }

            ArrayList<HashMap<String, String>> csvData = new ArrayList<>();

            try {
                HashMap<String, String> finalFORM_4_SET = FORM_4_SET;
                Files.list(Paths.get(dirPath))
//                    .limit(100)
                        .map(Path::toString)
                        .forEach(file -> {
                            try {
                                Path fileName = Path.of(file);
                                String content = Files.readString(fileName);
                                /*
                                HashMap<String, String> temp = Form4Parser.parseForm4String(content, finalFORM_4_SET);

                                if (temp != null) {
                                    System.out.println("parsed file: " + file);
                                    csvData.add(temp);
                                }
                                */
                            } catch (IOException e) {
                                System.out.println("error");
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }

            Form4Parser.appendToOutPutCSV(csvData, FORM_4_SET, "2019");
        }
    }
}
