
![Architecture](https://kroki.io/plantuml/svg/eNplVEFu2zAQvPMVixwCB2iQHnoq4CKpfSkQt67jOJdeVtLaZkKJDrk00CfkFz1G_QY_1qWo2HJ6MSBhZnZnZi117Rkdh9ooDGybUBfklNrJO13qHTYMZxPcMQUHo_ni4gzQw3xxCvjq7BM5mP1cLn81o1u9J_hRPFLJHq5gZv1z0Mw2czP2lH9Hbq9Lgm_Nhjxr23TI_HSKnNluk4rgLuwSy7-h87OqkLFAT7JU-hXg1DZNbMnn6dPpqeAU_baw6Cp4oKKDpDcKS7YOzu5ZG-0xu48vpTV2EygbuffvbcyNINfW1QQrvdE02WpnO-xqotR4fPQH57B0qJlqEuJ4rOYLuPzSZwOfIb7U2idrwA5F7tYu8OHmO4w4tia2dWzZabpQPUGoWVqoM_IeN9SVAaMd_jYWKyhc4IvkSu9lx7doe9KQPo1taaskINEZzPMHQAlQUHcY9rSR1AQWwJGhfWwHqFxGL2eoKbfZqaCcXusSUwiqovcLHTfs6-yFDoNv5aiCI_DUxeMBmampAqVLc_F1Y8j_R5ppuYT4Bx6t1CjRISdzXjqLrTSBhhwLbbBOPz11NpEpwXC3MRyvRTpLB5CGpJcy5aYs46sstKcSllJLMzgCGBXWPtXongwNe-hureOLzmoiKqthQsCdzhVUzmr2ShCXx4ErNLrKuD6Og1T2vaDnEP_KmINnKXRtrEQGo08f5W8sFyTIgeYiV-k_ZI4H4kM-WVywnXExvJY9t-lU8j0-pxbOQb4nHPgkzs7mdepJPjPqHzOgjos=)

```
@startuml
autonumber

participant "Capteur (PR)" as PR
participant "Broker MQTT\n(Live Objects / Mosquitto)" as Broker
participant "Service Ingestion" as Ingest
participant "Moteur de Supervision" as Superv
database "Base de Données" as BDD
participant "Dashboard Web" as Dash
actor "Utilisateur (Écologue)" as User
participant "Plateforme VigieChiro" as VC

== Ingestion & Traitement ==
PR -> Broker : Émission trame LoRaWAN (télémétrie)
Broker -> Ingest : Message MQTT (payload brut)
activate Ingest
Ingest -> Ingest : Décodage de la trame
Ingest -> BDD : Sauvegarde du relevé
Ingest -> Superv : Déclenchement vérification
deactivate Ingest

activate Superv
Superv -> BDD : Lecture sessions attendues / règles
Superv -> BDD : Mise à jour état de santé & alertes
deactivate Superv

== Consultation Dashboard ==
User -> Dash : Accès avec Token VigieChiro (bookmarklet)
activate Dash
Dash -> VC : Vérification token / droits
VC --> Dash : Validation session
Dash -> BDD : Requête état de la flotte (40 PR)
BDD --> Dash : Relevés, états et alertes
Dash --> User : Affichage métriques & statuts
deactivate Dash
@enduml
```