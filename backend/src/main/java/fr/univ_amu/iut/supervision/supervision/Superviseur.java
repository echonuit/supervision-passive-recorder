package fr.univ_amu.iut.supervision.supervision;

import fr.univ_amu.iut.supervision.persistance.DataBase;
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

    private final DataBase base;
    private final ScheduledExecutorService ordonnanceur = Executors.newSingleThreadScheduledExecutor();

    public Superviseur(DataBase base) {
        this.base = base;
    }

    public void start() {
        ordonnanceur.scheduleAtFixedRate(this::evaluate, 0, 1, TimeUnit.MINUTES);
    }

    void evaluate() {
        LOG.debug("évaluation des boîtiers (base joignable : {})", base.isJoinable());
    }

    public void stop() {
        ordonnanceur.shutdownNow();
    }
}
