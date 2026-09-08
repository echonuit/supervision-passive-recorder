# 0008. GitHub Actions pour l'intégration continue

Date : 2026-09-05 (décision) — note reconstituée le 2026-09-08
Statut : acceptée

Note : cette note a été écrite après coup, à partir de l'outillage déjà en
place. Elle reconstitue un raisonnement ; elle n'enregistre pas une
délibération.

## Contexte

Les conventions du dépôt demandent qu'une *pull request* ne soit fusionnée
qu'avec une CI verte. Encore faut-il une CI, et qu'elle soit sur la même
plateforme que les *issues* et les *pull requests* : un aller-retour vers
un autre outil est un aller-retour qu'on cesse vite de faire.

## Décision

GitHub Actions, un seul workflow (`.github/workflows/ci.yml`), sur `push`
et sur `pull_request`.

Un seul pas utile : `./mvnw -B verify`. C'est **la même commande que sur
un poste**, et elle enchaîne compilation, tests, formatage, analyse
statique et plancher de couverture. Les rapports sont déposés en artefact
du job, y compris quand il échoue.

## Options écartées

**Jenkins ou GitLab CI** : demandent un serveur, ou un compte sur une
autre plateforme que celle où vit le code.

**Une liste de pas séparés — compiler, puis tester, puis analyser** :
donne un joli tableau vert, mais fait diverger la CI du poste. Le jour où
un pas manque en local, on ne le découvre qu'en PR.

**Ne lancer la CI que sur les *pull requests*** : une branche cassée reste
cassée sans le dire jusqu'à l'ouverture de la PR.

## Conséquences

Ce qui passe en local passe en CI, et réciproquement. Ajouter un garde au
build l'ajoute mécaniquement à la CI, sans toucher au workflow — c'est ce
qui s'est passé pour PMD et pour le plancher de couverture.

Contrepartie : `verify` est un tout. Un formatage manquant fait rougir la
CI au même titre qu'un test cassé, et le tableau ne dit pas lequel des
deux sans qu'on ouvre le journal.

Les tests d'intégration demandent Docker, disponible sur les *runners*
`ubuntu-latest`. C'est une dépendance à cet environnement.

## Comment on saura qu'elle est tenue

Le workflow ne doit pas accumuler de pas qui n'existent pas sur un poste.
Si la CI se met à faire des choses que `./mvnw verify` ne fait pas,
l'écart est à combler côté build, pas côté workflow.
