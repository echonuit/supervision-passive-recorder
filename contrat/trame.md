# Contrat de trame

*À rédiger par l'équipe et à geler avec l'encadrement. Ce fichier est la
source de vérité : le firmware et le serveur s'y conforment, et ne
divergent jamais l'un de l'autre sans une révision de ce document et des
vecteurs.*

## Version du contrat

| Version | Date | Changement |
|---|---|---|
| 0 | | Gabarit vide |

## Ce que la trame doit permettre de savoir

*Les questions auxquelles le serveur doit pouvoir répondre pour un
boîtier donné. Reprendre celles du sujet et compléter à partir de
l'entretien avec le commanditaire.*

## Cadencement des émissions

*Quand un boîtier parle, et combien de fois par jour.*

## Structure

*Octet par octet. Pour chaque champ : position, taille, type, unité,
échelle, valeur en cas d'indisponibilité.*

| Position | Taille | Champ | Type | Unité / échelle | Note |
|---|---|---|---|---|---|
| 0 | 1 | version | uint8 | | Doit rester en tête, quelle que soit la version |

## Justification des choix

*Au regard des quatre contraintes : budget d'octets, transmission non
fiable, boîtiers non reflashables une fois déposés, évolution du format.*

## Vecteurs de test

Voir `vecteurs.json`. Chaque vecteur donne une trame en hexadécimal et
les valeurs que le décodeur doit en tirer. Les cas limites y figurent
obligatoirement.
