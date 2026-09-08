# 0004. JUnit et AssertJ pour les tests

Date : 2026-09-05 (décision) — note reconstituée le 2026-09-08
Statut : acceptée

Note : cette note a été écrite après coup, à partir de l'outillage déjà en
place. Elle reconstitue un raisonnement ; elle n'enregistre pas une
délibération.

## Contexte

Il fallait un cadre de test pour les trois modules Java, et une façon
d'écrire les assertions qui soit lisible par une équipe qui débute en
Java — y compris quand un test échoue, moment où le message compte plus
que la syntaxe.

## Décision

JUnit, importé par son BOM à la racine pour que tous les modules aient la
même version, et AssertJ pour les assertions.

Les tests paramétrés servent à jouer les vecteurs du contrat de trame :
un vecteur, un cas, un nom lisible dans le rapport.

## Options écartées

**TestNG** : équivalent sur le fond, mais JUnit est ce que l'équipe a
rencontré en cours et ce que la documentation de tout l'écosystème
suppose.

**Les assertions natives de JUnit seules** : `assertEquals(attendu, réel)`
oblige à se rappeler l'ordre des arguments, et son message d'échec dit
peu de chose sur une collection ou un objet composite.

**Hamcrest** : même intention qu'AssertJ, mais l'autocomplétion ne guide
pas — il faut connaître le nom du `Matcher` avant de pouvoir l'écrire.
AssertJ part de la valeur et propose la suite.

## Conséquences

Les deux styles coexistent aujourd'hui dans le dépôt : les tests du
contrat de trame utilisent les assertions de JUnit, ceux du backend et du
simulateur utilisent AssertJ. À uniformiser vers AssertJ quand on
repassera dessus.

AssertJ 3 reste la ligne stable ; sa version 4 n'existe qu'en jalons.

## Comment on saura qu'elle est tenue

Un test qui échoue doit dire ce qu'il attendait et ce qu'il a obtenu,
sans qu'on ait à ouvrir le code pour comprendre. C'est le seul critère
qui compte ici.
