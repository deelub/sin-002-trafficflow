package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static co.wethinkcode.trafficflow.IngestionServiceApp.cleanFile;

public class IntersectionServiceApp {

    public record IntersectionRecord(String id, String district, String signalType, String activeFlag) {
    }

    private static List<IntersectionRecord> cleanedIntersections = new ArrayList<>();

    private static void getCleanedData() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7020/intersections"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                cleanedIntersections = mapper.readValue(
                        response.body(),
                        new TypeReference<List<IntersectionRecord>>() {
                        }
                );
                System.out.println("Successfully fetched " + cleanedIntersections.size() + " canonical intersections.");
            } else {
                System.err.println("Failed to fetch intersections. HTTP status: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Error fetching canonical data from ingestion-service: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        getCleanedData();

        Javalin app = Javalin.create().start(7021);
//        List<IngestionServiceApp.signalRecord> cleanedData = cleanFile("/intersections-legacy.csv");
        app.get("/intersections/{id}", ctx -> {
            String id = ctx.pathParam("id");

            if (cleanedIntersections.isEmpty()) {
                getCleanedData();
            }
            IntersectionRecord foundRecord = cleanedIntersections.stream()
                    .filter(record -> record.id().equalsIgnoreCase(id))
                    .findFirst()
                    .orElse(null);

            if (foundRecord != null) {
                ctx.json(foundRecord);
            } else {
                ctx.status(404).result("Intersection not found");
            }
        });


        // TODO (Validates intersection/district names (source of truth).)
        // Add domain endpoints for intersection-service here.
    }


}

// MQ TODO: publishes a periodic heartbeat to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at
// MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig), consumed by intersection-watchdog.
