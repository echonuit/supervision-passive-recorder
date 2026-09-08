#include <unity.h>
#include "telemetrie.h"

// Doublures minimales pour compiler la logique sans matériel.
struct TensionFixe : MesureTension {
    uint16_t valeur = 3900;
    uint16_t millivolts() override { return valeur; }
};
struct CarteFixe : EtatCarte {
    bool presente() override { return true; }
    uint32_t megaoctetsLibres() override { return 12000; }
};
struct HorlogeFixe : Horloge {
    uint32_t secondesDepuisEpoque() override { return 0; }
};
struct RadioMemoire : Radio {
    size_t dernierLongueur = 0;
    bool emettre(const uint8_t*, size_t longueur) override { dernierLongueur = longueur; return true; }
};

void test_la_logique_se_construit_sans_materiel() {
    TensionFixe t; CarteFixe c; HorlogeFixe h; RadioMemoire r;
    Telemetrie telemetrie(t, c, h, r);
    telemetrie.surActivation();
    TEST_ASSERT_TRUE(true);
}

int main() {
    UNITY_BEGIN();
    RUN_TEST(test_la_logique_se_construit_sans_materiel);
    return UNITY_END();
}
