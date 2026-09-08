package fr.univ_amu.iut.supervision.supervision;

import static org.assertj.core.api.Assertions.assertThatCode;

import fr.univ_amu.iut.supervision.persistance.BaseDeDonnees;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class SuperviseurTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    private static BaseDeDonnees base() {
        return BaseDeDonnees.connecter(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @Test
    void uneEvaluationInterrogeLaBaseSansEchouer() {
        Superviseur superviseur = new Superviseur(base());

        // L'évaluation est encore un squelette : ce qu'on vérifie ici, c'est
        // qu'elle s'exécute et qu'elle sait joindre la base.
        assertThatCode(superviseur::evaluer).doesNotThrowAnyException();
    }

    @Test
    void seDemarreEtSArreteProprement() {
        Superviseur superviseur = new Superviseur(base());

        assertThatCode(() -> {
                    superviseur.demarrer();
                    superviseur.arreter();
                })
                .doesNotThrowAnyException();
    }
}
