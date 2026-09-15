package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

import java.util.Map;

public class CongestionServiceApp {

    private static int congestionLevel = 3;


    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7022);

        app.get("/congestion", ctx -> ctx.json(Map.of("level", congestionLevel))); // crerate key-val pair of congest lvl

        app.post("/congestion", ctx -> {
            int level = ctx.queryParamAsClass("level", Integer.class).getOrDefault(congestionLevel); // search for lvl: and val of level to use

            if (level < 0 || level > 8) {
                ctx.status(400).result("Level must be between 0 & 8 ");
                return;
            }

            congestionLevel = level;
            ctx.json(Map.of("level", congestionLevel));
        });


        // TODO (Tracks the city-wide Congestion Level (0-8).)
        // Add domain endpoints for congestion-service here.
    }
}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
