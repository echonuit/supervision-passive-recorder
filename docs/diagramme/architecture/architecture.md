![Architecture](https://kroki.io/plantuml/svg/eNplU01v2zAMvftXEL2sPTQDdgyGYMmybgGajzlBe8h6UGwm1WpLgSQHK7oCve6fzL_Df2wkbSdud3EiPorv8Yn0D9rslVM5JDbfW4MmLMNjhlDk2YfIH9G9Sh7UDmvMYRKU2WUYRU0czpa2cAl6SBFWVZlVZV6VwWk8g6cIYL1Q3usDQoyJdSm6iztQHhYxY0udF5kKWDi-fZXZEFBgAqLnDsfEbJ3ywRVJKBzCxK7g_Ip4jcaLhieuSo-qgGsbq9vhDOaOhNbVOCRSmGtrXY5wzZLmm5_Uj_9hzkfOPqCD6ffVqtbHeAPTzUUMl5cDqQP9loHiEhCkk97VvXz0ofpLfNTestijO2ivrYHz6gXSqjxgZvcUlR6o3vHe1KYF2T2hDnygC3WPANoEdFuViCXt304WKZ_UR0lfD1O1D7XB3Y6lRcGu53XixDDAeac3OWXxc3CafJp70Ov9Hrxia1P_R-TT8Ug8a-v0xXZJ4ctHiA99qIWxZ6nm6UNKfGavUhXURnlyYsRfMnhsjalK9GLDaDxuPFvHmOGBpgM2riBu8j2x9PF3NbxEz2_SHocZuoByEp6mD9HFRftQ_Umcljl0TWXOO24Rv1470p03F1VfzE4bbIXdVKXTW50cN8C_lvK1flajqHNI36k3yljNR5bVVKXJxHo_FO2RSQua7JNWT3NA7b9vqrwat5iJCO1M2nqs_P3GKpfCLW5kFOiXg116Zm_CHXraf0VMW9ln3ocks0V62gby4RcNr-GnenqzmDd6p_HzvXZWHFWGNAn7dPZtxrVaul5vIDHivVGZpnHgIVnRHpvoE3efZ_8Ar66giQ==)

```
skinparam componentStyle uml2
skinparam packageStyle rectangle

package "Sources de Télémétrie" {
  [Passive Recorder)] as PR
  [Simulateur de Flotte] as Sim
}

package "Infrastructure IoT (Fournie)" {
  [Réseau LoRaWAN Orange] as LoRa
  [Plateforme Live Objects\n(Broker MQTT)] as LiveObjects
  PR --> LoRa : LoRaWAN
  LoRa --> LiveObjects
}

package "Système de Supervision (À développer)" {

  package "Module Ingestion" {
    interface "Interface Ingestion" as IIngest
    [Adaptateur Live Objects] as AdaptLO
    [Injecteur Simulateur] as AdaptSim
    
    AdaptLO ..|> IIngest
    AdaptSim ..|> IIngest
    
    LiveObjects --> AdaptLO : MQTT
    Sim --> AdaptSim : Injection directe
  }

  database "Base de Données" as BDD {
    [Relevés bruts décodés]
    [Sessions]
    [Alertes]
  }

  IIngest --> BDD : Écriture relevés

  component "Moteur de Supervision" as Engine {
    [Vérificateur de sessions]
    [Gestionnaire d'alertes]
  }

  BDD <--> Engine : Lecture attendus\nÉcriture statuts/alertes

  package "Restitution" {
    [Dashboard Web] as WebDash
  }

  BDD --> WebDash : Lecture états/flotte
}

cloud "Systèmes Externes" {
  [Plateforme VigieChiro companion] as MNHN
}

WebDash ..> MNHN : Validation Token
@enduml
```