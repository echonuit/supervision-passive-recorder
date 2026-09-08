# 0001. Java 25 et Maven multi-modules, lancé par son wrapper

Date : 2026-09-05 (décision) — note reconstituée le 2026-09-08
Statut : acceptée

Note : cette note a été écrite après coup, à partir de l'outillage déjà en
place. Elle reconstitue un raisonnement ; elle n'enregistre pas une
délibération.

## Contexte

Le sujet impose un backend sur JVM. Restait à choisir comment on
construit, et comment on s'assure que les six postes de l'équipe, plus la
CI, construisent tous la même chose.

Le dépôt porte trois briques Java distinctes — le contrat de trame, le
simulateur, le backend — qui partagent des versions de dépendances et
doivent se compiler ensemble.

## Décision

Maven, en projet multi-modules : un `pom.xml` racine qui porte toutes les
versions dans ses `<properties>` et son `<dependencyManagement>`, et trois
modules qui n'y déclarent que ce dont ils ont besoin.

Java 25, fixé par `maven.compiler.release`, et la même version en CI.

Maven n'est pas à installer : `./mvnw` le télécharge à la première
exécution.

## Options écartées

**Gradle** : plus souple, mais son fichier de construction est un
programme. Pour une équipe qui découvre l'écosystème, un build qu'il faut
déboguer s'ajoute au reste plutôt que de l'alléger.

**Trois projets séparés** : obligerait à publier le contrat de trame dans
un dépôt d'artefacts pour que le simulateur et le backend le consomment,
ou à vivre avec des copies qui divergent.

**Compter sur un Maven installé par chacun** : la question « quelle
version de Maven as-tu ? » n'a plus à être posée avec le wrapper.

## Conséquences

Une version de dépendance se change à un seul endroit, le `pom.xml`
racine. Les modules ne portent pas de numéros de version.

Le premier `./mvnw` est lent : il télécharge Maven puis tout le reste.

Java 25 est récent. Les postes et la CI doivent l'avoir ; c'est une
contrainte assumée, pas un accident.

## Comment on saura qu'elle est tenue

`grep -c '<version>' */pom.xml` : un module qui se met à porter ses
propres versions de dépendances s'y verra. Et un `./mvnw verify` depuis
la racine construit les trois modules dans l'ordre.
