package Form4Parser;

import Form4Parser.FormTypes.TableType;
import csv.CSVTableBuilder;
import interfaces.FormConverter;
import org.xml.sax.SAXException;
import util.OutputException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CSVForm4Parser extends Form4Parser {
    public CSVForm4Parser(String name, String input) {
        super(name, input);
    }

    @Override
    public FormConverter configureOutputter(String outputPath, FormConverter.Outputter type) throws OutputException {
        try {
            Map<String, List<? extends TableType>> tables = new LinkedHashMap<>();
            tables.put("reportingOwners", this.reportingOwners);
            tables.put("nonDerivativeTransactions", this.nonDerivativeTransactions);
            tables.put("nonDerivativeHoldings", this.nonDerivativeHoldings);
            tables.put("derivativeTransactions", this.derivativeTransactions);
            tables.put("derivativeHoldings", this.derivativeHoldings);

            Map<String, Object> nonNestedTags = new LinkedHashMap<>();
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
}
