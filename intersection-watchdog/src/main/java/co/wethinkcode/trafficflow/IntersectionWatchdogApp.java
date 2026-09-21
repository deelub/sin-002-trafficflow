package src.main.java.co.wethinkcode.trafficflow;

import io.javalin.Javalin;

public class IntersectionWatchdogApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7024);

        app.get("/health", ctx -> ctx.result("OK"));

        app.get("/alert", ctx -> {
            boolean healthy = HeartbeatListener.isHealthy();
            if (healthy) {
                ctx.json(Map.of(
                        "status", "OK",
                        "message", "Intersection Service is producing heartbeats normally.",
                        "lastSeen", HeartbeatListener.getLastHeartbeatReceived().toString()
                ));
            } else {
                ctx.status(503).json(Map.of(
                        "status", "CRITICAL",
                        "error", "Intersection Service heartbeat missed or timed out!",
                        "lastSeen", HeartbeatListener.getLastHeartbeatReceived().toString()
                ));
            }
        });

        // TODO (Cries for help if the Intersection Service crashes, since routes can no longer be validated.)
        // Mechanism: ActiveMQ Queue heartbeat/dead-letter
    }
}

/ / MQ TODO: subscribes to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at MqConfig.BROKER_URL
/ / (see co.wethinkcode.trafficflow.mq.MqConfig) and alerts if a heartbeat from
/ / intersection-service is missed or a message lands in the dead-letter queue.
