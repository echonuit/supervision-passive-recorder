package fr.univ_amu.iut.supervision;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConfigurationTest {

    @Test
    @DisplayName("la configuration se lit sans variable d'environnement posée")
    void lectureDepuisEnvironnement() {
        Configuration config = Configuration.depuisEnvironnement();

        // On n'affirme pas les valeurs par défaut elles-mêmes : la machine qui
        // lance les tests a le droit d'avoir PORT ou DB_URL dans son environnement.
        assertThat(config.port()).isPositive();
        assertThat(config.mqttPort()).isPositive();
        assertThat(config.dbUrl()).isNotBlank();
        assertThat(config.dbUser()).isNotBlank();
        assertThat(config.dbPassword()).isNotBlank();
        assertThat(config.mongoUrl()).isNotBlank();
        assertThat(config.mqttHost()).isNotBlank();
        assertThat(config.mqttTopic()).isNotBlank();
    }

    @Test
    @DisplayName("le record porte bien les valeurs qu'on lui donne")
    void leRecordPorteSesValeurs() {
        Configuration config = new Configuration(
                8080, "jdbc:postgresql://h/b", "u", "mdp", "mongodb://h:27017", "courtier", 1883, "sujet/#");

        assertThat(config.port()).isEqualTo(8080);
        assertThat(config.dbUrl()).isEqualTo("jdbc:postgresql://h/b");
        assertThat(config.dbUser()).isEqualTo("u");
        assertThat(config.dbPassword()).isEqualTo("mdp");
        assertThat(config.mongoUrl()).isEqualTo("mongodb://h:27017");
        assertThat(config.mqttHost()).isEqualTo("courtier");
        assertThat(config.mqttPort()).isEqualTo(1883);
        assertThat(config.mqttTopic()).isEqualTo("sujet/#");
    }
}
