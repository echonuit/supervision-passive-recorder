# 0005. Testcontainers pour les tests d'intégration

Date : 2026-09-05 (décision) — note reconstituée le 2026-09-08
Statut : acceptée

Note : cette note a été écrite après coup, à partir de l'outillage déjà en
place. Elle reconstitue un raisonnement ; elle n'enregistre pas une
délibération.

## Contexte

Le backend parle à PostgreSQL, à MongoDB et à un courtier MQTT. Ce sont
exactement les endroits où les erreurs se logent : une requête SQL fausse,
un document mal formé, un sujet mal découpé. Un test qui remplace ces
services par des doublures ne dit rien de ces erreurs-là.

## Décision

Testcontainers : chaque test d'intégration démarre le vrai service dans
un conteneur, contre l'image réellement utilisée.

Les tests portent `@Testcontainers(disabledWithoutDocker = true)` : ils
s'ignorent d'eux-mêmes sur un poste sans Docker, et s'exécutent toujours
en CI.

## Options écartées

**H2 ou une base en mémoire** : ne parle pas le même SQL que PostgreSQL.
Un test vert sur H2 et rouge en production est le pire des deux mondes.

**Une base partagée, montée une fois pour toutes** : les tests se
marchent dessus, et l'ordre d'exécution finit par compter. Un conteneur
par classe de test repart d'un état propre.

**Des doublures pour tout** : ne teste que la doublure.

## Conséquences

Les tests d'intégration sont lents — quelques secondes chacun — et
demandent Docker. C'est le prix d'un test qui dit la vérité.

L'image doit être choisie explicitement, et ce choix a des conséquences :
MongoDB est épinglé en version 7 parce que la 8 refuse de démarrer sur
les noyaux Linux récents.

Un test ignoré faute de Docker reste vert dans le rapport. Ne pas
confondre « passé » et « exécuté » quand on lit un rapport local.

## Comment on saura qu'elle est tenue

Un test d'intégration doit échouer si on casse la requête qu'il exerce.
S'il reste vert, c'est qu'il ne touche pas le service qu'il prétend
tester.
