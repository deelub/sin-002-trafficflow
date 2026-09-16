package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.util.Map;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static java.net.URI.create;

public class RoutingServiceApp {

    private static boolean getIntersections(String ID) {

        if (ID == null || ID.isBlank()) {
            return false;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7021/intersections/" + ID))
                    .GET()
                    .build();

            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error validating intersection ID  " + e.getMessage());
            return false;
        }
    }

    private static String getCongestionLevel() {


        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7022/congestion"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("Failed to get congestion level. HTTP status: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error getting congestion levels: " + e.getMessage());
            return null;
        }
    }

    public record CongestionResponse(int level) {
    }

    private static int extractCongestionLevel() {
        String jsonResponse = getCongestionLevel();
        if (jsonResponse == null || jsonResponse.isBlank()) {
            return 0;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            CongestionResponse response = mapper.readValue(jsonResponse, CongestionResponse.class);
            return response.level();
        } catch (Exception e) {
            System.err.println("Error parsing congestion JSON: " + e.getMessage());
            return 0;
        }
    }

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/route", ctx -> {

            String fromID = ctx.queryParam("from");
            String toID = ctx.queryParam("to");

            if (fromID == null || toID == null) {
                ctx.status(400).result("Both IDs are required");
                return;
            }


            if (!getIntersections(fromID) || !getIntersections(toID)) {
                ctx.status(400).result("Invalid ID/s");
                return;
            }

            ctx.json(Map.of(
                    "from", fromID,
                    "to", toID,
                    "status", "found_route",
                    "level", extractCongestionLevel()

            ));

            // TODO (Provides estimated travel times based on congestion and intersection.)
            // Add domain endpoints for routing-service here.
        });
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
