package Form4Parser.Types;

import Form4Parser.Types.TableType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DerivativeHolding extends TableType {
    private String securityTitle = null;
    private Double conversionOrExercisePrice=null;
    private LocalDate exerciseDate = null;
    private LocalDate expirationDate = null;
    private String underlyingSecurityTitle = null;
    private Double underlyingSecurityShares = null;
    private Double underlyingSecurityValue = null;
    private Double sharesOwnedFollowingTransaction = null;
    private Double valueOwnedFollowingTransaction = null;
    private String directOrIndirectOwnership = null;
    private String natureOfOwnership = null;

//    @Override
//    public List<String> keys() {
//        return Arrays.asList("securityTitle", "conversionOrExercisePrice", "exerciseDate", "expirationDate",
//                "underlyingSecurityTitle", "underlyingSecurityShares", "underlyingSecurityValue",
//                "sharesOwnedFollowingTransaction", "valueOwnedFollowingTransaction",
//                "directOrIndirectOwnership", "natureOfOwnership");
//    }
//
//    @Override
//    public List<Object> values() {
//        List<Object> fieldValues = new ArrayList<>();
//        fieldValues.add(this.getSecurityTitle());
//        fieldValues.add(this.getConversionOrExercisePrice());
//        fieldValues.add(this.getExerciseDate());
//        fieldValues.add(this.getExpirationDate());
//        fieldValues.add(this.getUnderlyingSecurityTitle());
//        fieldValues.add(this.getUnderlyingSecurityShares());
//        fieldValues.add(this.getUnderlyingSecurityValue());
//        fieldValues.add(this.getSharesOwnedFollowingTransaction());
//        fieldValues.add(this.getValueOwnedFollowingTransaction());
//        fieldValues.add(this.getDirectOrIndirectOwnership());
//        fieldValues.add(this.getNatureOfOwnership());
//        return fieldValues;
//    }

    public String getSecurityTitle() {
        return securityTitle;
    }

    public void setSecurityTitle(String securityTitle) {
        this.securityTitle = securityTitle;
    }

    public Double getConversionOrExercisePrice() {
        return conversionOrExercisePrice;
    }

    public void setConversionOrExercisePrice(Double conversionOrExercisePrice) {
        this.conversionOrExercisePrice = conversionOrExercisePrice;
    }

    public LocalDate getExerciseDate() {
        return exerciseDate;
    }

    public void setExerciseDate(LocalDate exerciseDate) {
        this.exerciseDate = exerciseDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getUnderlyingSecurityTitle() {
        return underlyingSecurityTitle;
    }

    public void setUnderlyingSecurityTitle(String underlyingSecurityTitle) {
        this.underlyingSecurityTitle = underlyingSecurityTitle;
    }

    public Double getUnderlyingSecurityShares() {
        return underlyingSecurityShares;
    }

    public void setUnderlyingSecurityShares(Double underlyingSecurityShares) {
        this.underlyingSecurityShares = underlyingSecurityShares;
    }

    public Double getUnderlyingSecurityValue() {
        return underlyingSecurityValue;
    }

    public void setUnderlyingSecurityValue(Double underlyingSecurityValue) {
        this.underlyingSecurityValue = underlyingSecurityValue;
    }

    public Double getSharesOwnedFollowingTransaction() {
        return sharesOwnedFollowingTransaction;
    }

    public void setSharesOwnedFollowingTransaction(Double sharesOwnedFollowingTransaction) {
        this.sharesOwnedFollowingTransaction = sharesOwnedFollowingTransaction;
    }

    public Double getValueOwnedFollowingTransaction() {
        return valueOwnedFollowingTransaction;
    }

    public void setValueOwnedFollowingTransaction(Double valueOwnedFollowingTransaction) {
        this.valueOwnedFollowingTransaction = valueOwnedFollowingTransaction;
    }

    public String getDirectOrIndirectOwnership() {
        return directOrIndirectOwnership;
    }

    public void setDirectOrIndirectOwnership(String directOrIndirectOwnership) {
        this.directOrIndirectOwnership = directOrIndirectOwnership;
    }

    public String getNatureOfOwnership() {
        return natureOfOwnership;
    }

    public void setNatureOfOwnership(String natureOfOwnership) {
        this.natureOfOwnership = natureOfOwnership;
    }
}
