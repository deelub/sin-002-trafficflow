package co.wethinkcode.trafficflow;

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

    private static boolean getCongestionlevel(String ID) {

        if (ID == null || ID.isBlank()) {
            return false;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7021/intersections/" + ID))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error validating intersection ID  " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/route", ctx -> {

            String fromID = ctx.queryParam("from");
            String toID = ctx.queryParam("to");

            if (fromID == null || toID == null) {
                ctx.status(400).result("Bothvalues are needed");
                return;
            }

            ctx.json(Map.of(
                    "from", fromID,
                    "to", toID,
                    "status", "found_route"
            ));

            // TODO (Provides estimated travel times based on congestion and intersection.)
            // Add domain endpoints for routing-service here.
        });
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
