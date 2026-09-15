package co.wethinkcode.trafficflow;

import co.wethinkcode.trafficflow.*;

public class CsvParser {

    public static IngestionServiceApp.signalRecord parseLine(String rawId, String district, String type, String flagStr) {
        if (rawId == null || !rawId.trim().toUpperCase().matches("^INT-\\d{4}$")) {
            throw new IllegalArgumentException("Format must be INT-XXXX with 4 digits");
        }

        String active = String.valueOf("TRUE".equalsIgnoreCase(flagStr.trim()) || "Y".equalsIgnoreCase(flagStr.trim()));

        return new IngestionServiceApp.signalRecord(rawId.trim().toUpperCase(), district.trim(), type.trim(), active);
    }
}