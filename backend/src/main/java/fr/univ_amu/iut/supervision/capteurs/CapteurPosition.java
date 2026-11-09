package fr.univ_amu.iut.supervision.capteurs;

import java.util.List;

/**
 * Un capteur tel que la carte du tableau de bord l'affiche : où il est, et dans quel état.
 *
 * <p>C'est aussi le format JSON de la route {@code GET /api/capteurs}, champ pour champ.
 *
 * @param id identifiant du boîtier, par exemple {@code PR-07}
 * @param nom nom lisible du site où il est posé
 * @param latitude latitude en degrés décimaux, de -90 (sud) à 90 (nord)
 * @param longitude longitude en degrés décimaux, de -180 (ouest) à 180 (est)
 * @param etat {@link #STABLE}, {@link #AVERTISSEMENT} ou {@link #CRITIQUE}
 * @param batterie niveau de batterie en pourcentage
 * @param alerte message d'alerte, vide quand il n'y en a pas
 */
public record CapteurPosition(
        String id, String nom, double latitude, double longitude, String etat, int batterie, String alerte) {

    public static final String STABLE = "stable";
    public static final String AVERTISSEMENT = "avertissement";
    public static final String CRITIQUE = "critique";

    private static final List<String> ETATS = List.of(STABLE, AVERTISSEMENT, CRITIQUE);

    public CapteurPosition {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude hors de [-90 ; 90] : " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude hors de [-180 ; 180] : " + longitude);
        }
        if (!ETATS.contains(etat)) {
            throw new IllegalArgumentException("état inconnu : " + etat);
        }
    }
}
