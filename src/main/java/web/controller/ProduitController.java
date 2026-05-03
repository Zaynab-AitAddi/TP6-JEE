package web.controller;

import dao.model.Produit;
import dao.model.Role;
import dao.model.User;
import metier.ProduitMetier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import util.ValidationUtil;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Controller de gestion des Produits
 *
 * REMPLACEMENT MVC2 → Spring MVC :
 *   DashboardServlet      → @GetMapping("/dashboard")
 *   AddProduitServlet     → @GetMapping("/addProduit")  + @PostMapping("/addProduit")
 *   EditProduitServlet    → @GetMapping("/editProduit")
 *   UpdateProduitServlet  → @PostMapping("/updateProduit")
 *   DeleteProduitServlet  → @GetMapping("/deleteProduit")
 *
 * Un seul fichier remplace 5 Servlets séparés.
 */
@Controller
public class ProduitController {

    @Autowired
    private ProduitMetier produitMetier;

    // ================================================================
    //  DASHBOARD — liste des produits + recherche
    //  AVANT : DashboardServlet.doGet()
    // ================================================================

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String success,
            @RequestParam(required = false) String error,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("currentUser");

        List<Produit> produits;
        if (search != null && !search.trim().isEmpty()) {
            produits = produitMetier.searchProduits(search);
        } else {
            produits = produitMetier.getAllProduits();
        }

        model.addAttribute("produits", produits);
        model.addAttribute("search", search);
        model.addAttribute("currentUser", currentUser);

        // Permissions selon le rôle (même logique que DashboardServlet)
        boolean isAdminOrManager = currentUser.getRole() == Role.ADMIN
                || currentUser.getRole() == Role.MANAGER;
        model.addAttribute("canAdd",    isAdminOrManager);
        model.addAttribute("canEdit",   isAdminOrManager);
        model.addAttribute("canDelete", isAdminOrManager);

        if (success != null) model.addAttribute("success", success);
        if (error   != null) model.addAttribute("error",   error);

        return "dashboard";   // → WEB-INF/views/dashboard.jsp
    }

    // ================================================================
    //  ADD PRODUIT
    //  AVANT : AddProduitServlet.doGet() + doPost()
    // ================================================================

    @GetMapping("/addProduit")
    public String showAddForm() {
        return "addProduit";   // → WEB-INF/views/addProduit.jsp
    }

    @PostMapping("/addProduit")
    public String processAdd(
            @RequestParam String nom,
            @RequestParam String description,
            @RequestParam String prix,
            @RequestParam String categorie,
            Model model) {

        if (ValidationUtil.isEmpty(nom) || ValidationUtil.isEmpty(description)
                || ValidationUtil.isEmpty(prix) || ValidationUtil.isEmpty(categorie)) {
            model.addAttribute("error", "Veuillez remplir tous les champs");
            return "addProduit";
        }

        if (!ValidationUtil.isValidPrice(prix)) {
            model.addAttribute("error", "Le prix doit etre un nombre positif");
            return "addProduit";
        }

        try {
            Produit produit = new Produit(nom, description, Double.parseDouble(prix), categorie);
            if (produitMetier.addProduit(produit)) {
                return "redirect:/dashboard?success=Produit ajoute avec succes";
            } else {
                model.addAttribute("error", "Erreur lors de l'ajout du produit");
                return "addProduit";
            }
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Format de prix invalide");
            return "addProduit";
        }
    }

    // ================================================================
    //  EDIT PRODUIT — affiche le formulaire pré-rempli
    //  AVANT : EditProduitServlet.doGet()
    // ================================================================

    @GetMapping("/editProduit")
    public String showEditForm(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String error,
            Model model) {

        if (id == null || id.isEmpty()) {
            return "redirect:/dashboard?error=ID produit manquant";
        }

        try {
            Produit produit = produitMetier.getProduitById(Long.parseLong(id));
            if (produit == null) {
                return "redirect:/dashboard?error=Produit non trouvé";
            }
            model.addAttribute("produit", produit);
            if (error != null) model.addAttribute("error", error);
            return "editProduit";   // → WEB-INF/views/editProduit.jsp
        } catch (NumberFormatException e) {
            return "redirect:/dashboard?error=ID produit invalide";
        }
    }

    // ================================================================
    //  UPDATE PRODUIT — traite la soumission du formulaire d'édition
    //  AVANT : UpdateProduitServlet.doPost()
    // ================================================================

    @PostMapping("/updateProduit")
    public String processUpdate(
            @RequestParam String id,
            @RequestParam String nom,
            @RequestParam String description,
            @RequestParam String prix,
            @RequestParam String categorie) {

        if (ValidationUtil.isEmpty(id) || ValidationUtil.isEmpty(nom)
                || ValidationUtil.isEmpty(description) || ValidationUtil.isEmpty(prix)
                || ValidationUtil.isEmpty(categorie)) {
            return "redirect:/dashboard?error=Tous les champs sont obligatoires";
        }

        try {
            Long produitId = Long.parseLong(id);
            Produit produit = produitMetier.getProduitById(produitId);

            if (produit == null) {
                return "redirect:/dashboard?error=Produit non trouve";
            }

            if (!ValidationUtil.isValidPrice(prix)) {
                return "redirect:/editProduit?id=" + produitId + "&error=Prix invalide";
            }

            produit.setNom(nom);
            produit.setDescription(description);
            produit.setPrix(Double.parseDouble(prix));
            produit.setCategorie(categorie);

            if (produitMetier.updateProduit(produit)) {
                return "redirect:/dashboard?success=Produit modifie avec succes";
            } else {
                return "redirect:/editProduit?id=" + produitId + "&error=Erreur lors de la modification";
            }
        } catch (NumberFormatException e) {
            return "redirect:/dashboard?error=Format invalide";
        }
    }

    // ================================================================
    //  DELETE PRODUIT
    //  AVANT : DeleteProduitServlet.doGet()
    // ================================================================

    @GetMapping("/deleteProduit")
    public String deleteProduit(@RequestParam(required = false) String id) {

        if (id == null || id.isEmpty()) {
            return "redirect:/dashboard?error=ID produit manquant";
        }

        try {
            if (produitMetier.deleteProduit(Long.parseLong(id))) {
                return "redirect:/dashboard?success=Produit supprime avec succes";
            } else {
                return "redirect:/dashboard?error=Erreur lors de la suppression";
            }
        } catch (NumberFormatException e) {
            return "redirect:/dashboard?error=ID produit invalide";
        }
    }
}
