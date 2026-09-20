package co.wethinkcode.trafficflow.mq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Shared by every producer/consumer service that talks to the "congestion-topic"
 * ActiveMQ topic. Duplicated into each participating service's own source tree,
 * since these are independent Maven projects with no shared parent pom.
 */
public final class MqConfig {

    public static final String BROKER_URL = "tcp://localhost:61616";
    public static final String TOPIC = "congestion-topic";

    private MqConfig() {
    }

    public static void publishUpdate(int level){

        ActiveMQConnectionFactory connectionFactory= new ActiveMQConnectionFactory(BROKER_URL);

        try{
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic = session.createTopic("congestion-topic");

            MessageProducer producer = session.createProducer(topic);

            TextMessage message = session.createTextMessage("level" + level);
            producer.send(message);

            session.close();
            connection.close();

        }catch(JMSException e){
            e.printStackTrace();
        }
    }
}
