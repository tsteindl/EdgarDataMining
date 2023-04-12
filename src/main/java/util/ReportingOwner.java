package util;

//TODO: maybe delete this
public record ReportingOwner(String cik, String ccc, String street1, String street2, String city, String state, String zip,
                             boolean isDirector, boolean isOfficer, boolean isTenPercentOwner,
                             String officerTitle, String otherText) {
}
