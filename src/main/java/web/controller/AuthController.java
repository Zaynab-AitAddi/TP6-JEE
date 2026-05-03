package web.controller;

import dao.model.User;
import metier.UserMetier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import util.PasswordUtil;
import util.ValidationUtil;

import javax.servlet.http.HttpSession;

/**
 * Controller d'authentification
 *
 * REMPLACEMENT MVC2 → Spring MVC :
 *   LoginServlet   → @GetMapping("/login")  + @PostMapping("/login")
 *   SignupServlet  → @GetMapping("/signup") + @PostMapping("/signup")
 *   LogoutServlet  → @GetMapping("/logout")
 *
 * @Controller  : indique à Spring que cette classe gère des requêtes HTTP
 * @Autowired   : Spring injecte UserMetier automatiquement (plus de getInstance())
 * Model        : remplace req.setAttribute() pour envoyer des données à la JSP
 * String retour: le nom de la vue JSP à afficher (grâce au ViewResolver)
 */
@Controller
public class AuthController {

    @Autowired
    private UserMetier userMetier;

    // ================================================================
    //  LOGIN
    // ================================================================

    /**
     * Affiche la page de connexion
     * AVANT (MVC2) : doGet() dans LoginServlet
     */
    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        // Si déjà connecté → rediriger vers dashboard
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/dashboard";
        }
        return "login";   // → WEB-INF/views/login.jsp
    }

    /**
     * Traite le formulaire de connexion
     * AVANT (MVC2) : doPost() dans LoginServlet
     *
     * @RequestParam : récupère les paramètres du formulaire
     *                 (remplace req.getParameter())
     */
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        if (ValidationUtil.isEmpty(email) || ValidationUtil.isEmpty(password)) {
            model.addAttribute("error", "Veuillez remplir tous les champs");
            return "login";
        }

        User user = userMetier.authenticate(email, password);

        if (user != null) {
            session.setAttribute("currentUser", user);
            session.setMaxInactiveInterval(30 * 60);
            return "redirect:/dashboard";      // remplace resp.sendRedirect()
        } else {
            model.addAttribute("error", "Email ou mot de passe incorrect");
            return "login";
        }
    }

    // ================================================================
    //  LOGOUT
    // ================================================================

    /**
     * Déconnexion
     * AVANT (MVC2) : doGet() dans LogoutServlet
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ================================================================
    //  SIGNUP
    // ================================================================

    /**
     * Affiche le formulaire d'inscription
     * AVANT (MVC2) : doGet() dans SignupServlet
     */
    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup";   // → WEB-INF/views/signup.jsp
    }

    /**
     * Traite le formulaire d'inscription
     * AVANT (MVC2) : doPost() dans SignupServlet
     */
    @PostMapping("/signup")
    public String processSignup(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String nom,
            @RequestParam String prenom,
            Model model) {

        // Validations
        if (ValidationUtil.isEmpty(email) || ValidationUtil.isEmpty(password)
                || ValidationUtil.isEmpty(confirmPassword)
                || ValidationUtil.isEmpty(nom) || ValidationUtil.isEmpty(prenom)) {
            model.addAttribute("error", "Veuillez remplir tous les champs");
            return "signup";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Les mots de passe ne correspondent pas");
            return "signup";
        }

        if (!PasswordUtil.isStrongPassword(password)) {
            model.addAttribute("error",
                    "Le mot de passe doit contenir au moins 8 caractères, 1 majuscule, 1 minuscule et 1 chiffre");
            return "signup";
        }

        if (!ValidationUtil.isValidEmail(email)) {
            model.addAttribute("error", "Veuillez entrer une adresse email valide");
            return "signup";
        }

        if (userMetier.signUp(email, password, nom, prenom)) {
            model.addAttribute("success", "Inscription reussie ! Veuillez vous connecter.");
            return "signup";
        } else {
            if (userMetier.getUserByEmail(email) != null) {
                model.addAttribute("error", "Cet email est deja utilise");
            } else {
                model.addAttribute("error", "Erreur lors de l'inscription. Veuillez reessayer.");
            }
            return "signup";
        }
    }
}
