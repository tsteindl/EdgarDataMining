package Form4Parser.FormTypes;

public class ReportingOwner extends TableType { //TODO: check if record works here
    private Integer rptOwnerCik=null;
    private String rptOwnerCcc=null;
    private String rptOwnerName=null;
    private String rptOwnerStreet1=null;
    private String rptOwnerStreet2=null;
    private String rptOwnerCity=null;
    private String rptOwnerState=null;
    private String rptOwnerZipCode=null;
    private Boolean isDirector=false;
    private Boolean isOfficer=false;
    private Boolean isTenPercentOwner=false;
    private Boolean isOther=false;
    private String officerTitle=null;
    private String otherText=null;

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


    /*
    GETTERS AND SETTERS
     */
    public String getRptOwnerCcc() {
        return rptOwnerCcc;
    }

    public void setRptOwnerCcc(String rptOwnerCcc) {
        this.rptOwnerCcc = rptOwnerCcc;
    }

    public String getRptOwnerName() {
        return rptOwnerName;
    }

    public void setRptOwnerName(String rptOwnerName) {
        this.rptOwnerName = rptOwnerName;
    }

    public String getRptOwnerStreet1() {
        return rptOwnerStreet1;
    }

    public void setRptOwnerStreet1(String rptOwnerStreet1) {
        this.rptOwnerStreet1 = rptOwnerStreet1;
    }

    public String getRptOwnerStreet2() {
        return rptOwnerStreet2;
    }

    public void setRptOwnerStreet2(String rptOwnerStreet2) {
        this.rptOwnerStreet2 = rptOwnerStreet2;
    }

    public String getRptOwnerCity() {
        return rptOwnerCity;
    }

    public void setRptOwnerCity(String rptOwnerCity) {
        this.rptOwnerCity = rptOwnerCity;
    }

    public String getRptOwnerState() {
        return rptOwnerState;
    }

    public void setRptOwnerState(String rptOwnerState) {
        this.rptOwnerState = rptOwnerState;
    }

    public String getRptOwnerZipCode() {
        return rptOwnerZipCode;
    }

    public void setRptOwnerZipCode(String rptOwnerZipCode) {
        this.rptOwnerZipCode = rptOwnerZipCode;
    }

    public Boolean getIsDirector() {
        return isDirector;
    }

    public void setDirector(Boolean director) {
        isDirector = director;
    }

    public Boolean getIsOfficer() {
        return isOfficer;
    }

    public void setIsOfficer(Boolean officer) {
        isOfficer = officer;
    }

    public Boolean getIsTenPercentOwner() {
        return isTenPercentOwner;
    }

    public void setTenPercentOwner(Boolean tenPercentOwner) {
        isTenPercentOwner = tenPercentOwner;
    }

    public Boolean getIsOther() {
        return isOther;
    }

    public void setOther(Boolean other) {
        isOther = other;
    }

    public String getOfficerTitle() {
        return officerTitle;
    }

    public void setOfficerTitle(String officerTitle) {
        this.officerTitle = officerTitle;
    }

    public String getOtherText() {
        return otherText;
    }

    public void setOtherText(String otherText) {
        this.otherText = otherText;
    }

    public Integer getRptOwnerCik() {
        return rptOwnerCik;
    }

    public void setRptOwnerCik(Integer rptOwnerCik) {
        this.rptOwnerCik = rptOwnerCik;
    }
}