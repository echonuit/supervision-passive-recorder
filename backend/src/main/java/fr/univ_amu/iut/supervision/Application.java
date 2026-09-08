package fr.univ_amu.iut.supervision;

import fr.univ_amu.iut.supervision.ingestion.SourceDeTrames;
import fr.univ_amu.iut.supervision.ingestion.SourceMqtt;
import fr.univ_amu.iut.supervision.persistance.ArchiveTrames;
import fr.univ_amu.iut.supervision.persistance.BaseDeDonnees;
import fr.univ_amu.iut.supervision.supervision.Superviseur;
import fr.univ_amu.iut.supervision.web.ServeurWeb;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Point d'entrée : assemble les composants et démarre le serveur.
 *
 * <p>Il n'y a pas d'injection de dépendances. Chaque composant est
 * construit ici, explicitement, et branché sur les autres. C'est
 * volontaire : l'architecture du système se lit dans cette méthode.
 */
public final class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        Configuration config = Configuration.depuisEnvironnement();

        BaseDeDonnees base = BaseDeDonnees.connecter(config);
        ArchiveTrames archive = ArchiveTrames.connecter(config);

        SourceDeTrames source = new SourceMqtt(config);
        source.demarrer(trame -> {
            LOG.info("trame reçue de {} : {} octets", trame.idBoitier(), trame.octets().length);
            archive.archiver(trame);
            // Le décodage et la mise à jour de l'état du boîtier viendront ici.
        });

        Superviseur superviseur = new Superviseur(base);
        superviseur.demarrer();

        Javalin app = ServeurWeb.creer(base);
        app.start(config.port());
        LOG.info("serveur démarré sur le port {}", config.port());
    }
}
