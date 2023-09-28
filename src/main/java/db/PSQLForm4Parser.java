package db;

import Form4Parser.Form4Parser;
import interfaces.FormOutputter;
import util.OutputException;
import java.sql.Connection;

public class PSQLForm4Parser extends Form4Parser implements FormOutputter {
    Connection conn;
    public PSQLForm4Parser(String name, String input, Connection conn) {
        super(name, input);
        this.conn = conn;
    }

    @Override
    public void outputForm(String outputPath) throws OutputException {

    }
}
