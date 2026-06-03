package fr.epsi.repository;

import fr.epsi.model.Article;

/**
 * Contrat pour la vérification du stock.
 * En production : interroge une base de données.
 * Dans les tests : on le mocke.
 */
public interface StockRepository {
    /**
     * Retourne le stock disponible pour un article donné.
     * @param article l'article recherché
     * @return la quantité en stock (0 si indisponible)
     */
    int getStock(Article article);
}