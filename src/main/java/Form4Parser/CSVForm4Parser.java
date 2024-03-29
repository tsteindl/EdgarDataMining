package Form4Parser;

import Form4Parser.FormTypes.TableType;
import csv.CSVBuilder;
import csv.CSVTableBuilder;
import interfaces.FormOutputter;
import org.xml.sax.SAXException;
import util.OutputException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public class CSVForm4Parser extends Form4Parser implements FormOutputter {
    public CSVForm4Parser(String name, String input) {
        super(name, input);
    }

    public CSVBuilder configureOutputter(String outputPath) throws OutputException {
        try {
            LinkedHashMap<String, List<? extends TableType>> tables = new LinkedHashMap<>();
            tables.put("reportingOwners", this.reportingOwners);
            tables.put("nonDerivativeTransactions", this.nonDerivativeTransactions);
            tables.put("nonDerivativeHoldings", this.nonDerivativeHoldings);
            tables.put("derivativeTransactions", this.derivativeTransactions);
            tables.put("derivativeHoldings", this.derivativeHoldings);

            LinkedHashMap<String, Object> nonNestedTags = new LinkedHashMap<>();
            nonNestedTags.put("schemaVersion", schemaVersion);
            nonNestedTags.put("documentType", documentType);
            nonNestedTags.put("periodOfReport", periodOfReport);
            nonNestedTags.put("notSubjectToSection16", notSubjectToSection16);
            nonNestedTags.put("issuerCik", issuerCik);
            nonNestedTags.put("issuerName", issuerName);
            nonNestedTags.put("issuerTradingSymbol", issuerTradingSymbol);
            nonNestedTags.put("remarks", remarks);

            return new CSVTableBuilder(
                    outputPath,
                    ";",
                    nonNestedTags,
                    tables
            );
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new OutputException(e.getMessage());
        }
    }


    @Override
    public void outputForm(String outputPath) throws OutputException {
        CSVBuilder csvBuilder = configureOutputter(outputPath);
        csvBuilder.outputForm(outputPath);
    }
}
