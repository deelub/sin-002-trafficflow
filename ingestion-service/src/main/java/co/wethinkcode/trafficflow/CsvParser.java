package co.wethinkcode.trafficflow;

public class CsvParser {

    public static SignalRecord parseLine(String rawId, String district, String type, String flagStr) {
        if (rawId == null || !rawId.trim().toUpperCase().matches("^INT-\\d{4}$")) {
            throw new IllegalArgumentException("Format must be INT-XXXX with 4 digits");
        }

        boolean active = "TRUE".equalsIgnoreCase(flagStr.trim()) || "Y".equalsIgnoreCase(flagStr.trim());

        return new SignalRecord(rawId.trim().toUpperCase(), district.trim(), type.trim(), active);
    }
}