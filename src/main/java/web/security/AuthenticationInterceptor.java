package web.security;

import dao.model.Role;
import dao.model.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Intercepteur d'authentification Spring MVC
 *
 * REMPLACEMENT MVC2 → Spring MVC :
 *   AuthenticationFilter (javax.servlet.Filter) → HandlerInterceptor (Spring)
 *
 * Avantages :
 *   - S'intègre naturellement dans le cycle Spring MVC
 *   - preHandle() déclenché AVANT chaque requête
 *   - On peut injecter des beans Spring si nécessaire (@Autowired)
 *   - Logique identique à AuthenticationFilter
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String path = request.getRequestURI()
                .substring(request.getContextPath().length());

        // 1. URLs publiques → laisser passer
        if (isPublicURL(path)) {
            return true;
        }

        // 2. Vérifier l'authentification
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;   // stoppe la requête
        }

        // 3. Vérifier les permissions selon le rôle
        if (!hasPermission(user.getRole(), path)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Accès refusé. Vous n'avez pas les permissions nécessaires.");
            return false;
        }

        return true;   // tout OK → continuer
    }

    private boolean isPublicURL(String path) {
        return path.endsWith(".css") || path.endsWith(".js")
                || path.endsWith(".png") || path.endsWith(".jpg")
                || path.endsWith(".ico") || path.endsWith(".woff")
                || path.endsWith(".woff2") || path.endsWith(".ttf")
                || path.equals("/login") || path.startsWith("/login")
                || path.equals("/signup") || path.startsWith("/signup");
    }

    private boolean hasPermission(Role role, String path) {
        if (role == Role.ADMIN) return true;

        String[] managerAdminOnly = {"/addProduit", "/editProduit", "/updateProduit", "/deleteProduit"};
        for (String url : managerAdminOnly) {
            if (path.contains(url)) {
                return role == Role.MANAGER || role == Role.ADMIN;
            }
        }
        return true;   // dashboard et les autres URLs sont accessibles à tous les connectés
    }
}
