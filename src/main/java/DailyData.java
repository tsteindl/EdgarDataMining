public class DailyData {
    private final String formType;
    private final String companyName;
    private final String CIK;
    private final String dateFiled;
    private final String fileName;
    private final String folderPath;

    public DailyData(String url, String... args) {
        if (args == null) throw new IllegalArgumentException();
        this.formType = args[0];
        this.companyName = args[1];
        this.CIK = args[2];
        this.dateFiled = args[3];
        this.folderPath = args[4];
        this.fileName = url;
    }

    //GETTERS
    public String getFormType() {
        return formType;
    }
    public String getCompanyName() {
        return companyName;
    }
    public String getCIK() {
        return CIK;
    }
    public String getDateFiled() {
        return dateFiled;
    }
    public String getFileName() {
        return fileName;
    }
    public String getFolderPath() {
        return folderPath;
    }
}
