package fr.univ_amu.iut.supervision.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.Test;

class ServeurWebTest {

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
    void laPageDAccueilSeRend() {
        Javalin app = ServeurWeb.creer(null);
        JavalinTest.test(app, (serveur, client) -> {
            var reponse = client.get("/");
            assertThat(reponse.code()).isEqualTo(200);
            assertThat(reponse.body().string()).contains("Supervision");
        });
    }
}
