# 0001. Javalin pour le backend

Date : 2026-09-05
Statut : acceptée

## Contexte

Le sujet impose un backend sur JVM, en continuité avec VigieChiro
Companion, et une architecture en interfaces et adaptateurs que
l'équipe doit pouvoir lire dans le code. L'équipe a peu pratiqué Java.

## Décision

Javalin, assemblé à la main dans `Application.main`, sans injection de
dépendances. JDBI pour la base relationnelle, le pilote MongoDB pour
l'archive, HiveMQ pour MQTT, `ScheduledExecutorService` pour la
supervision, JTE pour le rendu côté serveur.

## Options écartées

Spring Boot et Quarkus : l'auto-configuration masque l'architecture et
rend les erreurs illisibles pour qui maîtrise mal le langage. Micronaut :
même remarque, dans une moindre mesure. Kotlin et Ktor : deux inconnues
au lieu d'une.

## Conséquences

Chaque brique est choisie et branchée explicitement ; le coût d'entrée
est absorbé par ce dépôt de démarrage. Pas de front séparé : le tableau
de bord est rendu côté serveur.
