package fr.univ_amu.iut.supervision.simulateur;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import fr.univ_amu.iut.supervision.trame.Hex;
import fr.univ_amu.iut.supervision.trame.Trame;
import fr.univ_amu.iut.supervision.trame.TrameCodec;
import java.nio.charset.StandardCharsets;

/**
 * Point de départ du simulateur de flotte.
 *
 * <p>Pour l'instant : publie une trame par boîtier virtuel sur le courtier
 * MQTT, sur le sujet {@code simulateur/<identifiant>}. Le corps du message
 * est la trame en hexadécimal.
 *
 * <p>Ce qu'il deviendra est décrit dans le sujet : un calendrier de
 * sessions cohérent avec les éphémérides des sites, et des pannes
 * scénarisées. Rien de cela n'est écrit ici.
 *
 * <p>Comme dans le backend, {@code main} ne fait que lire l'environnement et
 * déléguer : la publication vit dans {@link #publier}, qui est testable
 * contre un vrai courtier.
 */
public final class Simulateur {

    private static final int BOITIERS_PAR_DEFAUT = 24;

    public static void main(String[] args) {
        publier(env("MQTT_HOST", "localhost"), Integer.parseInt(env("MQTT_PORT", "1883")), nbBoitiers(args));
    }

    /** Publie une trame par boîtier virtuel, puis rend la main. */
    public static void publier(String hote, int port, int nbBoitiers) {
        Mqtt3BlockingClient client = MqttClient.builder()
                .useMqttVersion3()
                .identifier("simulateur")
                .serverHost(hote)
                .serverPort(port)
                .buildBlocking();
        client.connect();
        try {
            for (int i = 1; i <= nbBoitiers; i++) {
                String id = "PR-%02d".formatted(i);
                String hex = Hex.depuisOctets(TrameCodec.encoder(new Trame(1)));
                client.publishWith()
                        .topic("simulateur/" + id)
                        .payload(hex.getBytes(StandardCharsets.US_ASCII))
                        .send();
                System.out.printf("%s -> %s%n", id, hex);
            }
        } finally {
            client.disconnect();
        }
    }

    /** Nombre de boîtiers à simuler : premier argument, ou la valeur par défaut. */
    static int nbBoitiers(String[] args) {
        return args.length > 0 ? Integer.parseInt(args[0]) : BOITIERS_PAR_DEFAUT;
    }

    static String env(String nom, String defaut) {
        String v = System.getenv(nom);
        return v == null || v.isBlank() ? defaut : v;
    }
}
