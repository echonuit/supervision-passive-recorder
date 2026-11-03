package fr.univ_amu.iut.supervision.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import fr.univ_amu.iut.supervision.Configuration;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class SourceLiveObjectsMqttTest {

    private static final String TOPIC = "router/~event/v1/data/new/urn/lora/test";

    @Container
    static final GenericContainer<?> MOSQUITTO = new GenericContainer<>("eclipse-mosquitto:2")
            .withExposedPorts(1883)
            .withCopyToContainer(
                    Transferable.of("listener 1883\nallow_anonymous true\n"), "/mosquitto/config/mosquitto.conf")
            .waitingFor(Wait.forListeningPort());

    private static Configuration configWithApiKey(String key) {
        return new Configuration(0, "", "", "", "", "", 0, "", "", key);
    }

    private static void publish(Mqtt3BlockingClient publisher, String json) {
        publisher
                .publishWith()
                .topic(TOPIC)
                .payload(json.getBytes(StandardCharsets.UTF_8))
                .send();
    }

    @Test
    void receivesWrappedFramesAndSurvivesAMalformedOne() throws Exception {
        BlockingQueue<TrameReceived> recues = new LinkedBlockingQueue<>();
        SourceLiveObjects source = new SourceLiveObjects(
                configWithApiKey("cle"), MOSQUITTO.getHost(), MOSQUITTO.getMappedPort(1883), false);
        source.start(recues::add);

        Mqtt3BlockingClient publisher = MqttClient.builder()
                .useMqttVersion3()
                .serverHost(MOSQUITTO.getHost())
                .serverPort(MOSQUITTO.getMappedPort(1883))
                .buildBlocking();
        publisher.connect();

        // L'abonnement est asynchrone : on republie jusqu'à réception.
        TrameReceived premiere = null;
        for (int i = 0; i < 20 && premiere == null; i++) {
            publish(publisher, "{\"streamId\":\"PR-01\",\"value\":{\"payload\":\"0AFF\"}}");
            premiere = recues.poll(500, TimeUnit.MILLISECONDS);
        }
        assertThat(premiere).isNotNull();
        assertThat(premiere.idBox()).isEqualTo("PR-01");

        // Une enveloppe invalide est ignorée, l'abonnement continue.
        recues.clear();
        publish(publisher, "pas du json");
        publish(publisher, "{\"streamId\":\"PR-02\",\"value\":{\"payload\":\"01\"}}");
        TrameReceived suivante = recues.poll(5, TimeUnit.SECONDS);
        assertThat(suivante).isNotNull();
        assertThat(suivante.idBox()).isEqualTo("PR-02");

        source.stop();
        publisher.disconnect();
    }
}

/**
 * Tests envelope decoding and the API-key guard for Live Objects, without a
 * real network connection (the host and port are hardcoded in the class, so
 * {@code demarrer} with a valid key isn't testable here).
 */
class SourceLiveObjectsTest {

    private static Configuration configWithApiKey(String key) {
        return new Configuration(0, "", "", "", "", "", 0, "", "", key);
    }

    @Test
    void startingWithoutApiKeyNeverInvokesReceiver() {
        SourceLiveObjects source = new SourceLiveObjects(configWithApiKey(""));
        AtomicBoolean invoked = new AtomicBoolean(false);

        source.start(trame -> invoked.set(true));

        assertThat(invoked).isFalse();
    }

    @Test
    void startingWithNullApiKeyNeverInvokesReceiver() {
        SourceLiveObjects source = new SourceLiveObjects(configWithApiKey(null));
        AtomicBoolean invoked = new AtomicBoolean(false);

        source.start(trame -> invoked.set(true));

        assertThat(invoked).isFalse();
    }

    @Test
    void stoppingWithoutHavingStartedThrowsNothing() {
        new SourceLiveObjects(configWithApiKey("")).stop();
    }

    @Test
    void decodeEnvelopeUsesStreamIdWhenPresent() throws Exception {
        SourceLiveObjects source = new SourceLiveObjects(configWithApiKey("x"));
        byte[] wrapper = """
                {"streamId":"PR-99","value":{"payload":"0AFF"}}
                """.getBytes(StandardCharsets.UTF_8);

        TrameReceived trame = source.decodeWrapper(wrapper);

        assertThat(trame.idBox()).isEqualTo("PR-99");
        assertThat(trame.octets()).containsExactly(0x0A, 0xFF);
        assertThat(trame.recueLe()).isNotNull();
    }

    @Test
    void decodeEnvelopeFallsBackToDevEuiWhenStreamIdAbsent() throws Exception {
        SourceLiveObjects source = new SourceLiveObjects(configWithApiKey("x"));
        byte[] wrapper = """
                {"value":{"devEUI":"PR-77","payload":"01"}}
                """.getBytes(StandardCharsets.UTF_8);

        TrameReceived trame = source.decodeWrapper(wrapper);

        assertThat(trame.idBox()).isEqualTo("PR-77");
        assertThat(trame.octets()).containsExactly(0x01);
    }

    @Test
    void decodeEnvelopeFallsBackToUnknownWhenNoIdentifierPresent() throws Exception {
        SourceLiveObjects source = new SourceLiveObjects(configWithApiKey("x"));
        byte[] wrapper = """
                {"value":{"payload":"01"}}
                """.getBytes(StandardCharsets.UTF_8);

        TrameReceived trame = source.decodeWrapper(wrapper);

        assertThat(trame.idBox()).isEqualTo("inconnu");
    }

    @Test
    void startingAgainstAClosedPortLogsErrorAndCanBeStopped() throws Exception {
        SourceLiveObjects source = new SourceLiveObjects(configWithApiKey("cle"), "localhost", 1, false);

        source.start(trame -> {});
        Thread.sleep(1500); // laisse whenComplete s'exécuter côté erreur

        source.stop();
    }
}
