package fr.univ_amu.iut.supervision.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import fr.univ_amu.iut.supervision.Configuration;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Vérifie que ce qui est publié sur le courtier ressort en {@link TrameRecue}. */
@Testcontainers(disabledWithoutDocker = true)
class SourceMqttTest {

    @Container
    static final GenericContainer<?> COURTIER = new GenericContainer<>("eclipse-mosquitto:2")
            .withExposedPorts(1883)
            .withCopyToContainer(
                    Transferable.of("listener 1883\nallow_anonymous true\n"), "/mosquitto/config/mosquitto.conf")
            .waitingFor(Wait.forListeningPort());

    private static Configuration configuration() {
        return new Configuration(0, "", "", "", "", COURTIER.getHost(), COURTIER.getMappedPort(1883), "simulateur/#");
    }

    @Test
    void uneTrameLuePuisPubliéeRessortDécodée() throws InterruptedException {
        SourceMqtt source = new SourceMqtt(configuration());
        AtomicReference<TrameRecue> recue = new AtomicReference<>();
        CountDownLatch arrivee = new CountDownLatch(1);

        source.demarrer(trame -> {
            recue.set(trame);
            arrivee.countDown();
        });

        Mqtt3BlockingClient editeur = MqttClient.builder()
                .useMqttVersion3()
                .serverHost(COURTIER.getHost())
                .serverPort(COURTIER.getMappedPort(1883))
                .buildBlocking();
        editeur.connect();
        try {
            // L'abonnement de SourceMqtt est asynchrone : on republie jusqu'à ce
            // qu'il soit en place, plutôt que de parier sur un délai fixe.
            for (int essai = 0; essai < 30 && arrivee.getCount() > 0; essai++) {
                editeur.publishWith()
                        .topic("simulateur/PR-42")
                        .payload("0AFF".getBytes(StandardCharsets.US_ASCII))
                        .send();
                arrivee.await(200, TimeUnit.MILLISECONDS);
            }
        } finally {
            editeur.disconnect();
        }

        assertThat(arrivee.getCount()).as("aucune trame reçue du courtier").isZero();
        assertThat(recue.get().idBoitier()).isEqualTo("PR-42");
        assertThat(recue.get().octets()).containsExactly(0x0A, 0xFF);
        assertThat(recue.get().recueLe()).isNotNull();

        source.arreter();
    }

    @Test
    void sArreterSansAvoirDemarreNeLeveRien() {
        new SourceMqtt(configuration()).arreter();
    }
}
