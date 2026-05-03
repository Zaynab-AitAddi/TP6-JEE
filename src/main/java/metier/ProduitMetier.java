package metier;

import dao.interfaces.IProduitDAO;
import dao.model.Produit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service métier pour les Produits
 *
 * CHANGEMENTS MVC2 → Spring MVC :
 *   AVANT  : ProduitMetier.getInstance()  — singleton manuel
 *   APRÈS  : @Service + @Autowired        — Spring gère l'injection
 *
 *   @Service  : déclare ce bean comme couche service
 *   @Autowired : Spring injecte automatiquement ProduitDAOImpl
 *                (plus besoin de new ProduitDAOImpl() dans le constructeur)
 *
 * La logique métier (validations, gestion des erreurs) est identique à MVC2.
 */
@Service                              // ← NOUVEAU : remplace le singleton manuel
public class ProduitMetier {

    @Autowired                        // ← NOUVEAU : injection automatique du DAO
    private IProduitDAO dao;

    // Plus de constructeur privé ni de getInstance()

    public boolean addProduit(Produit produit) {
        try {
            if (produit == null || produit.getNom() == null || produit.getPrix() == null)
                return false;
            dao.addProduit(produit);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur addProduit : " + e.getMessage());
            return false;
        }
    }

    public boolean deleteProduit(Long id) {
        try {
            if (id == null || id <= 0) return false;
            dao.deleteProduit(id);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur deleteProduit : " + e.getMessage());
            return false;
        }
    }

    public Produit getProduitById(Long id) {
        try {
            if (id == null || id <= 0) return null;
            return dao.getProduitById(id);
        } catch (Exception e) {
            System.err.println("Erreur getProduitById : " + e.getMessage());
            return null;
        }
    }

    public List<Produit> getAllProduits() {
        try {
            return dao.getAllProduits();
        } catch (Exception e) {
            System.err.println("Erreur getAllProduits : " + e.getMessage());
            return List.of();
        }
    }

    public boolean updateProduit(Produit produit) {
        try {
            if (produit == null || produit.getIdProduit() == null) return false;
            dao.updateProduit(produit);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur updateProduit : " + e.getMessage());
            return false;
        }
    }

    public List<Produit> searchProduits(String keyword) {
        try {
            return dao.searchProduits(keyword);
        } catch (Exception e) {
            System.err.println("Erreur searchProduits : " + e.getMessage());
            return List.of();
        }
    }
}
