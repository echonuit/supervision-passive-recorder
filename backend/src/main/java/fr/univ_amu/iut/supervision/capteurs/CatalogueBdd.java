package fr.univ_amu.iut.supervision.capteurs;

import fr.univ_amu.iut.supervision.persistance.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Catalogue lu en base : position = dernière position connue dans Historique,
 * à défaut celle de la table Capteur.
 */
public final class CatalogueBdd implements CatalogueCapteurs {

    private static final String SQL = """
            SELECT c.devEUI, c.nomCapteur,
                   COALESCE(h.latitude,  c.latitude)  AS latitude,
                   COALESCE(h.longitude, c.longitude) AS longitude,
                   c.pourcentageBatterie, c.pourcentageStockage
            FROM Capteur c
            LEFT JOIN Historique h ON h.idHistorique = (
                SELECT h2.idHistorique
                FROM Historique h2
                WHERE h2.idCapteur = c.idCapteur
                  AND h2.latitude IS NOT NULL
                  AND h2.longitude IS NOT NULL
                ORDER BY h2.dateHeure DESC, h2.idHistorique DESC
                LIMIT 1)
            """;

    private static final String SQL_PAR_UTILISATEUR = """
            SELECT c.devEUI, c.nomCapteur,
                   COALESCE(h.latitude,  c.latitude)  AS latitude,
                   COALESCE(h.longitude, c.longitude) AS longitude,
                   c.pourcentageBatterie, c.pourcentageStockage
            FROM Capteur c
            JOIN `est_lie_a` l ON c.idCapteur = l.idCapteur
            LEFT JOIN Historique h ON h.idHistorique = (
                SELECT h2.idHistorique
                FROM Historique h2
                WHERE h2.idCapteur = c.idCapteur
                  AND h2.latitude IS NOT NULL
                  AND h2.longitude IS NOT NULL
                ORDER BY h2.dateHeure DESC, h2.idHistorique DESC
                LIMIT 1)
            WHERE l.idUtilisateur = ?
            """;

    private final DbConnection dbConnection;
    private static final Logger LOG = LoggerFactory.getLogger(CatalogueBdd.class);

    public CatalogueBdd(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public List<CapteurPosition> lister() {
        List<CapteurPosition> capteurs = new ArrayList<>();

        try (Connection conn = dbConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(SQL);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                CapteurPosition capteur = lireLigne(rs);
                if (capteur != null) {
                    capteurs.add(capteur);
                }
            }
        } catch (SQLException e) {
            LOG.error("Lecture des capteurs impossible", e);
            throw new IllegalStateException("Lecture des capteurs impossible : " + e.getMessage(), e);
        }

        return capteurs;
    }

    @Override
    public List<CapteurPosition> lister(String userId) {
        List<CapteurPosition> capteurs = new ArrayList<>();

        try (Connection conn = dbConnection.connect();
                PreparedStatement stmt = conn.prepareStatement(SQL_PAR_UTILISATEUR)) {

            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CapteurPosition capteur = lireLigne(rs);
                    if (capteur != null) {
                        capteurs.add(capteur);
                    }
                }
            }
        } catch (SQLException e) {
            LOG.error("Lecture des capteurs impossible", e);
            throw new IllegalStateException("Lecture des capteurs impossible : " + e.getMessage(), e);
        }

        return capteurs;
    }

    /** Une ligne devient un capteur, ou {@code null} si elle n'a pas de position à afficher. */
    private static CapteurPosition lireLigne(ResultSet rs) throws SQLException {
        double latitude = rs.getDouble("latitude");
        boolean latitudeAbsente = rs.wasNull();
        double longitude = rs.getDouble("longitude");
        boolean longitudeAbsente = rs.wasNull();
        if (latitudeAbsente || longitudeAbsente) {
            return null;
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            LOG.warn(
                    "position ignorée pour {} : latitude={}, longitude={}",
                    rs.getString("devEUI"),
                    latitude,
                    longitude);
            return null;
        }

        int batterie = rs.getInt("pourcentageBatterie");
        int stockage = rs.getInt("pourcentageStockage");

        return new CapteurPosition(
                rs.getString("devEUI"),
                rs.getString("nomCapteur"),
                latitude,
                longitude,
                etat(batterie, stockage),
                batterie,
                alerte(batterie, stockage));
    }

    private static String etat(int batterie, int stockage) {
        if (batterie < 20 || stockage < 15) {
            return CapteurPosition.CRITIQUE;
        }
        if (batterie < 40 || stockage < 30) {
            return CapteurPosition.AVERTISSEMENT;
        }
        return CapteurPosition.STABLE;
    }

    private static String alerte(int batterie, int stockage) {
        boolean batterieFaible = batterie < 20;
        boolean stockageFaible = stockage < 15;

        if (batterieFaible && stockageFaible) {
            return "Batterie faible et stockage critique";
        }
        if (batterieFaible) {
            return "Batterie faible (<20%)";
        }
        if (stockageFaible) {
            return "Stockage presque saturé";
        }
        if (batterie < 40 || stockage < 30) {
            return "Niveau de ressource moyen";
        }
        return "";
    }
}
