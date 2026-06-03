package fr.epsi.service;

import fr.epsi.repository.StockRepository;
import fr.epsi.model.Article;
import fr.epsi.model.Panier;
import java.util.Map;

/**
 * Service métier de gestion des commandes.
 * ICDE848 – TP Jenkins
 */
public class CommandeService {

    /**
     * Calcule le total d'un panier.
     *
     * @param panier le panier à calculer
     * @return le montant total en euros
     * @throws IllegalArgumentException si le panier est null ou vide
     */
    public double calculerTotal(Panier panier) {
        if (panier == null || panier.estVide()) {
            throw new IllegalArgumentException("Panier vide ou null");
        }
        double total = 0;
        for (Map.Entry<Article, Integer> entry : panier.getArticles().entrySet()) {
            total += entry.getKey().getPrix() * entry.getValue();
        }
        return total;
    }

    /**
     * Applique une remise en pourcentage sur un total.
     *
     * @param total      le montant brut
     * @param pourcentage la remise entre 0 et 100
     * @return le montant après remise
     * @throws IllegalArgumentException si le pourcentage est invalide
     */
    public double appliquerRemise(double total, int pourcentage) {
        if (pourcentage < 0 || pourcentage > 100) {
            throw new IllegalArgumentException("Remise invalide : " + pourcentage);
        }
        return total * (1 - pourcentage / 100.0);
    }

    /**
     * Catégorise une commande selon son montant.
     *
     * @param total le montant de la commande
     * @return "PETITE" < 50€, "MOYENNE" < 200€, "GRANDE" sinon
     */
    public String categoriserCommande(double total) {
        if (total < 50)       return "PETITE";
        else if (total < 200) return "MOYENNE";
        else                  return "GRANDE";
    }

    /**
     * Calcule la TVA à 20% sur un montant donné.
     *
     * @param montant le montant HT
     * @return la TVA arrondie à 2 décimales
     * @throws IllegalArgumentException si le montant est négatif
     */
    public double calculerTVA(double montant) {
        if (montant < 0) {
            throw new IllegalArgumentException("Montant négatif : " + montant);
        }
        double tva = montant * 0.20;
        return Math.round(tva * 100.0) / 100.0;
    }

    // Ajouter en attribut de classe
    private StockRepository stockRepository;

    // Constructeur sans dépendance (comportement existant inchangé)
    public CommandeService() {
        this.stockRepository = null;
    }

    // Nouveau constructeur avec injection de dépendance
    public CommandeService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * Vérifie si la commande est réalisable selon le stock disponible.
     *
     * @param article   l'article à commander
     * @param quantite  la quantité demandée
     * @return true si le stock est suffisant
     * @throws IllegalStateException si aucun StockRepository n'est configuré
     */
    public boolean commandeRealisable(Article article, int quantite) {
        if (stockRepository == null) {
            throw new IllegalStateException("StockRepository non configuré");
        }
        int stockDisponible = stockRepository.getStock(article);
        return stockDisponible >= quantite;
    }
}
