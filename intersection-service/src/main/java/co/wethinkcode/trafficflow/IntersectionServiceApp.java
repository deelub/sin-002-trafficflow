package co.wethinkcode.trafficflow;

import co.wethinkcode.trafficflow.mq.MqConfig;
import io.javalin.Javalin;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static co.wethinkcode.trafficflow.IngestionServiceApp.cleanFile;

public class IntersectionServiceApp {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
    ;

    public record IntersectionRecord(String intersectionID, String district, String signalType, String activeFlag) {
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

    public void startHeartbeat() {

        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 0, 3, TimeUnit.SECONDS);
        System.out.println("Heartbeat producer started ");
    }

    private void sendHeartbeat() {
        try (Connection connection = connectionFactory.createConnection()) {
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue queue = session.createQueue(MqConfig.HEARTBEAT_QUEUE);
            MessageProducer producer = session.createProducer(queue);

            long timestamp = System.currentTimeMillis();

            String jsonPayload = String.format(
                    "{\"service\": \"intersection-service\", \"timestamp\": %d}",
                    timestamp
            );

            TextMessage message = session.createTextMessage(jsonPayload);
            producer.send(message);

            session.close();
        } catch (JMSException e) {
            System.err.println("Failed to send heartbeat: " + e.getMessage());
        }
    }


    public void stopHeartbeat() {
        scheduler.shutdown();
    }

    public static void main(String[] args) {
        getCleanedData();

        IntersectionServiceApp serviceApp = new IntersectionServiceApp();
        serviceApp.startHeartbeat();
        Runtime.getRuntime().addShutdownHook(new Thread(serviceApp::stopHeartbeat));

        Javalin app = Javalin.create().start(7021);

//        List<IngestionServiceApp.signalRecord> cleanedData = cleanFile("/intersections-legacy.csv");
        app.get("/intersections/{id}", ctx -> {
            String id = ctx.pathParam("id");

            if (cleanedIntersections.isEmpty()) {
                getCleanedData();
            }
            IntersectionRecord foundRecord = cleanedIntersections.stream()
                    .filter(record -> record.intersectionID().equalsIgnoreCase(id))
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
