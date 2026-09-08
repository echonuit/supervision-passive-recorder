# Module de télémétrie embarquée

Ce dossier contient le module qui se greffe sur le firmware existant du
Passive Recorder pour émettre les trames de télémétrie. Le firmware
d'acquisition lui-même n'est pas ici et n'est pas à modifier.

## Principe

La logique du module (quoi émettre, quand, comment sérialiser, quel
état) dépend de quatre interfaces déclarées dans `lib/telemetrie/ports.h` :
mesure de tension, état de la carte, horloge, radio. Elle se compile et
se teste sur un poste, sans matériel, avec l'environnement `native`.
Seuls les adaptateurs qui implémentent ces interfaces demandent la carte.

## Commandes

    pio test -e native          # tests unitaires sur le poste
    pio run -e teensy41         # compilation pour la cible

Les vecteurs de `../contrat/vecteurs.json` sont la référence pour le
codec, comme côté Java.
