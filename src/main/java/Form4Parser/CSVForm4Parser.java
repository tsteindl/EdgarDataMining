package Form4Parser;

import Form4Parser.FormTypes.TableType;
import csv.CSVTableBuilder;
import db.DBOutputter;
import interfaces.FormConverter;
import org.xml.sax.SAXException;
import util.OutputException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVForm4Parser extends Form4Parser{
    public CSVForm4Parser(String name, String input) {
        super(name, input);
    }

    public CSVTableBuilder configureCSV(String outputPath) throws OutputException {
        try {
            Map<String, List<? extends TableType>> tables = new HashMap<>();
            tables.put("reportingOwners", this.reportingOwners);
            tables.put("nonDerivativeTransactions", this.nonDerivativeTransactions);
            tables.put("nonDerivativeHoldings", this.nonDerivativeHoldings);
            tables.put("derivativeTransactions", this.derivativeTransactions);
            tables.put("derivativeHoldings", this.derivativeHoldings);
            return new CSVTableBuilder(
                    outputPath,
                    ";",
                    this.fields,
                    tables
            );
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new OutputException(e.getMessage());
        }
    }
    @Override
    public FormConverter configureOutputter(String outputPath, FormConverter.Outputter type) throws OutputException {
        return configureCSV(outputPath);
    }
}
