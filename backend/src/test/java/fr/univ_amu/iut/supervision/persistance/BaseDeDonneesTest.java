package fr.univ_amu.iut.supervision.persistance;

import static org.assertj.core.api.Assertions.assertThat;

import fr.univ_amu.iut.supervision.Configuration;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test d'intégration sur une vraie base PostgreSQL démarrée dans un
 * conteneur. Ignoré automatiquement si Docker n'est pas disponible sur le
 * poste ; il s'exécute toujours en CI.
 */
@Testcontainers(disabledWithoutDocker = true)
class DataBaseTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    @Test
    void laBaseEstJoignable() {
        DataBase base =
                DataBase.connect(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());

        assertThat(base.isJoinable()).isTrue();
    }

    @Test
    void seConnecteAussiDepuisLaConfiguration() {
        Configuration config = new Configuration(
                0, POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword(), "", "", 0, "", "", "");

        DataBase base = DataBase.connect(config);

        assertThat(base.isJoinable()).isTrue();
    }

    @Test
    void exposeLeJdbiPourLesRequetesDuMetier() {
        DataBase base =
                DataBase.connect(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());

        Integer deux = base.jdbi()
                .withHandle(h -> h.createQuery("select 2").mapTo(Integer.class).one());

        assertThat(deux).isEqualTo(2);
    }
}
