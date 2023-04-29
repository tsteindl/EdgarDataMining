package Form4Parser;

import Form4Parser.Types.TableType;

import java.util.List;

public class NonDerivativeHolding extends TableType {
    private String securityTitle=null;
    private Double sharesOwnedFollowingTransaction=null;
    private Double valueOwnedFollowingTransaction=null;
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
