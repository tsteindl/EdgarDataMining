package util;

public class Constants { //TODO: add JSON parsing possibility instead of hard coding here
    public static String DEFAULT_YEAR = "2020/";

    public static String CSV_DOCUMENT_ROOT = "ownershipDocument";

    public static String[] TABLE_NODE_TAGS = {
            "reportingOwner",
            "nonDerivativeTable",
            "derivativeTable",
    };

    //TODO: add table mapping using hash table

    public static String[] NULLABLE_TAGS = {
            "ownershipDocument",
            "schemaVersion",
            "notSubjectToSection16",
            "deemedExecutionDate",
            "transactionCoding",
            "transactionTimeliness",
            "deemedExecutionDate",
            "transactionCoding",
            "transactionTimeliness",
    };

    public static String[] EXCLUDE_TAGS = {
            "footnotes",
            "ownerSignature",
            "remarks",
    };

    public static String FORM_PATH = "documentation/doc4.xml";

}


