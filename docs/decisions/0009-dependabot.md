# 0009. Dependabot pour la mise à jour des dépendances

Date : 2026-09-05 (décision) — note reconstituée le 2026-09-08
Statut : acceptée

Note : cette note a été écrite après coup, à partir de l'outillage déjà en
place. Elle reconstitue un raisonnement ; elle n'enregistre pas une
délibération.

## Contexte

Un projet étudiant s'étale sur plusieurs mois. Sans rien, les versions se
figent au jour de l'import, et la mise à jour devient une corvée qu'on
repousse jusqu'à ce qu'elle soit un chantier.

## Décision

Dependabot, configuré dans `.github/dependabot.yml` pour deux écosystèmes,
Maven et les actions GitHub, en cadence mensuelle, avec au plus cinq
*pull requests* ouvertes à la fois et le préfixe de commit `chore(deps)`.

## Options écartées

**Renovate** : plus configurable, notamment pour regrouper les montées de
version. Cette souplesse est un coût de configuration que le projet n'a
pas à payer.

**Mettre à jour à la main, quand on y pense** : personne n'y pense.

**Une cadence hebdomadaire** : cinq PR de dépendances par semaine sur un
projet où l'équipe a déjà ses propres tâches, ce sont cinq PR qu'on
fusionne sans lire.

## Conséquences

La limite à cinq PR simultanées est ce qui rend le dispositif tenable :
il vaut mieux cinq montées relues qu'une file de trente ignorée.

Dependabot ne voit que les numéros de version. Il ne détecte pas un
artefact renommé ni un module scindé — deux cas rencontrés sur ce dépôt,
avec les coordonnées de Testcontainers et la scission de
`javalin-rendering`. Ces migrations-là restent manuelles.

Une PR de Dependabot passe la même CI que les autres. C'est elle qui dit
si la montée est sans danger, pas le numéro de version.

## Comment on saura qu'elle est tenue

Si le nombre de PR `chore(deps)` ouvertes atteint la limite et y reste,
c'est que personne ne les relit : le dispositif tourne à vide et il faut
soit rythmer la relecture, soit espacer la cadence.
