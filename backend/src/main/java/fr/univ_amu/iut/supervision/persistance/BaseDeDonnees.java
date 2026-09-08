package fr.univ_amu.iut.supervision.persistance;

import fr.univ_amu.iut.supervision.Configuration;
import org.jdbi.v3.core.Jdbi;

/**
 * Accès à la base relationnelle, qui porte le référentiel de la campagne :
 * sites, boîtiers, déploiements, sessions attendues et constatées.
 *
 * <p>Le schéma est à concevoir par l'équipe ; voir {@code schema.sql}.
 */
public final class BaseDeDonnees {

    private final Jdbi jdbi;

    private BaseDeDonnees(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public static BaseDeDonnees connecter(Configuration config) {
        return connecter(config.dbUrl(), config.dbUser(), config.dbPassword());
    }

    public static BaseDeDonnees connecter(String url, String utilisateur, String motDePasse) {
        return new BaseDeDonnees(Jdbi.create(url, utilisateur, motDePasse));
    }

    /** Vérifie que la base répond. Sert à la route de santé et au premier test. */
    public boolean estJoignable() {
        Integer un = jdbi.withHandle(
                h -> h.createQuery("select 1").mapTo(Integer.class).one());
        return un != null && un == 1;
    }

    public Jdbi jdbi() {
        return jdbi;
    }
}
