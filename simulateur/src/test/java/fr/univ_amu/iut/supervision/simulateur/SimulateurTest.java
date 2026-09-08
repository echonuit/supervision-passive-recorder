package fr.univ_amu.iut.supervision.simulateur;

import static org.assertj.core.api.Assertions.assertThat;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Vérifie ce que le simulateur met réellement sur le courtier. */
@Testcontainers(disabledWithoutDocker = true)
class SimulateurTest {

    private static final String REPLI = "repli";

    @Container
    static final GenericContainer<?> COURTIER = new GenericContainer<>("eclipse-mosquitto:2")
            .withExposedPorts(1883)
            .withCopyToContainer(
                    Transferable.of("listener 1883\nallow_anonymous true\n"), "/mosquitto/config/mosquitto.conf")
            .waitingFor(Wait.forListeningPort());

    @Test
    void publieUneTrameParBoitierSurSonPropreSujet() throws InterruptedException {
        Mqtt3BlockingClient abonne = MqttClient.builder()
                .useMqttVersion3()
                .serverHost(COURTIER.getHost())
                .serverPort(COURTIER.getMappedPort(1883))
                .buildBlocking();
        abonne.connect();

        List<Mqtt3Publish> recues = new ArrayList<>();
        try (Mqtt3BlockingClient.Mqtt3Publishes publications = abonne.publishes(MqttGlobalPublishFilter.ALL)) {
            abonne.subscribeWith().topicFilter("simulateur/#").send();

            Simulateur.publier(COURTIER.getHost(), COURTIER.getMappedPort(1883), 3);

            for (int i = 0; i < 3; i++) {
                publications.receive(10, TimeUnit.SECONDS).ifPresent(recues::add);
            }
        } finally {
            abonne.disconnect();
        }

        assertThat(recues).hasSize(3);
        assertThat(recues.stream().map(p -> p.getTopic().toString()))
                .containsExactlyInAnyOrder("simulateur/PR-01", "simulateur/PR-02", "simulateur/PR-03");
        // Trame(1) encodée sur un octet : 0x01, soit "01" en hexadécimal.
        assertThat(recues)
                .allSatisfy(publication -> assertThat(
                                new String(publication.getPayloadAsBytes(), StandardCharsets.US_ASCII))
                        .isEqualTo("01"));
    }

    @Test
    void leNombreDeBoitiersVientDuPremierArgument() {
        assertThat(Simulateur.nbBoitiers(new String[] {"5"})).isEqualTo(5);
    }

    @Test
    void sansArgumentLeNombreDeBoitiersPrendSaValeurParDefaut() {
        assertThat(Simulateur.nbBoitiers(new String[] {})).isEqualTo(24);
    }

    @Test
    void uneVariableDEnvironnementAbsenteDonneLaValeurParDefaut() {
        assertThat(Simulateur.env("VARIABLE_QUI_NEXISTE_PAS_ICI", REPLI)).isEqualTo(REPLI);
    }

    @Test
    void uneVariableDEnvironnementPresenteEstLue() {
        // PATH est posée sur toute machine qui fait tourner ces tests.
        assertThat(Simulateur.env("PATH", REPLI)).isNotEqualTo(REPLI).isNotBlank();
    }
}
