package db;

import Form4Parser.Form4Parser;
import Form4Parser.FormTypes.NonDerivativeTransaction;
import Form4Parser.FormTypes.ReportingOwner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import interfaces.FormOutputter;
import org.postgresql.util.PGobject;
import util.OutputException;

import java.sql.*;
import java.sql.Date;
import java.util.*;

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
            insertIssuerStmt.executeUpdate();

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

            int[] upds = insertReportingOwnerStmt.executeBatch();

            PreparedStatement insertForm4Stmt = conn.prepareStatement("INSERT INTO form_4(name, documentType, periodOfReport, notSubjectToSection16, issuer_cik, nonDerivativeTransactions, nonDerivativeHoldings, derivativeTransactions, derivativeHoldings) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING");
            insertForm4Stmt.setString(1, this.name);
            insertForm4Stmt.setString(2, documentType);
            insertForm4Stmt.setDate(3, Date.valueOf(periodOfReport));
            insertForm4Stmt.setBoolean(4, notSubjectToSection16);
            insertForm4Stmt.setInt(5, issuerCik); //TODO: maybe not the best idea

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            //create JSON array of nonDerivativeTransaction
            String nDtransactionsJSONString = objectMapper.writeValueAsString(nonDerivativeTransactions);
            PGobject nDTransObj = new PGobject();
            nDTransObj.setType("json");
            nDTransObj.setValue(nDtransactionsJSONString);
            insertForm4Stmt.setObject(6, nDTransObj);

            //create JSON array of nonDerivativeHoldings
            String nDHoldingsString = objectMapper.writeValueAsString(nonDerivativeHoldings);
            PGobject nDHoldsObj = new PGobject();
            nDHoldsObj.setType("json");
            nDHoldsObj.setValue(nDHoldingsString);
            insertForm4Stmt.setObject(7, nDHoldsObj);

            //create JSON array of derivativeTransaction
            String dTransactionsJSONString = objectMapper.writeValueAsString(derivativeTransactions);
            PGobject dTransObj = new PGobject();
            dTransObj.setType("json");
            dTransObj.setValue(dTransactionsJSONString);
            insertForm4Stmt.setObject(8, dTransObj);

            //create JSON array of derivativeHoldings
            String dHoldsJSONString = objectMapper.writeValueAsString(derivativeHoldings);
            PGobject dHoldsObj = new PGobject();
            dHoldsObj.setType("json");
            dHoldsObj.setValue(dHoldsJSONString);
            insertForm4Stmt.setObject(9, dHoldsObj);

            int c = insertForm4Stmt.executeUpdate();

            if (c > 0) {
                int insertedForm4Id;
                ResultSet rs = conn.createStatement().executeQuery("select lastval()");
                if (rs.next()) {
                    insertedForm4Id = rs.getInt(1);
                } else {
                    throw new SQLException("Inserting data failed, no ID obtained.");
                }

                PreparedStatement insertReportingOwner_Form4Stmt = conn.prepareStatement("INSERT INTO reporting_owner_form_4 VALUES(?,?)");
                this.reportingOwners.stream().map(ReportingOwner::getRptOwnerCik).forEach(owner_cik -> {
                    try {
                        insertReportingOwner_Form4Stmt.setInt(1, owner_cik);
                        insertReportingOwner_Form4Stmt.setInt(2, insertedForm4Id);

                        insertReportingOwner_Form4Stmt.addBatch();

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                insertReportingOwner_Form4Stmt.executeUpdate();
            }

        } catch (SQLException | JsonProcessingException | RuntimeException e) {
            throw new OutputException(e);
        }

    }

    private void printResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        while (resultSet.next()) {
            for (int i = 1; i <= columnsNumber; i++) {
                if (i > 1) System.out.print(",  ");
                String columnValue = resultSet.getString(i);
                System.out.print(columnValue + " " + rsmd.getColumnName(i));
            }
            System.out.println("");
        }
    }
}
