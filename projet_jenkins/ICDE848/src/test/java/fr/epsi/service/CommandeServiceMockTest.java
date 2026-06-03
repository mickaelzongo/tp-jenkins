package fr.epsi.service;

import fr.epsi.model.Article;
import fr.epsi.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires avec Mockito.
 * @Mock crée un objet simulé de StockRepository.
 * On contrôle ce qu'il retourne sans toucher à une vraie base de données.
 */
@ExtendWith(MockitoExtension.class)
class CommandeServiceMockTest {

    @Mock
    private StockRepository stockRepository;   // Mock créé par Mockito

    private CommandeService service;
    private Article article;

    @BeforeEach
    void setUp() {
        // Injecter le mock dans le service
        service = new CommandeService(stockRepository);
        article = new Article("Stylo", 2.0);
    }

    @Test
    @DisplayName("Stock suffisant → commande réalisable")
    void commandeRealisable_StockSuffisant_RetourneTrue() {
        // GIVEN — le mock retourne 10 quand on lui demande le stock de "Stylo"
        when(stockRepository.getStock(article)).thenReturn(10);

        // WHEN
        boolean resultat = service.commandeRealisable(article, 5);

        // THEN
        assertTrue(resultat, "La commande devrait être réalisable (stock=10, demande=5)");

        // Vérifier que le mock a bien été appelé exactement une fois
        verify(stockRepository, times(1)).getStock(article);
    }

    @Test
    @DisplayName("Stock insuffisant → commande non réalisable")
    void commandeRealisable_StockInsuffisant_RetourneFalse() {
        // GIVEN — stock de 2 seulement
        when(stockRepository.getStock(article)).thenReturn(2);

        // WHEN
        boolean resultat = service.commandeRealisable(article, 5);

        // THEN
        assertFalse(resultat, "La commande ne devrait pas être réalisable (stock=2, demande=5)");
    }

    @Test
    @DisplayName("Stock exactement égal à la demande → réalisable")
    void commandeRealisable_StockEgalDemande_RetourneTrue() {
        // GIVEN — stock = quantité demandée exactement (frontière)
        when(stockRepository.getStock(article)).thenReturn(5);

        // WHEN
        boolean resultat = service.commandeRealisable(article, 5);

        // THEN
        assertTrue(resultat, "Commande égale au stock devrait être réalisable");
    }

    @Test
    @DisplayName("Stock à zéro → commande non réalisable")
    void commandeRealisable_StockZero_RetourneFalse() {
        // GIVEN — article en rupture de stock
        when(stockRepository.getStock(article)).thenReturn(0);

        // WHEN
        boolean resultat = service.commandeRealisable(article, 1);

        // THEN
        assertFalse(resultat, "Stock à zéro : commande non réalisable");
    }

    @Test
    @DisplayName("Sans StockRepository → IllegalStateException")
    void commandeRealisable_SansRepository_LeveException() {
        // GIVEN — service créé sans repository
        CommandeService serviceNonConfigure = new CommandeService();

        // WHEN + THEN
        assertThrows(
            IllegalStateException.class,
            () -> serviceNonConfigure.commandeRealisable(article, 1)
        );
    }
}
