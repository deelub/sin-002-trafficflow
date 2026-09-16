package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

import java.util.Map;

public class RoutingServiceApp {

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
