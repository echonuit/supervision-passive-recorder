package fr.univ_amu.iut.supervision.trame;

/**
 * Contenu décodé d'une trame de télémétrie.
 *
 * <p>Ce record est le reflet en Java du contrat décrit dans
 * {@code contrat/trame.md}. Il ne contient que le champ de version tant que
 * le contrat n'est pas gelé : c'est à l'équipe d'y ajouter les champs
 * décidés, puis d'adapter {@link TrameCodec} et les vecteurs de test.
 *
 * <p>Les valeurs sont brutes : aucune conversion d'unité n'a lieu ici.
 * Les échelles s'appliquent côté serveur, là où on peut les corriger sans
 * reflasher un boîtier.
 *
 * @param version numéro de version du format, toujours dans le premier octet
 */
public record Trame(int version) {

    public Trame {
        if (version < 0 || version > 255) {
            throw new IllegalArgumentException("version hors de l'octet : " + version);
        }
    }
}
