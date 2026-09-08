package fr.univ_amu.iut.supervision.supervision;

import fr.univ_amu.iut.supervision.persistance.BaseDeDonnees;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Moteur de supervision : évalue périodiquement l'état de chaque boîtier.
 *
 * <p>C'est ici que vivra la détection d'absence, à partir des sessions
 * attendues (calculées sur les éphémérides du site) et des trames reçues.
 * Pour l'instant, la tâche ne fait que s'exécuter.
 */
public final class Superviseur {

    private static final Logger LOG = LoggerFactory.getLogger(Superviseur.class);

    private final BaseDeDonnees base;
    private final ScheduledExecutorService ordonnanceur = Executors.newSingleThreadScheduledExecutor();

    public Superviseur(BaseDeDonnees base) {
        this.base = base;
    }

    public void demarrer() {
        ordonnanceur.scheduleAtFixedRate(this::evaluer, 0, 1, TimeUnit.MINUTES);
    }

    void evaluer() {
        LOG.debug("évaluation des boîtiers (base joignable : {})", base.estJoignable());
    }

    public void arreter() {
        ordonnanceur.shutdownNow();
    }
}
