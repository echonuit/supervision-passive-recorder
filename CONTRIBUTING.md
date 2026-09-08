# Contribuer

## Mettre en place son poste

1. Java 25. Rien d'autre à installer : `./mvnw` télécharge Maven.
2. `./mvnw verify` une première fois. Ça compile, ça teste, et ça installe
   les hooks git du dépôt (formatage automatique au commit).
3. Pour le backend, les services locaux : `docker compose -f infra/docker-compose.yml up -d`
   si Docker est disponible, sinon une installation locale de PostgreSQL,
   MongoDB et Mosquitto avec les valeurs par défaut du `README`.

## Le cycle d'une tâche

1. Une *issue* par tâche, avec un critère de fin vérifiable, assignée à
   une personne, rattachée à un palier du plan.
2. Une branche par *issue* : `42-decodeur-trame`.
3. Des commits petits et réguliers.
4. Une *pull request* vers `main`, avec le gabarit rempli. Le titre suit la
   même convention que les commits : c'est lui qui devient le sujet du
   commit de fusion.
5. Une relecture par un autre membre de l'équipe. La relecture porte sur le
   fond : le formatage est déjà fait par le hook, ne le commentez pas.
6. Fusion quand la CI est verte et la relecture faite.

## Commits

Format `type(portée): sujet`, en français, sans espace avant les deux-points.

    feat(trame): champ tension batterie en millivolts
    fix(ingestion): trame dupliquée comptée deux fois
    test(supervision): fenêtre d'attente à cheval sur minuit
    docs(contrat): justification du budget d'octets
    chore(deps): bump jdbi 3.50

Types : `feat`, `fix`, `test`, `docs`, `refactor`, `chore`. La portée est
le module ou le domaine : `trame`, `simulateur`, `ingestion`,
`persistance`, `supervision`, `web`, `firmware`, `contrat`, `ci`.

## Code

- Formatage : Palantir Java Format, appliqué par le hook. Pour le lancer
  à la main : `./mvnw spotless:apply`.
- Noms de classes et de méthodes en français, sans accents dans les
  identifiants (`Superviseur`, `TrameRecue`, `estJoignable`).
- Pas de tiret cadratin dans le code, les commentaires ni la documentation :
  un tiret simple, deux-points ou une virgule.
- Un test par comportement, nommé par ce qu'il vérifie :
  `laRouteDeSanteRepond`, pas `test1`.

## Décisions

Tout choix d'architecture, de bibliothèque ou de format fait l'objet
d'une note numérotée dans `docs/decisions/`, à partir du gabarit. Une
note ne se modifie pas après coup : si la décision change, on en écrit
une nouvelle qui remplace l'ancienne.
