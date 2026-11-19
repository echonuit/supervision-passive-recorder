package fr.univ_amu.iut.supervision.capteurs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogueDemoTest {

    private final List<CapteurPosition> capteurs = new CatalogueDemo().lister();

    @Test
    void contientPlusieursCapteurs() {
        assertThat(capteurs).hasSizeGreaterThan(1);
    }

    @Test
    void chaqueCapteurADesIdentifiantsDistincts() {
        assertThat(capteurs).extracting(CapteurPosition::id).doesNotHaveDuplicates();
    }

    @Test
    void lesTroisEtatsSontRepresentes() {
        assertThat(capteurs)
                .extracting(CapteurPosition::etat)
                .contains(CapteurPosition.STABLE, CapteurPosition.AVERTISSEMENT, CapteurPosition.CRITIQUE);
    }

    @Test
    void uneAlerteAccompagneChaqueCapteurNonStable() {
        assertThat(capteurs)
                .filteredOn(capteur -> !CapteurPosition.STABLE.equals(capteur.etat()))
                .extracting(CapteurPosition::alerte)
                .allMatch(alerte -> !alerte.isBlank());
    }
}
