package fr.univ_amu.iut.supervision;

import fr.univ_amu.iut.supervision.ingestion.SourceLiveObjects;
import fr.univ_amu.iut.supervision.ingestion.SourceMqtt;
import fr.univ_amu.iut.supervision.ingestion.TramesSource;
import fr.univ_amu.iut.supervision.persistance.ArchiveTrames;
import fr.univ_amu.iut.supervision.persistance.DataBase;
import fr.univ_amu.iut.supervision.persistance.DbModel;
import fr.univ_amu.iut.supervision.supervision.Superviseur;
import fr.univ_amu.iut.supervision.web.ServeurWeb;
import io.javalin.Javalin;

/**
 * Point d'entrée : assemble les composants et démarre le serveur.
 *
 * <p>Il n'y a pas d'injection de dépendances. Chaque composant est
 * construit ici, explicitement, et branché sur les autres. C'est
 * volontaire : l'architecture du système se lit dans cette méthode.
 */
public final class Application {

    static void main() {
        Configuration config = Configuration.fromEnvironment();

        DataBase base = DataBase.connect(config);
        // we use base2, because other system are not up to date for the new class that manage the database
        DbModel base2 = new DbModel();
        ArchiveTrames archive = ArchiveTrames.connecter(config);

        TramesSource source = createSource(config);
        source.start(archive::archiver);

        // TODO change the argument used by the constructor of Superviseur, make necessary change in Superviseur
        Superviseur superviseur = new Superviseur(base);
        superviseur.start();

        Javalin app = ServeurWeb.creer(base2);
        app.start(config.port());
    }

    /** Choisit l'implémentation d'ingestion selon INGESTION_MODE, sans que le reste du code le sache. */
    private static TramesSource createSource(Configuration config) {
        if ("live-objects".equals(config.ingestionMode())) {
            return new SourceLiveObjects(config);
        }
        return new SourceMqtt(config);
    }
}
