package metier;

import dao.interfaces.IUserDAO;
import dao.model.Role;
import dao.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import util.PasswordUtil;
import util.ValidationUtil;

import java.util.List;

/**
 * Service métier pour l'authentification et la gestion des utilisateurs
 *
 * CHANGEMENTS MVC2 → Spring MVC :
 *   @Service  + @Autowired remplacent le singleton manuel
 *   La logique est identique.
 */
@Service                              // ← NOUVEAU
public class UserMetier {

    @Autowired                        // ← NOUVEAU
    private IUserDAO dao;

    public User authenticate(String email, String password) {
        try {
            if (ValidationUtil.isEmpty(email) || ValidationUtil.isEmpty(password)) return null;
            User user = dao.getUserByEmail(email);
            if (user == null || !user.isActif()) return null;
            return PasswordUtil.verifyPassword(password, user.getPassword()) ? user : null;
        } catch (Exception e) {
            System.err.println("Erreur authenticate : " + e.getMessage());
            return null;
        }
    }

    public boolean signUp(String email, String password, String nom, String prenom) {
        try {
            if (!ValidationUtil.isValidEmail(email)) return false;
            if (!ValidationUtil.isValidName(nom) || !ValidationUtil.isValidName(prenom)) return false;
            if (!PasswordUtil.isStrongPassword(password)) return false;
            if (dao.userExists(email)) return false;

            User newUser = new User(email, PasswordUtil.hashPassword(password), nom, prenom);
            newUser.setRole(Role.USER);
            dao.addUser(newUser);
            return true;
        } catch (Exception e) {
            System.err.println("Erreur signUp : " + e.getMessage());
            return false;
        }
    }

    public User getUserByEmail(String email) {
        try {
            if (ValidationUtil.isEmpty(email)) return null;
            return dao.getUserByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }

    public User getUserById(Long id) {
        try {
            if (id == null || id <= 0) return null;
            return dao.getUserById(id);
        } catch (Exception e) {
            return null;
        }
    }

    public List<User> getAllUsers() {
        try {
            return dao.getAllUsers();
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean updateUser(User user) {
        try {
            if (user == null || user.getIdUser() == null) return false;
            dao.updateUser(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteUser(Long id) {
        try {
            if (id == null || id <= 0) return false;
            dao.deleteUser(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean changeUserRole(Long userId, Role newRole) {
        try {
            User user = getUserById(userId);
            if (user == null) return false;
            user.setRole(newRole);
            return updateUser(user);
        } catch (Exception e) {
            return false;
        }
    }
}
