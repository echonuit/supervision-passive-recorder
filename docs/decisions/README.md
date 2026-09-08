# Décisions d'architecture

Une note par décision, numérotée, jamais modifiée après coup : si une
décision est revue, on écrit une nouvelle note qui remplace l'ancienne.

Gabarit : `0000-gabarit.md`.

## Les notes

Numérotées dans l'ordre où les choix se posent quand on monte le projet :
avec quoi on construit, ce qu'on construit, comment on teste, comment on
fait tourner, comment on garde le tout propre.

| Note | Sujet |
|---|---|
| [0001](0001-java-et-maven.md) | Java 25 et Maven multi-modules, lancé par son wrapper |
| [0002](0002-javalin.md) | Javalin et la pile du backend |
| [0003](0003-platformio.md) | PlatformIO pour le module de télémétrie embarquée |
| [0004](0004-junit-assertj.md) | JUnit et AssertJ pour les tests |
| [0005](0005-testcontainers.md) | Testcontainers pour les tests d'intégration |
| [0006](0006-docker-compose.md) | Docker Compose pour les services du poste |
| [0007](0007-spotless.md) | Spotless et palantir-java-format, posés par un hook git |
| [0008](0008-github-actions.md) | GitHub Actions pour l'intégration continue |
| [0009](0009-dependabot.md) | Dependabot pour la mise à jour des dépendances |
| [0010](0010-pmd.md) | PMD pour l'analyse statique |
| [0011](0011-couverture.md) | Un plancher de couverture, tenu par JaCoCo |

## Notes reconstituées

Les notes 0001 et 0003 à 0009 portent la mention « reconstituée ». Elles
ont été écrites à l'import du dépôt, à partir de l'outillage déjà en
place : elles expliquent un choix, elles n'enregistrent pas la
délibération qui l'a produit. Leur date distingue les deux moments.
