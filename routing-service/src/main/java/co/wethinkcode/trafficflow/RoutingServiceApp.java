package co.wethinkcode.trafficflow;

import co.wethinkcode.trafficflow.mq.MqConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.time.Instant;
import java.util.Map;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static co.wethinkcode.trafficflow.mq.MqConfig.*;
import static java.net.URI.create;

public class RoutingServiceApp {

    private static volatile Instant lastHeartbeatReceived = Instant.now();
    private static volatile boolean isHealthy = true;

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

    private static String getCongestionLevel() {            //change this to return nothing?/


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

    public static double travelTime() {
        int BASE_TIME = 20;
        double eta = BASE_TIME * (1 + (extractCongestionLevel() / 8.0));
        return eta;

    }


    //Calculating eta : set a abse time :: base_time x ( 1 + congestionlevel/8);
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

            int congestionLevel = extractCongestionLevel();
            double etaMinutes = 20.0 * (1.0 + (congestionLevel / 8.0));

            ctx.json(Map.of(
                    "from", fromID,
                    "to", toID,
                    "status", "found_route",
                    "EstimateTime", Math.round(etaMinutes * 100.0) / 100.0

            ));

            // TODO (Provides estimated travel times based on congestion and intersection.)
            // Add domain endpoints for routing-service here.
        });
    }

    public static void startListening() {

        new Thread
                (() -> {
                    try {
                        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
                        Connection connection = connectionFactory.createConnection();
                        connection.start();

                        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                        Topic topic = session.createTopic(TOPIC);

                        MessageProducer producer = session.createProducer(topic);

                        MessageConsumer consumer = session.createConsumer(topic);

                        consumer.setMessageListener(message -> {
                            if (message instanceof TextMessage textMessage) {
                                try {
                                    String payload = textMessage.getText();
                                    System.out.println("Received update: " + payload);
                                } catch (JMSException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        connection.start();

                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }).start();
    }

    private static void startWatchdogTimer() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            long secondsSinceLastHeartbeat = Instant.now().getEpochSecond() - lastHeartbeatReceived.getEpochSecond();

            if (secondsSinceLastHeartbeat > MqConfig.TIMEOUT_THRESHOLD_SECONDS) {
                if (isHealthy) {
                    isHealthy = false;
                    System.err.println("[ALERT] ALARM RAISED! Intersection Service down. No heartbeat for "
                            + secondsSinceLastHeartbeat + "s!");
                }
            }
        }, 3, 3, TimeUnit.SECONDS);
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
