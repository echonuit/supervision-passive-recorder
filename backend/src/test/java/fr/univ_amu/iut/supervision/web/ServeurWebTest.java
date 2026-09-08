package fr.univ_amu.iut.supervision.web;

import static org.assertj.core.api.Assertions.assertThat;

import fr.univ_amu.iut.supervision.persistance.BaseDeDonnees;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class ServeurWebTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    @Test
    void laRouteDeSanteRepond() {
        Javalin app = ServeurWeb.creer(null);
        JavalinTest.test(app, (serveur, client) -> {
            var reponse = client.get("/sante");
            assertThat(reponse.code()).isEqualTo(200);
            assertThat(reponse.body().string()).contains("ok");
        });
    }

    @Test
    void laRouteDeSanteDitLaBaseAbsenteQuandIlNyEnAPas() {
        Javalin app = ServeurWeb.creer(null);
        JavalinTest.test(app, (serveur, client) -> {
            assertThat(client.get("/sante").body().string()).contains("absente");
        });
    }

    @Test
    void laRouteDeSanteDitLaBaseJoignableQuandElleLest() {
        BaseDeDonnees base =
                BaseDeDonnees.connecter(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());

        Javalin app = ServeurWeb.creer(base);
        JavalinTest.test(app, (serveur, client) -> {
            assertThat(client.get("/sante").body().string()).contains("joignable");
        });
    }

    @Test
    void laPageDAccueilSeRend() {
        Javalin app = ServeurWeb.creer(null);
        JavalinTest.test(app, (serveur, client) -> {
            var reponse = client.get("/");
            assertThat(reponse.code()).isEqualTo(200);
            assertThat(reponse.body().string()).contains("Supervision");
        });
    }
}
