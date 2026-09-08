package fr.univ_amu.iut.supervision.ingestion;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import fr.univ_amu.iut.supervision.Configuration;
import fr.univ_amu.iut.supervision.trame.Hex;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Source de trames par abonnement MQTT.
 *
 * <p>Convient telle quelle au simulateur (sujet {@code simulateur/<id>}, corps
 * en hexadécimal). L'adaptateur Live Objects s'en inspirera mais devra
 * lire le format d'enveloppe propre à la plateforme.
 */
public final class SourceMqtt implements SourceDeTrames {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMqtt.class);

    private final Configuration config;
    private Mqtt3AsyncClient client;

    public SourceMqtt(Configuration config) {
        this.config = config;
    }

    @Override
    public void demarrer(Consumer<TrameRecue> recepteur) {
        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier("backend-supervision")
                .serverHost(config.mqttHost())
                .serverPort(config.mqttPort())
                .buildAsync();

        client.connect()
                .thenCompose(ack -> client.subscribeWith()
                        .topicFilter(config.mqttTopic())
                        .callback(publication -> {
                            String sujet = publication.getTopic().toString();
                            String id = sujet.substring(sujet.lastIndexOf('/') + 1);
                            String hex = new String(publication.getPayloadAsBytes(), StandardCharsets.US_ASCII);
                            recepteur.accept(new TrameRecue(id, Instant.now(), Hex.versOctets(hex)));
                        })
                        .send())
                .whenComplete((ack, erreur) -> {
                    if (erreur != null) {
                        LOG.error("connexion MQTT impossible vers {}:{}", config.mqttHost(), config.mqttPort(), erreur);
                    } else {
                        LOG.info("abonné à {} sur {}:{}", config.mqttTopic(), config.mqttHost(), config.mqttPort());
                    }
                });
    }

    @Override
    public void arreter() {
        if (client != null) {
            client.disconnect();
        }
    }
}
