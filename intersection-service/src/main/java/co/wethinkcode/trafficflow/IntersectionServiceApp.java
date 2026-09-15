package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import co.wethinkcode.trafficflow.IngestionServiceApp;

import java.util.List;

import static co.wethinkcode.trafficflow.IngestionServiceApp.cleanFile;

public class IntersectionServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7021);
        List<IngestionServiceApp.signalRecord> cleanedData = cleanFile("/intersections-legacy.csv");
        app.get("/intersections", ctx -> ctx.json(cleanedData));


        // TODO (Validates intersection/district names (source of truth).)
        // Add domain endpoints for intersection-service here.
    }
}

// MQ TODO: publishes a periodic heartbeat to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at
// MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig), consumed by intersection-watchdog.
