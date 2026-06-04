package fr.epsi.service;

import fr.epsi.model.Article;
import fr.epsi.model.Panier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;

/**
 * Tests d'INTÉGRATION du CommandeService.
 *
 * Différence avec les tests unitaires :
 *   - On teste la chaîne complète de méthodes (pas une seule à la fois)
 *   - On simule des scénarios réels métier de bout en bout
 *   - Pas de mocks — on utilise les vraies implémentations
 *
 * Convention de nommage : *IT.java
 * Lancé par Maven Failsafe via : mvn verify
 *
 * ICDE848 – TP Jenkins
 */
class CommandeServiceIT {

    private final CommandeService service = new CommandeService();

    @Test
    @DisplayName("Pipeline complète : panier mixte → total → remise → catégorie")
    void pipelineComplete_PanierMixte_ResultatCoherent() {
        // GIVEN — scénario réaliste d'un client
        Panier panier = new Panier();
        panier.ajouter(new Article("Stylo",  2.0), 10); // 20€
        panier.ajouter(new Article("Cahier", 5.0),  4); // 20€
        // Total brut attendu = 40€

        // WHEN — enchaînement complet des opérations métier
        double total     = service.calculerTotal(panier);
        double apresRemise = service.appliquerRemise(total, 10);
        String categorie = service.categoriserCommande(apresRemise);

        // THEN — cohérence de bout en bout
        assertEquals(40.0,    total,       0.001, "Total brut incorrect");
        assertEquals(36.0,    apresRemise, 0.001, "Total après remise incorrect");
        assertEquals("PETITE", categorie,         "Catégorie incorrecte");
    }

    @Test
    @DisplayName("Commande premium : panier > 200€ → catégorie GRANDE")
    void pipelineComplete_PanierPremium_CategorieGrande() {
        // GIVEN
        Panier panier = new Panier();
        panier.ajouter(new Article("Ordinateur", 800.0), 1);
        panier.ajouter(new Article("Souris",      30.0), 2);

        // WHEN
        double total     = service.calculerTotal(panier);
        double apresRemise = service.appliquerRemise(total, 5);
        String categorie = service.categoriserCommande(apresRemise);

        // THEN
        assertEquals(860.0,   total,       0.001);
        assertEquals(817.0,   apresRemise, 0.001);
        assertEquals("GRANDE", categorie);
    }

    @Test
    @DisplayName("Remise 100% → total à zéro → catégorie PETITE")
    void pipelineComplete_RemiseTotale_TotalZero() {
        // GIVEN
        Panier panier = new Panier();
        panier.ajouter(new Article("Cadeau", 50.0), 1);

        // WHEN
        double total     = service.calculerTotal(panier);
        double apresRemise = service.appliquerRemise(total, 100);
        String categorie = service.categoriserCommande(apresRemise);

        // THEN
        assertEquals(0.0,    apresRemise, 0.001);
        assertEquals("PETITE", categorie);
    }
    // test integartion ajouter
    // panier vide
   @Test
    @DisplayName("Panier vide -> lève une exception")
    void pipelineComplete_PanierVide() {
        Panier panier = new Panier();
        assertThrows(
            IllegalArgumentException.class,
            () -> service.calculerTotal(panier)
        );
    }

    @Test
    @DisplayName("Montant exactement au seuil")
    void pipelineComplete_SeuilCategorie() {
        Panier panier = new Panier();
        panier.ajouter(new Article("Produit", 100.0), 1);

        double total = service.calculerTotal(panier);

        Assertions.assertEquals("MOYENNE",
                service.categoriserCommande(total));
    }

    @Test
    @DisplayName("Panier avec plusieurs articles")
    void pipelineComplete_MultiArticles() {
        Panier panier = new Panier();

        panier.ajouter(new Article("A", 10.0), 2);
        panier.ajouter(new Article("B", 20.0), 3);
        panier.ajouter(new Article("C", 5.0), 4);

        double total = service.calculerTotal(panier);

        Assertions.assertEquals(100.0, total, 0.001);
    }  

    @Test
    @DisplayName("Aucune remise appliquée")
    void pipelineComplete_SansRemise() {
        Panier panier = new Panier();
        panier.ajouter(new Article("Livre", 20.0), 5);

        double total = service.calculerTotal(panier);
        double apresRemise = service.appliquerRemise(total, 0);

        Assertions.assertEquals(100.0, total, 0.001);
        Assertions.assertEquals(100.0, apresRemise, 0.001);
    }
}
