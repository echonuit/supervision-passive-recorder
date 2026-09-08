#pragma once
#include <cstdint>
#include <cstddef>

// Interfaces dont dépend la logique de télémétrie.
// Les implémentations réelles vivent dans les adaptateurs ; les tests
// natifs fournissent des doublures.

struct MesureTension {
    virtual ~MesureTension() = default;
    virtual uint16_t millivolts() = 0;
};

struct EtatCarte {
    virtual ~EtatCarte() = default;
    virtual bool presente() = 0;
    virtual uint32_t megaoctetsLibres() = 0;
};

struct Horloge {
    virtual ~Horloge() = default;
    virtual uint32_t secondesDepuisEpoque() = 0;
};

struct Radio {
    virtual ~Radio() = default;
    // Retourne vrai si l'émission a été acceptée par la pile radio.
    virtual bool emettre(const uint8_t* octets, size_t longueur) = 0;
};
