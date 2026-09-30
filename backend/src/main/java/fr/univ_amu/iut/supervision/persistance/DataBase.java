package fr.univ_amu.iut.supervision.persistance;

import fr.univ_amu.iut.supervision.Configuration;
import org.jdbi.v3.core.Jdbi;

/**
 * Accès à la base relationnelle, qui porte le référentiel de la campagne :
 * sites, boîtiers, déploiements, sessions attendues et constatées.
 *
 * <p>Le schéma est à concevoir par l'équipe ; voir {@code schema.sql}.
 */
public final class DataBase {

    private final Jdbi jdbi;

    private DataBase(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public static DataBase connect(Configuration config) {
        return connect(config.dbUrl(), config.dbUser(), config.dbPassword());
    }

    public static DataBase connect(String url, String utilisateur, String motDePasse) {
        return new DataBase(Jdbi.create(url, utilisateur, motDePasse));
    }

    /** Vérifie que la base répond. Sert à la route de santé et au premier test. */
    public boolean isJoinable() {
        Integer un = jdbi.withHandle(
                h -> h.createQuery("select 1").mapTo(Integer.class).one());
        return un != null && un == 1;
    }

    public Jdbi jdbi() {
        return jdbi;
    }
}
