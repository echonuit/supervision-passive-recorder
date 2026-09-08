# 0003. PlatformIO pour le module de télémétrie embarquée

Date : 2026-09-05 (décision) — note reconstituée le 2026-09-08
Statut : acceptée

Note : cette note a été écrite après coup, à partir de l'outillage déjà en
place. Elle reconstitue un raisonnement ; elle n'enregistre pas une
délibération.

## Contexte

Le module de télémétrie se greffe sur le firmware existant du Passive
Recorder, sur une carte Teensy 4.1. Une équipe de six ne peut pas
travailler à six sur une seule carte, et le codec de trame doit être
validé contre les mêmes vecteurs que la version Java.

Il fallait donc pouvoir compiler et tester la logique embarquée sur un
poste ordinaire, sans matériel.

## Décision

PlatformIO, avec deux environnements dans `platformio.ini` :

- `native`, qui compile et teste sur le poste, avec Unity ;
- `teensy41`, qui compile pour la carte, avec le framework Arduino et
  RadioLib.

La logique ne dépend que de quatre interfaces (`lib/telemetrie/ports.h`) :
tension, état de la carte, horloge, radio. Seuls leurs adaptateurs
demandent le matériel.

## Options écartées

**L'IDE Arduino** : pas de gestion de dépendances déclarative, pas de
tests, pas d'exécution en CI. Rien de ce qu'on demande au reste du dépôt.

**Un Makefile et une chaîne de compilation montée à la main** : possible,
mais c'est un projet en soi, et il retomberait sur une seule personne.

**Tout tester sur la carte** : condamne l'équipe à se passer la carte, et
rend la CI aveugle sur le firmware.

## Conséquences

Le firmware a son propre cycle : il n'est pas construit par `./mvnw`.
C'est un dossier à part, avec ses propres commandes.

L'architecture en ports et adaptateurs n'est pas un ornement : c'est ce
qui rend l'environnement `native` possible. Une logique qui appellerait
directement la radio ne se testerait plus sur un poste.

## Comment on saura qu'elle est tenue

`pio test -e native` doit passer sans carte branchée. Le jour où ce n'est
plus vrai, c'est qu'une dépendance au matériel a fui dans la logique.
