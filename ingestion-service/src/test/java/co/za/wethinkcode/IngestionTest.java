//package co.za.wethinkcode;
//
//import co.wethinkcode.trafficflow.*;
//
//import co.wethinkcode.trafficflow.CsvParser;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.CsvFileSource;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//
//class IntersectionLegacyTest {
//
//    @ParameterizedTest
//    @CsvFileSource(resources = "/intersections-legacy.csv", numLinesToSkip = 1)
//    void MissingSignalTypeParameter(String intersectionID, String district, String signalType, boolean activeFlag) {
//
//        if (signalType == null || signalType.trim().isEmpty()) {
//            IngestionServiceApp.signalRecord record = new IngestionServiceApp.signalRecord(
//                    intersectionID.trim(),
//                    district.trim(),
//                    signalType.trim(),
//                    "null"
//            );
//
//            assertEquals(activeFlag, record.getActiveFlag());
//        }
//    }
//
//    @ParameterizedTest
//    void IntersectionIdAllCaps(String intersectionID, String district, String signalType, String activeFlag) {
//        IngestionServiceApp.signalRecord record = CsvParser.parseLine(intersectionID, district, signalType, activeFlag);
//        assertEquals(intersectionID.toUpperCase(), record.getIntersectionID());
//    }
//
/// /    @ParameterizedTest
/// /    void RemoveRedundantIntersectionId(String intersectionID,String District,String signalType,boolean activeFlag) {
/// /        //Add all Id/s to list
/// /
/// /        }
/// /    }
//
//
//    @ParameterizedTest
//    void CheckIntersectionIdFormat(String intersectionID, String district, String signalType, String activeFlag) {
//        IngestionServiceApp.signalRecord record = CsvParser.parseLine(intersectionID, district, signalType, activeFlag);
//        assertTrue(record.getIntersectionID().matches("^INT-\\d{4}$"),
//                "intersection_id must match format INT-XXXX with 4 digits, but got: " + record.getIntersectionID());
//    }
//
//
//    @ParameterizedTest
//    void ActiveFlagCorrectFormat(String intersectionID, String district, String signalType, String activeFlag) {
//        IngestionServiceApp.signalRecord record = CsvParser.parseLine(intersectionID, district, signalType, activeFlag);
//        assertEquals(activeFlag, record.getActiveFlag());
//    }
//}
//
