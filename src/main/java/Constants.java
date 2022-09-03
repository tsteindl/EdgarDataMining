public class Constants {
    public static String DEFAULT_YEAR = "2020/";
    public static String[] CSV_TAG_NAMES_REP = {"periodOfReport", "rptOwnerCik", "rptOwnerName", "isDirector", "isOfficer", "isTenPercentOwner", "isOther", "issuerCik", "issuerName", "issuerTradingSymbol",};
    public static String[] CSV_TAG_NAMES_TABLE = {"securityTitle", "transactionDate", "deemedExecutionDate", "transactionFormType", "transactionCode", "transactionTimeliness", "transactionShares", "transactionPricePerShare",
            "transactionAcquiredDisposedCode", "sharesOwnedFollowingTransaction", "directOrIndirectOwnership"};
    public static String[][] CSV_TAGS_REP = {
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
    public static String[][] CSV_TAGS_TABLE = {
            {"nonDerivativeTransaction", "securityTitle", "value"},
            {"nonDerivativeTransaction", "transactionDate", "value"},
            {"nonDerivativeTransaction", "deemedExecutionDate", "value"},
            {"nonDerivativeTransaction", "transactionCoding", "transactionFormType"},
            {"nonDerivativeTransaction", "transactionCoding", "transactionCode"},
            {"nonDerivativeTransaction", "transactionTimeliness", "value"},
            {"nonDerivativeTransaction", "transactionAmounts", "transactionShares"},
            {"nonDerivativeTransaction", "transactionAmounts", "transactionPricePerShare"},
            {"nonDerivativeTransaction", "transactionAmounts", "transactionAcquiredDisposedCode", "value"},
            {"nonDerivativeTransaction", "postTransactionAmounts", "sharesOwnedFollowingTransaction", "value"},
            {"nonDerivativeTransaction", "ownershipNature", "directOrIndirectOwnership", "value"}
    };

}


