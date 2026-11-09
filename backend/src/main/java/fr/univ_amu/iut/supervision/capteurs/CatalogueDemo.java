package fr.univ_amu.iut.supervision.capteurs;

import java.util.List;

/**
 * Données fictives, en attendant la base. Les trois états sont représentés pour voir les trois
 * couleurs de marqueur sur la carte. À supprimer quand le vrai catalogue existe.
 */
public final class CatalogueDemo implements CatalogueCapteurs {

    private static final List<CapteurPosition> CAPTEURS = List.of(
            new CapteurPosition("PR-01", "Calanques de Marseille", 43.2145, 5.4350, CapteurPosition.STABLE, 92, ""),
            new CapteurPosition("PR-02", "Montagne Sainte-Victoire", 43.5290, 5.6050, CapteurPosition.STABLE, 78, ""),
            new CapteurPosition(
                    "PR-03",
                    "Camargue, étang de Vaccarès",
                    43.5380,
                    4.6330,
                    CapteurPosition.AVERTISSEMENT,
                    34,
                    "Batterie faible"),
            new CapteurPosition("PR-04", "Luberon, Bonnieux", 43.8240, 5.3070, CapteurPosition.STABLE, 85, ""),
            new CapteurPosition(
                    "PR-05",
                    "Gorges du Verdon",
                    43.7480,
                    6.3300,
                    CapteurPosition.CRITIQUE,
                    9,
                    "Aucune trame depuis 26 h"),
            new CapteurPosition("PR-06", "Étang de Berre", 43.4500, 5.1000, CapteurPosition.STABLE, 66, ""),
            new CapteurPosition(
                    "PR-07",
                    "Massif de la Sainte-Baume",
                    43.3180,
                    5.7050,
                    CapteurPosition.AVERTISSEMENT,
                    41,
                    "Carte SD presque pleine"),
            new CapteurPosition("PR-08", "Alpilles", 43.7580, 4.8300, CapteurPosition.STABLE, 95, ""));

    @Override
    public List<CapteurPosition> lister() {
        return CAPTEURS;
    }
}
