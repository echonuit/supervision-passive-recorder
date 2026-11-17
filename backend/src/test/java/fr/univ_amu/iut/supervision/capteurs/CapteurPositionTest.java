package fr.univ_amu.iut.supervision.capteurs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CapteurPositionTest {

    private static CapteurPosition capteur(double latitude, double longitude, String etat) {
        return new CapteurPosition("PR-01", "Site", latitude, longitude, etat, 80, "");
    }

    @Test
    void porteSesValeursEnDegresDecimaux() {
        CapteurPosition capteur = capteur(43.2145, 5.435, CapteurPosition.STABLE);

        assertThat(capteur.latitude()).isEqualTo(43.2145);
        assertThat(capteur.longitude()).isEqualTo(5.435);
        assertThat(capteur.etat()).isEqualTo("stable");
    }

    @Test
    void accepteLesBornesDuGlobe() {
        assertThat(capteur(90, 180, CapteurPosition.CRITIQUE)).isNotNull();
        assertThat(capteur(-90, -180, CapteurPosition.AVERTISSEMENT)).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(doubles = {90.1, -90.1, 4300})
    void refuseUneLatitudeHorsLimites(double latitude) {
        assertThatThrownBy(() -> capteur(latitude, 5, CapteurPosition.STABLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("latitude");
    }

    @ParameterizedTest
    @ValueSource(doubles = {180.1, -180.1, 543})
    void refuseUneLongitudeHorsLimites(double longitude) {
        assertThatThrownBy(() -> capteur(43, longitude, CapteurPosition.STABLE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("longitude");
    }

    @Test
    void refuseUnEtatInconnu() {
        assertThatThrownBy(() -> capteur(43, 5, "en panne"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("état");
    }
}
