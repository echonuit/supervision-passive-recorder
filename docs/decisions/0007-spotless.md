# 0007. Spotless et palantir-java-format, posés par un hook git

Date : 2026-09-05 (décision) — note reconstituée le 2026-09-08
Statut : acceptée

Note : cette note a été écrite après coup, à partir de l'outillage déjà en
place. Elle reconstitue un raisonnement ; elle n'enregistre pas une
délibération.

## Contexte

À six sur un même dépôt, sans règle de formatage, les différences de
présentation finissent par polluer les revues : on discute d'accolades au
lieu de discuter du code, et les diffs mélangent les vrais changements
avec les reformatages de l'éditeur de chacun.

## Décision

Spotless avec `palantir-java-format`, un formateur qui ne se configure
pas. `spotless:check` est bloquant en `verify`, donc en CI.

Un hook `pre-commit`, installé automatiquement par
`git-build-hook-maven-plugin` au premier `./mvnw`, applique le formatage
aux fichiers Java stagés et les re-stage. Il ne bloque jamais un commit :
la forme n'est pas un motif de refus.

## Options écartées

**google-java-format** : équivalent, mais sa limite à 100 colonnes et son
traitement des chaînes longues rendent certaines lignes moins lisibles.
Palantir en dérive et corrige surtout cela.

**Checkstyle en vérification seule** : signale sans corriger. On passe son
temps à réparer à la main ce qu'un outil sait faire.

**Un formateur configuré finement** : chaque option est une discussion
sans fin, sans gain. L'intérêt d'un formateur imposé est justement qu'on
n'en parle plus.

**Un hook bloquant** : refuser un commit pour une accolade rend le hook
détesté, donc contourné par `--no-verify`.

## Conséquences

Le formatage n'est jamais un sujet de revue. `CONTRIBUTING.md` le dit
explicitement : ne le commentez pas.

Une limite connue du hook : un `git add -p` partiel est re-stagé en
entier. C'est écrit dans le hook lui-même.

Spotless analyse aussi le code de test : un test mal formé casse le
build comme le reste.

## Comment on saura qu'elle est tenue

`./mvnw spotless:check` doit être vert sur `main` à tout moment. Et une
revue qui contient un commentaire sur du formatage est le signe que
quelque chose s'est détraqué dans la chaîne.
