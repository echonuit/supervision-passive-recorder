#pragma once
#include "ports.h"

// Logique de télémétrie. À écrire par l'équipe une fois le contrat gelé.
class Telemetrie {
public:
    Telemetrie(MesureTension& tension, EtatCarte& carte, Horloge& horloge, Radio& radio)
        : tension_(tension), carte_(carte), horloge_(horloge), radio_(radio) {}

    // Appelée au démarrage d'une session d'enregistrement.
    void surActivation();

    // Appelée à la fin d'une session d'enregistrement.
    void surDesactivation();

private:
    MesureTension& tension_;
    EtatCarte& carte_;
    Horloge& horloge_;
    Radio& radio_;
};
