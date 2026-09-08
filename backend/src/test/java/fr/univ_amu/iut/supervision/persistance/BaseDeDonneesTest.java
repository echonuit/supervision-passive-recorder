package fr.univ_amu.iut.supervision.persistance;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
class BaseDeDonneesTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    @Test
    void laBaseEstJoignable() {
        BaseDeDonnees base =
                BaseDeDonnees.connecter(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        assertTrue(base.estJoignable());
    }
}
