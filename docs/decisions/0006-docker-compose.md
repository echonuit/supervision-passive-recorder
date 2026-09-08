# 0006. Docker Compose pour les services du poste de développement

Date : 2026-09-05 (décision) — note reconstituée le 2026-09-08
Statut : acceptée

Note : cette note a été écrite après coup, à partir de l'outillage déjà en
place. Elle reconstitue un raisonnement ; elle n'enregistre pas une
délibération.

## Contexte

Faire tourner le backend demande trois services : PostgreSQL, MongoDB et
un courtier MQTT. Demander à six personnes de les installer à la main sur
leur machine, c'est six installations différentes, six versions
différentes, et des heures perdues à comprendre pourquoi ça marche chez
l'un et pas chez l'autre.

## Décision

Un `infra/docker-compose.yml` qui démarre les trois services avec les
versions et les identifiants attendus par les valeurs par défaut de la
configuration du backend. Une commande, et le poste est prêt.

Les images sont épinglées sur une ligne de version, pas sur `latest`.

## Options écartées

**Une installation locale de chaque service** : possible, et le README
laisse la porte ouverte à qui préfère. Mais ce ne peut pas être le chemin
par défaut.

**Des services partagés, hébergés quelque part** : deux personnes qui
travaillent en même temps se marchent dessus, et il faut un accès réseau
pour développer.

**`latest` sur les images** : le jour où une image majeure sort, l'équipe
découvre la panne sans avoir rien changé.

## Conséquences

Docker devient un prérequis de fait pour travailler sur le backend — et
il l'est déjà pour les tests d'intégration.

Les identifiants sont en clair dans le fichier. C'est délibéré : ce sont
des services locaux jetables, pas un environnement de production. Aucun
secret réel ne doit y entrer.

Le choix des versions n'est pas neutre : MongoDB est en 7 et non en 8,
parce que la 8 refuse de démarrer sur les noyaux Linux 6.19 et suivants.
La raison est écrite dans le fichier, à côté de la ligne.

## Comment on saura qu'elle est tenue

Sur un poste neuf : `docker compose -f infra/docker-compose.yml up -d`
puis `./mvnw verify` doit suffire, sans autre installation que Java et
Docker.
