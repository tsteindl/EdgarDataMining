package db;

import Form4Parser.Form4Parser;
import interfaces.FormOutputter;
import util.OutputException;

public class PSQLForm4Parser extends Form4Parser implements FormOutputter {
    public PSQLForm4Parser(String name, String input) {
        super(name, input);
    }


    @Override
    public void outputForm(String outputPath) throws OutputException {

    }
}
