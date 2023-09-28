package db;

import Form4Parser.Form4Parser;
import Form4Parser.FormTypes.ReportingOwner;
import interfaces.FormOutputter;
import util.OutputException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class PSQLForm4Parser extends Form4Parser implements FormOutputter {
    Connection conn;
    public PSQLForm4Parser(String name, String input, Connection conn) {
        super(name, input);
        this.conn = conn;
    }

    @Override
    public void outputForm(String outputPath) throws OutputException {
        System.out.println("output Form " + this.name);
        try {
            PreparedStatement insertIssuerStmt = conn.prepareStatement("INSERT INTO issuer (cik, issuerName, issuerTradingSymbol) VALUES (?,?,?) ON CONFLICT (cik) DO NOTHING");
            insertIssuerStmt.setInt(1, issuerCik);
            insertIssuerStmt.setString(2, issuerName);
            insertIssuerStmt.setString(3, issuerTradingSymbol);
            insertIssuerStmt.addBatch();

            PreparedStatement insertReportingOwnerStmt = conn.prepareStatement("INSERT INTO reporting_owner VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (cik) DO NOTHING");
            for (ReportingOwner owner : reportingOwners) {
                insertReportingOwnerStmt.setInt(1, owner.getRptOwnerCik());
                insertReportingOwnerStmt.setString(2, owner.getRptOwnerCcc());
                insertReportingOwnerStmt.setString(3, owner.getRptOwnerName());
                insertReportingOwnerStmt.setString(4, owner.getRptOwnerStreet1());
                insertReportingOwnerStmt.setString(5, owner.getRptOwnerStreet2());
                insertReportingOwnerStmt.setString(6, owner.getRptOwnerCity());
                insertReportingOwnerStmt.setString(7, owner.getRptOwnerState());
                insertReportingOwnerStmt.setString(8, owner.getRptOwnerZipCode());
                insertReportingOwnerStmt.setBoolean(9, owner.getIsDirector());
                insertReportingOwnerStmt.setBoolean(10, owner.getIsOfficer());
                insertReportingOwnerStmt.setBoolean(11, owner.getIsTenPercentOwner());
                insertReportingOwnerStmt.setBoolean(12, owner.getIsOther());
                insertReportingOwnerStmt.setString(13, owner.getOfficerTitle());
                insertReportingOwnerStmt.setString(14, owner.getOtherText());

                insertReportingOwnerStmt.addBatch();
            }
            int[] upds = insertIssuerStmt.executeBatch(); //TODO: rework this
            System.out.println("Executed batch: " + Arrays.toString(upds));
            upds = insertReportingOwnerStmt.executeBatch();
            System.out.println("Executed batch: " + Arrays.toString(upds));
        } catch (SQLException e) {
            throw new OutputException(e.getMessage());
        }

    }
}
