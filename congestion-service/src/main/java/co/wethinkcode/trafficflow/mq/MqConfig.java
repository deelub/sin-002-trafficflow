package co.wethinkcode.trafficflow.mq;

/**
 * Shared by every producer/consumer service that talks to the "congestion-topic"
 * ActiveMQ topic. Duplicated into each participating service's own source tree,
 * since these are independent Maven projects with no shared parent pom.
 */
public final class MqConfig {

    public static final String BROKER_URL = "tcp://localhost:61616";
    public static final String TOPIC = "congestion-topic";
    public static final String HEARTBEAT_QUEUE = "intersection-heartbeat-queue";

    private MqConfig() {
    }
}
