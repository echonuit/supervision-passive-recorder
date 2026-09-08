# 0011. Un plancher de couverture, tenu par JaCoCo

Date : 2026-09-08
Statut : acceptée

## Contexte

JaCoCo était déjà branché, mais ne produisait qu'un rapport. Personne
n'ouvre un rapport que rien n'oblige à ouvrir : la couverture pouvait
descendre indéfiniment sans que quiconque le remarque.

Le sujet demande un système dont on puisse démontrer le bon
fonctionnement. Une couverture qui s'effrite pendant six mois de projet
se rattrape mal en fin de parcours.

## Décision

`jacoco:check` bloquant dans `./mvnw verify`, avec deux limites par
module : **80 % de lignes** et **50 % de branches**.

Les deux chiffres sont posés sous une mesure réelle du 2026-09-08, pas
choisis pour faire joli :

| Module | Lignes | Branches |
|---|---|---|
| backend | 82,8 % | 55,6 % |
| simulateur | 87,0 % | 87,5 % |
| trame | 85,7 % | 60,0 % |

C'est un plancher, pas une cible : il empêche de régresser, il ne
récompense pas d'avancer.

## Options écartées

**Ne rien imposer, garder le rapport seul** : l'état précédent. Aucun
signal, donc aucune prise.

**80 % sur les branches aussi** : aurait fait rougir la CI dès le premier
lancement, les branches étant mesurées entre 55 et 60 % sur deux modules.
Un portail qu'on doit désactiver le jour où on le pose n'apprend rien.

**Un seuil par module** : plus juste — trame à 85 % ne devrait pas avoir
le droit de retomber à 80 — mais trois jeux de chiffres à tenir à jour
pour un dépôt de cette taille. À reprendre si les modules divergent.

**Un objectif à 100 %** : pousse à écrire des tests qui exécutent du code
sans rien vérifier, ce qui est pire que pas de test du tout, parce que
ça ment.

## Conséquences

La marge est mince côté backend : 2,8 points. Elle a une cause unique et
connue, `Application.main`, qui câble tout le système et démarre le
serveur. Ses 16 lignes ne sont pas testables unitairement et pèsent 16 %
du module. Si le seuil devient pénible en revue, la bonne réponse est
d'exclure `Application` du calcul, pas de baisser le chiffre.

Un piège à connaître : **un module sans aucun test ne produit pas de
`jacoco.exec`, et la vérification y passe au vert sans rien mesurer.**
C'était le cas du simulateur jusqu'à ce jour. Les trois modules ont
maintenant des tests, mais un quatrième qui arriverait sans tests ne
serait pas signalé.

Le seuil se relève quand la couverture monte. Il ne se baisse pas quand
elle descend : c'est ce qui le distingue d'une décoration.

## Comment on saura qu'elle est tenue

Pousser une limite au-dessus de la couverture mesurée et lancer
`./mvnw verify` : le build doit échouer en nommant le module et le ratio
constaté. Vérifié à la mise en place, à 95 % de lignes — l'échec cite
bien `Rule violated for bundle trame`.

Pour re-mesurer avant de toucher aux chiffres : `./mvnw verify`, puis
`*/target/site/jacoco/jacoco.csv`.
