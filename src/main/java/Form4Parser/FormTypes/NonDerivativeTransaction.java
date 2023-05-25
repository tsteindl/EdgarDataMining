package Form4Parser.FormTypes;

import java.time.LocalDate;

public class NonDerivativeTransaction extends TableType {
    private String securityTitle = null;
    private LocalDate transactionDate = null;
    private LocalDate deemedExecutionDate = null;
    private String transactionFormType = null;
    private String transactionCode = null;
    private Boolean equitySwapInvolved = null;
    private String transactionTimeliness = null;
    private Double transactionShares = null;
    private Double transactionPricePerShare = null;
    private String transactionAcquiredDisposedCode = null;
    private Double sharesOwnedFollowingTransaction = null;
    private Double valueOwnedFollowingTransaction = null;
    private String directOrIndirectOwnership = null;
    private String natureOfOwnership = null;

//    @Override
//    public List<String> keys() {
//        //TODO: implement
//        return null;
//    }
//
//    @Override
//    public List<Object> values() {
//        return null;
//    }

    public String getSecurityTitle() {
        return securityTitle;
    }

    public void setSecurityTitle(String securityTitle) {
        this.securityTitle = securityTitle;
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

    public String getTransactionFormType() {
        return transactionFormType;
    }

    public void setTransactionFormType(String transactionFormType) {
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
