package fr.univ_amu.iut.supervision.persistance;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.client.MongoClients;
import fr.univ_amu.iut.supervision.Configuration;
import fr.univ_amu.iut.supervision.ingestion.TrameRecue;
import java.time.Instant;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Vérifie que la trame arrive dans l'archive telle quelle, en hexadécimal.
 *
 * <p>Image {@code mongo:7} et non {@code mongo:8} : cette dernière refuse de
 * démarrer sur les noyaux Linux 6.19 et suivants (SERVER-121912), comme
 * {@code infra/docker-compose.yml} l'explique.
 */
@Testcontainers(disabledWithoutDocker = true)
class ArchiveTramesTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    private static Configuration configurationVers(String url) {
        return new Configuration(0, "", "", "", url, "", 0, "");
    }

    @Test
    void archiveLaTrameTelleQuelle() {
        ArchiveTrames archive = ArchiveTrames.connecter(configurationVers(MONGO.getConnectionString()));
        Instant recueLe = Instant.parse("2026-09-08T21:30:00Z");

        archive.archiver(new TrameRecue("PR-07", recueLe, new byte[] {0x0A, (byte) 0xFF}));

        try (var client = MongoClients.create(MONGO.getConnectionString())) {
            Document trouve = client.getDatabase("supervision")
                    .getCollection("trames")
                    .find(new Document("boitier", "PR-07"))
                    .first();

            assertThat(trouve).isNotNull();
            assertThat(trouve.getString("hex")).isEqualTo("0AFF");
            assertThat(trouve.getDate("recueLe").toInstant()).isEqualTo(recueLe);
        }
    }
}
