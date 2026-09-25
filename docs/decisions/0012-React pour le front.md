# 0012. React pour le front

Date : 2026-09-24
Statut : proposée
Remplace en partie : 0002 (rendu côté serveur du tableau de bord)

## Contexte

La note 0002 prévoyait un tableau de bord rendu côté serveur avec JTE,
sans front séparé. Le front (page de connexion, puis tableau de bord)
est confié à une personne différente de celle qui écrit le backend. Un
front indépendant permet de travailler en parallèle, à partir de la
maquette, sans attendre que les routes du backend existent. L'interface
comporte des interactions côté navigateur, comme la validation du token
de 24 caractères avant l'envoi.

## Décision

React, avec Vite, dans un dossier `frontend/` à la racine du dépôt. Ce
dossier n'est pas un module Maven : il a son propre cycle (npm).
Organisation : un dossier par page dans `frontend/src/pages/`
(`login`, `dashboard`), et `frontend/src/composants/` pour les éléments
partagés entre plusieurs pages. ESLint pour l'analyse et Prettier pour
le formatage, puisque Spotless ne couvre que le Java. En développement,
Vite redirige les appels `/api` vers le backend sur `localhost:7070`.
Le contrat des routes (URL, format des requêtes et des réponses) est
convenu avec la personne qui écrit le backend.

## Options écartées

Rester sur JTE : cohérent avec 0002 et sans outillage supplémentaire,
mais le front ne peut avancer qu'une fois le backend en place, et les
interactions côté navigateur restent limitées. Vue, Svelte ou Angular :
aucun avantage net pour ce projet, et React est celui que le porteur du
front maîtrise le mieux.

## Conséquences

Il faut Node.js en plus de Java 25 pour travailler sur le front. Le
backend doit exposer des routes qui répondent en JSON, et la question du
CORS ou du proxy en production reste à trancher. `./mvnw verify` ne
couvre pas le front : soit on ajoute une étape dans la CI (lint et
build), soit on documente que le front n'est pas vérifié
automatiquement. La mention "pas de front séparé" du `README` devient
fausse pour les pages concernées et doit être mise à jour. JTE peut être
conservé pour ce qui reste rendu côté serveur, ou retiré s'il n'a plus
d'usage.