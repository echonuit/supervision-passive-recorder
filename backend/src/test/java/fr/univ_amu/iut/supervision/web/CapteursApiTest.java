package fr.univ_amu.iut.supervision.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.univ_amu.iut.supervision.capteurs.CapteurPosition;
import fr.univ_amu.iut.supervision.capteurs.CatalogueCapteurs;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Le contrat de {@code GET /api/capteurs}, tel que le front le lit. Pas besoin de Docker. */
class CapteursApiTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void laRouteRendLesCapteursAvecLeurPositionEnDegresDecimaux() {
        CatalogueCapteurs catalogue = () -> List.of(
                new CapteurPosition("PR-07", "Sainte-Baume", 43.318, 5.705, CapteurPosition.AVERTISSEMENT, 41, "SD"));

        JavalinTest.test(ServeurWeb.creer(null, catalogue), (serveur, client) -> {
            var reponse = client.get("/api/capteurs");

            assertThat(reponse.code()).isEqualTo(200);
            JsonNode tableau = JSON.readTree(reponse.body().string());
            assertThat(tableau).hasSize(1);
            JsonNode capteur = tableau.get(0);
            assertThat(capteur.get("id").asText()).isEqualTo("PR-07");
            assertThat(capteur.get("nom").asText()).isEqualTo("Sainte-Baume");
            assertThat(capteur.get("latitude").asDouble()).isEqualTo(43.318);
            assertThat(capteur.get("longitude").asDouble()).isEqualTo(5.705);
            assertThat(capteur.get("etat").asText()).isEqualTo("avertissement");
            assertThat(capteur.get("batterie").asInt()).isEqualTo(41);
            assertThat(capteur.get("alerte").asText()).isEqualTo("SD");
        });
    }

    @Test
    void sansCatalogueLaRouteRendUneListeVide() {
        Javalin app = ServeurWeb.creer(null);

        JavalinTest.test(app, (serveur, client) -> {
            var reponse = client.get("/api/capteurs");

            assertThat(reponse.code()).isEqualTo(200);
            assertThat(JSON.readTree(reponse.body().string())).isEmpty();
        });
    }
}
