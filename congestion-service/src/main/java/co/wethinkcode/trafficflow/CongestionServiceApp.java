package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Map;

import static co.wethinkcode.trafficflow.mq.MqConfig.*;

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

    public static void publishUpdate(int level) {

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);

        try {
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic = session.createTopic(TOPIC);

            MessageProducer producer = session.createProducer(topic);

            TextMessage message = session.createTextMessage("{'level':" + level + "}");
            producer.send(message);

            session.close();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
