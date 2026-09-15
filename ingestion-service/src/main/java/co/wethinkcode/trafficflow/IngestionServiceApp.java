package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.*;

public class IngestionServiceApp {


    public static class signalRecord {
        private final String intersectionID;
        private final String district;
        private final String signalType;
        private final String activeFlag;

        public signalRecord(String intersectionID, String district, String signalType, String activeFlag) {
            this.intersectionID = intersectionID;
            this.district = district;
            this.signalType = signalType;
            this.activeFlag = activeFlag;
        }

        public String getIntersectionID() {
            return intersectionID;
        }

        public String getDistrict() {
            return district;
        }

        public String getSignalType() {
            return signalType;
        }

        public String getActiveFlag() {
            return activeFlag;
        }
    }


    public static List<signalRecord> cleanFile(String filename) {
        List<signalRecord> records = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();

        try (InputStream is = IngestionServiceApp.class.getResourceAsStream(filename);
             InputStreamReader isr = new InputStreamReader(is);
             CSVReader csvReader = new CSVReaderBuilder(isr).withSkipLines(1).build()) {

            String[] nextRecord;

            while ((nextRecord = csvReader.readNext()) != null) {
                if (nextRecord.length < 4) continue;

                String inputDistrict = nextRecord[1].trim().toUpperCase();
                String inputSignalType = nextRecord[2].trim().toUpperCase();
                String inputActiveFlag = nextRecord[3].trim().toUpperCase();
                String rawId = nextRecord[0].trim();
                String rawSignalType;
                String rawActiveFlag;
                String rawDistrict;


                switch (inputDistrict) {
                    case "DOWNTOWN":
                    case "MIDTOWN":
                    case "UPTOWN":
                    case "EASTSIDE":
                    case "WESTSIDE":
                        rawDistrict = nextRecord[1].trim();
                        break;
                    default:
                        rawDistrict = "null";
                }

                switch (inputSignalType) {
                    case "4-WAY":
                    case "PEDESTRIAN":
                    case "STOP-SIGN":
                    case "UNKNOWN":
                    case "WESTSIDE":
                    case "ROUNDABOUT":
                        rawSignalType = nextRecord[2].trim();
                        break;
                    default:
                        rawSignalType = "null";
                }

                switch (inputActiveFlag) {
                    case "Y":
                    case "YES":
                    case "TRUE":
                    case "1":
                        rawActiveFlag = "true";
                        break;
                    case "N":
                    case "NO":
                    case "FALSE":
                    case "0":
                        rawActiveFlag = "false";
                        break;
                    case "UNKNOWN":
                        rawActiveFlag = "UNKNOWN";
                    default:
                        rawActiveFlag = "null";
                }


                records.add(new signalRecord(rawId, Character.toUpperCase(rawDistrict.charAt(0)) + rawDistrict.substring(1).toLowerCase(), Character.toUpperCase(rawSignalType.charAt(0)) + rawSignalType.substring(1).toLowerCase(Locale.ROOT), rawActiveFlag));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return records;
    }

    public static void main(String[] args) {


        List<signalRecord> cleanedData = cleanFile("/intersections-legacy.csv");
        Javalin app = Javalin.create();
        app.get("/health", ctx -> ctx.result("ok"));
        app.get("/intersections", ctx -> ctx.json(cleanedData));
        // TODO: read and clean src/main/resources/intersections-legacy.csv (intersections, districts, signal types data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.
        app.start(7020);
        //First : clean teh csv file
        // ensure it covers all the ways to validate an active flag
        //check for no caps or irregualr caps
        // If value iss missing determine it null.
        //Itenrsection ID must always be the sameformat : capital In - and four digits
        // If dtehree is tehe same four digits nullify seciond occurence (NO SPACING AT ALL IN intersection)
        //signal type : Capitalise the first letter
        // active flag : true or false, no other alternative
        //Make getters for each and every parameter
    }


}