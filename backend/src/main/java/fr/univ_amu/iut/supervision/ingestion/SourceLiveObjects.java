package fr.univ_amu.iut.supervision.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Source de trames par abonnement MQTT à Live Objects (Orange)
 *
 * <p>Contrairement au simulateur, la connexion se fait en TLS, authentifiée par une
 * clé API Live Objects (utilisateur fixe {@code payload+bridge}, mot de passe = la clé),
 * et le corps du message n'est pas la trame brute mais une enveloppe JSON dans laquelle
 * la charge utile LoRa est nichée. Le nom exact des champs de cette enveloppe est à
 * confirmer sur une vraie trame (voir le TODO dans {@link #decoderEnveloppe}) ; ce qui
 * suit reproduit la structure documentée par Live Objects pour un connecteur LoRa, mais
 * n'a pas encore été vérifié contre une trame réelle de nos boîtiers.
 */
public final class SourceLiveObjects implements SourceDeTrames {

    private static final Logger LOG = LoggerFactory.getLogger(SourceLiveObjects.class);
    private static final String HOTE = "liveobjects.orange-business.com";
    private static final int PORT = 8883;
    private static final String UTILISATEUR = "payload+bridge";
    private static final String TOPIC = "router/~event/v1/data/new/urn/lora/#";

    private final Configuration config;
    private final ObjectMapper mapper = new ObjectMapper();
    private Mqtt3AsyncClient client;

    public SourceLiveObjects(Configuration config) {
        this.config = config;
    }

    @Override
    public void demarrer(Consumer<TrameRecue> recepteur) {
        if (config.loApiKey() == null || config.loApiKey().isBlank()) {
            LOG.error("LO_API_KEY absente : impossible de se connecter à Live Objects");
            return;
        }

        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier("backend-supervision-" + System.currentTimeMillis())
                .serverHost(HOTE)
                .serverPort(PORT)
                .sslWithDefaultConfig()
                .simpleAuth()
                .username(UTILISATEUR)
                .password(config.loApiKey().getBytes(StandardCharsets.UTF_8))
                .applySimpleAuth()
                .buildAsync();

        client.connect()
                .thenCompose(ack -> client.subscribeWith()
                        .topicFilter(TOPIC)
                        .callback(publication -> {
                            try {
                                TrameRecue trame = decoderEnveloppe(publication.getPayloadAsBytes());
                                recepteur.accept(trame);
                            } catch (Exception e) {
                                // Une enveloppe imprévue ne doit jamais interrompre l'abonnement.
                                LOG.warn("trame Live Objects ignorée (décodage impossible) : {}", e.getMessage());
                            }
                        })
                        .send())
                .whenComplete((ack, erreur) -> {
                    if (erreur != null) {
                        LOG.error("connexion Live Objects impossible vers {}:{}", HOTE, PORT, erreur);
                    } else {
                        LOG.info("abonné à {} sur {}:{}", TOPIC, HOTE, PORT);
                    }
                });
    }

    /**
     * Extrait une {@link TrameRecue} de l'enveloppe JSON publiée par Live Objects.
     *
     * <p>TODO équipe : brancher un vrai boîtier (ou consulter le Swagger Live Objects /
     * utiliser MQTT Explorer sur le topic ci-dessus) pour confirmer :
     * <ul>
     *   <li>le nom du champ identifiant le boîtier ({@code streamId} est une hypothèse,
     *       peut-être {@code devEUI} selon la config du connecteur LoRaWAN) ;</li>
     *   <li>où se trouve la charge utile brute (souvent {@code value.payload}, en
     *       hexadécimal ou en base64 selon la configuration du décodeur côté Live
     *       Objects).</li>
     * </ul>
     */
    private TrameRecue decoderEnveloppe(byte[] payloadBrut) throws Exception {
        JsonNode enveloppe = mapper.readTree(payloadBrut);

        String idBoitier = enveloppe.path("streamId").asText(null);
        if (idBoitier == null) {
            idBoitier = enveloppe.path("value").path("devEUI").asText("inconnu");
        }

        String hexCharge = enveloppe.path("value").path("payload").asText("");

        return new TrameRecue(idBoitier, Instant.now(), Hex.versOctets(hexCharge));
    }

    @Override
    public void arreter() {
        if (client != null) {
            client.disconnect();
        }
    }
}
