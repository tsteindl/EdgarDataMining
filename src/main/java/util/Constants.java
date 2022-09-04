package util;

public class Constants { //TODO: add JSON parsing possibility instead of hard coding here
    public static String DEFAULT_YEAR = "2020/";

    public static String[] CSV_DOCUMENT_ROOT = {
            "ownershipDocument"
    };

    public static String[] TABLE_NODE_TAGS = {
            "nonDerivativeTable",
            "nonDerivativeTransaction"
    };
    public static String[] CSV_TAG_NAMES_REP = {
            "documentType",
            "periodOfReport",
            "rptOwnerCik",
            "rptOwnerName",
            "isDirector",
            "isOfficer",
            "isTenPercentOwner",
            "isOther",
            "issuerCik",
            "issuerName",
            "issuerTradingSymbol"
    };

    public static String[][] CSV_TAGS_REP = {
            {"documentType"},
            {"periodOfReport"},
            {"reportingOwner", "reportingOwnerId", "rptOwnerCik"},
            {"reportingOwner", "reportingOwnerId", "rptOwnerName"},
            {"reportingOwner", "reportingOwnerRelationship", "isDirector"},
            {"reportingOwner", "reportingOwnerRelationship", "isOfficer"},
            {"reportingOwner", "reportingOwnerRelationship", "isTenPercentOwner"},
            {"reportingOwner", "reportingOwnerRelationship", "isOther"},
            {"issuer", "issuerCik"},
            {"issuer", "issuerName"},
            {"issuer", "issuerTradingSymbol"},
    };
    public static String[] CSV_TAG_NAMES_TABLE = {
            "securityTitle",
            "transactionDate",
            "deemedExecutionDate",
//            "transactionFormType",
            "transactionCode",
            "transactionTimeliness",
            "transactionShares",
            "transactionPricePerShare",
            "transactionAcquiredDisposedCode",
            "sharesOwnedFollowingTransaction",
            "directOrIndirectOwnership"
    };

    public static String[][] CSV_TAGS_TABLE = {
            {"securityTitle", "value"},
            {"transactionDate", "value"},
            {"deemedExecutionDate", "value"},
//            {"transactionCoding", "transactionFormType"},
            {"transactionCoding", "transactionCode"},
            {"transactionTimeliness", "value"},
            {"transactionAmounts", "transactionShares", "value"},
            {"transactionAmounts", "transactionPricePerShare", "value"},
            {"transactionAmounts", "transactionAcquiredDisposedCode", "value"},
            {"postTransactionAmounts", "sharesOwnedFollowingTransaction", "value"},
            {"ownershipNature", "directOrIndirectOwnership", "value"}
    };

    public static String[] NULLABLE_TAGS = {
            "deemedExecutionDate",
            "transactionTimeliness"
    };

}


