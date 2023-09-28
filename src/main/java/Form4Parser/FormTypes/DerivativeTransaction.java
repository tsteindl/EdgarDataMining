package Form4Parser.FormTypes;

import java.time.LocalDate;

public class DerivativeTransaction extends TableType {
    private String securityTitle = null;
    private Double conversionOrExercisePrice=null;
    private LocalDate transactionDate = null;
    private LocalDate deemedExecutionDate = null;
    private Integer transactionFormType = null;
    private String transactionCode = null;
    private Boolean equitySwapInvolved = null;
    private String transactionTimeliness = null;
    private Double transactionShares = null;
    private Double transactionTotalValue = null;
    private Double transactionPricePerShare = null;
    private String transactionAcquiredDisposedCode = null;
    private LocalDate exerciseDate = null;
    private LocalDate expirationDate = null;
    private String underlyingSecurityTitle = null;
    private Double underlyingSecurityShares = null;
    private Double underlyingSecurityValue = null;
    private Double sharesOwnedFollowingTransaction = null;
    private Double valueOwnedFollowingTransaction = null;
    private String directOrIndirectOwnership = null;
    private String natureOfOwnership = null;

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

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDate getDeemedExecutionDate() {
        return deemedExecutionDate;
    }

    public void setDeemedExecutionDate(LocalDate deemedExecutionDate) {
        this.deemedExecutionDate = deemedExecutionDate;
    }

    public Integer getTransactionFormType() {
        return transactionFormType;
    }

    public void setTransactionFormType(Integer transactionFormType) {
        this.transactionFormType = transactionFormType;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public Boolean getEquitySwapInvolved() {
        return equitySwapInvolved;
    }

    public void setEquitySwapInvolved(Boolean equitySwapInvolved) {
        this.equitySwapInvolved = equitySwapInvolved;
    }

    public String getTransactionTimeliness() {
        return transactionTimeliness;
    }

    public void setTransactionTimeliness(String transactionTimeliness) {
        this.transactionTimeliness = transactionTimeliness;
    }

    public Double getTransactionShares() {
        return transactionShares;
    }

    public void setTransactionShares(Double transactionShares) {
        this.transactionShares = transactionShares;
    }

    public Double getTransactionTotalValue() {
        return transactionTotalValue;
    }

    public void setTransactionTotalValue(Double transactionTotalValue) {
        this.transactionTotalValue = transactionTotalValue;
    }

    public Double getTransactionPricePerShare() {
        return transactionPricePerShare;
    }

    public void setTransactionPricePerShare(Double transactionPricePerShare) {
        this.transactionPricePerShare = transactionPricePerShare;
    }

    public String getTransactionAcquiredDisposedCode() {
        return transactionAcquiredDisposedCode;
    }

    public void setTransactionAcquiredDisposedCode(String transactionAcquiredDisposedCode) {
        this.transactionAcquiredDisposedCode = transactionAcquiredDisposedCode;
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

//    @Override
//    public List<String> keys() {
//        List<String> fields = new ArrayList<>();
//        fields.add("securityTitle");
//        fields.add("conversionOrExercisePrice");
//        fields.add("transactionDate");
//        fields.add("deemedExecutionDate");
//        fields.add("transactionFormType");
//        fields.add("transactionCode");
//        fields.add("equitySwapInvolved");
//        fields.add("transactionTimeliness");
//        fields.add("transactionShares");
//        fields.add("transactionTotalValue");
//        fields.add("transactionPricePerShare");
//        fields.add("transactionAcquiredDisposedCode");
//        fields.add("exerciseDate");
//        fields.add("expirationDate");
//        fields.add("underlyingSecurityTitle");
//        fields.add("underlyingSecurityShares");
//        fields.add("underlyingSecurityValue");
//        fields.add("sharesOwnedFollowingTransaction");
//        fields.add("valueOwnedFollowingTransaction");
//        fields.add("directOrIndirectOwnership");
//        fields.add("natureOfOwnership");
//        return fields;
//    }

//    @Override
//    public List<Object> values() {
//        List<Object> fields = new ArrayList<>();
//        fields.add(getSecurityTitle());
//        fields.add(getConversionOrExercisePrice());
//        fields.add(getTransactionDate());
//        fields.add(getDeemedExecutionDate());
//        fields.add(getTransactionFormType());
//        fields.add(getTransactionCode());
//        fields.add(getEquitySwapInvolved());
//        fields.add(getTransactionTimeliness());
//        fields.add(getTransactionShares());
//        fields.add(getTransactionTotalValue());
//        fields.add(getTransactionPricePerShare());
//        fields.add(getTransactionAcquiredDisposedCode());
//        fields.add(getExerciseDate());
//        fields.add(getExpirationDate());
//        fields.add(getUnderlyingSecurityTitle());
//        fields.add(getUnderlyingSecurityShares());
//        fields.add(getUnderlyingSecurityValue());
//        fields.add(getSharesOwnedFollowingTransaction());
//        fields.add(getValueOwnedFollowingTransaction());
//        fields.add(getDirectOrIndirectOwnership());
//        fields.add(getNatureOfOwnership());
//        return fields;
//    }
}
