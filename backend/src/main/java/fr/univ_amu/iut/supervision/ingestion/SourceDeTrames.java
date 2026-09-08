package fr.univ_amu.iut.supervision.ingestion;

import java.util.function.Consumer;

/**
 * Port d'entrée du système : d'où viennent les trames.
 *
 * <p>Deux implémentations sont attendues au minimum : l'une branchée sur le
 * réseau réel via Live Objects, l'autre sur le simulateur. Le reste du
 * système ne doit jamais savoir laquelle est en service.
 */
public interface SourceDeTrames {

    void demarrer(Consumer<TrameRecue> recepteur);

    void arreter();
}
