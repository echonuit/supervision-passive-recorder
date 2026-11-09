package fr.univ_amu.iut.supervision.capteurs;

import java.util.List;

/**
 * Port : d'où viennent les capteurs et leurs positions.
 *
 * <p>Aujourd'hui {@link CatalogueDemo}. Quand le référentiel de la campagne sera en base, une
 * implémentation lira les boîtiers, leurs déploiements et les coordonnées de leurs sites, et
 * remplacera la démo dans {@code Application}. La route et le front n'auront pas à changer.
 */
@FunctionalInterface
public interface CatalogueCapteurs {

    List<CapteurPosition> lister();
}
