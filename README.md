# TP6 – Migration vers Spring MVC 5 (MVC2 → Spring MVC)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring](https://img.shields.io/badge/Spring_MVC-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)

> **Module :** Java EE – Développement Web Entreprise  
> **Étudiante :** Zaynab Ait Addi | **Encadrant :** Prof. Mohamed CHERRADI  
> **ENSAH – TDIA2 S4 | Année 2025-2026**

---

## 📋 Description

Migration complète de l'application de gestion de produits (TP5/MVC2) vers **Spring MVC 5**. Ce TP illustre comment le framework Spring remplace les Servlets manuelles par des **Controllers annotés**, les filtres HTTP par des **intercepteurs Spring**, et l'instanciation manuelle par l'**injection de dépendances** (`@Autowired`).

---

## 🎯 Objectifs

- Comprendre l'architecture Spring MVC et le rôle du `DispatcherServlet`
- Remplacer les Servlets Java EE par des `@Controller` annotés
- Remplacer les filtres HTTP par un `HandlerInterceptor` Spring
- Utiliser `@Autowired` pour injecter les services métier
- Configurer Spring MVC en Java pur (sans XML Spring)
- Conserver les couches DAO et métier de TP4/TP5 sans modification

---

## 🏗️ Architecture Spring MVC

```
TP6/
├── pom.xml
└── src/main/java/
    ├── dao/                         # ← Réutilisé de TP4/TP5 sans modification
    │   ├── interfaces/
    │   │   ├── IProduitDAO.java
    │   │   └── IUserDAO.java
    │   ├── impl/
    │   │   ├── ProduitDAOImpl.java
    │   │   └── UserDAOImpl.java
    │   └── model/
    │       ├── Produit.java
    │       ├── User.java
    │       └── Role.java
    ├── metier/                      # ← Réutilisé de TP4/TP5 sans modification
    │   ├── ProduitMetier.java
    │   └── UserMetier.java
    ├── util/
    │   ├── PasswordUtil.java
    │   └── ValidationUtil.java
    └── web/
        ├── controller/              # ← NOUVEAU : remplace les Servlets
        │   ├── AuthController.java       # Login + Signup + Logout
        │   └── ProduitController.java    # CRUD produits complet
        └── security/               # ← NOUVEAU : remplace les filtres
            ├── AuthenticationInterceptor.java
            └── WebMvcConfig.java
```

---

## 🔄 Correspondance MVC2 → Spring MVC

| Concept | MVC2 (TP5) | Spring MVC (TP6) |
|---------|-----------|-----------------|
| Point d'entrée | Servlet par Servlet dans `web.xml` | `DispatcherServlet` unique (front controller) |
| Contrôleur | `class LoginServlet extends HttpServlet` | `@Controller` + `@GetMapping`/`@PostMapping` |
| Récupérer un paramètre | `request.getParameter("email")` | `@RequestParam String email` |
| Passer données à la vue | `request.setAttribute("data", obj)` | `model.addAttribute("data", obj)` |
| Retourner une vue | `dispatcher.forward(req, resp)` | `return "nom-vue"` (ViewResolver) |
| Redirection | `response.sendRedirect("/url")` | `return "redirect:/url"` |
| Filtre de sécurité | `AuthenticationFilter implements Filter` | `AuthenticationInterceptor implements HandlerInterceptor` |
| Injection de service | `ProduitMetier.getInstance()` (Singleton manuel) | `@Autowired ProduitMetier produitMetier` |

---

## ✨ Controllers Spring MVC

### AuthController.java
Remplace **3 Servlets** (`LoginServlet`, `SignupServlet`, `LogoutServlet`) :

```java
@Controller
public class AuthController {

    @Autowired
    private UserMetier userMetier;

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute("currentUser") != null)
            return "redirect:/dashboard";
        return "login";  // → WEB-INF/views/login.jsp
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session, Model model) {
        User user = userMetier.authenticate(email, password);
        if (user != null) {
            session.setAttribute("currentUser", user);
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "Email ou mot de passe incorrect");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
```

### ProduitController.java
Remplace **5 Servlets** en un seul fichier :

| Ancienne Servlet | Nouvelle méthode | Annotation |
|------------------|-----------------|------------|
| `DashboardServlet.doGet()` | `dashboard()` | `@GetMapping("/dashboard")` |
| `AddProduitServlet.doGet()` | `showAddForm()` | `@GetMapping("/addProduit")` |
| `AddProduitServlet.doPost()` | `processAdd()` | `@PostMapping("/addProduit")` |
| `EditProduitServlet.doGet()` | `showEditForm()` | `@GetMapping("/editProduit")` |
| `UpdateProduitServlet.doPost()` | `processUpdate()` | `@PostMapping("/updateProduit")` |
| `DeleteProduitServlet.doGet()` | `deleteProduit()` | `@GetMapping("/deleteProduit")` |

---

## 🔐 Intercepteur Spring (AuthenticationInterceptor)

```java
public boolean preHandle(HttpServletRequest request,
                          HttpServletResponse response, Object handler)
        throws Exception {
    String uri = request.getRequestURI();
    // Laisser passer login, signup et ressources statiques
    if (uri.contains("/login") || uri.contains("/signup")) return true;

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("currentUser") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
    return true;
}
```

**Enregistrement (WebMvcConfig.java) :**
```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/favicon.ico");
    }
}
```

---

## 🌐 URLs de l'Application

| URL | Rôle requis | Description |
|-----|-------------|-------------|
| `/login` | Public | Connexion |
| `/signup` | Public | Inscription |
| `/logout` | Authentifié | Déconnexion |
| `/dashboard` | Tout rôle | Liste produits + recherche |
| `/addProduit` | ADMIN, MANAGER | Ajouter un produit |
| `/editProduit?id=X` | ADMIN, MANAGER | Modifier un produit |
| `/updateProduit` | ADMIN, MANAGER | Traitement modification (POST) |
| `/deleteProduit?id=X` | ADMIN, MANAGER | Supprimer un produit |

---

## 🚀 Installation et Exécution

### Prérequis

| Outil | Version |
|-------|---------|
| Java JDK | 11 |
| Apache Maven | 3.8.x |
| Apache Tomcat | 9.x |
| Spring MVC | 5.3.30 |

### Déploiement

```powershell
# Configuration environnement
$env:JAVA_HOME     = "C:\Program Files\Java\jdk-11"
$env:Path         += ";C:\Program Files\Java\jdk-11\bin"
$env:Path         += ";C:\apache-maven-3.9.x\bin"
$env:CATALINA_HOME = "C:\Tomcat"

# Build
cd C:\...\TP6
mvn clean package

# Déploiement
Copy-Item "target\gestion-produits-spring.war" "C:\Tomcat\webapps\" -Force
C:\Tomcat\bin\shutdown.bat
Start-Sleep -Seconds 3
C:\Tomcat\bin\startup.bat

# Accès
Start-Process "http://localhost:8080/gestion-produits-spring/login"
```

### Dépendances Maven clés

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>5.3.30</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>5.3.30</version>
</dependency>
```

---

## 🔑 Concepts Clés Spring MVC

| Annotation / Concept | Rôle |
|----------------------|------|
| `@Controller` | Indique que la classe gère des requêtes HTTP |
| `@GetMapping("/url")` | Route GET |
| `@PostMapping("/url")` | Route POST |
| `@RequestParam` | Injection des paramètres de formulaire |
| `Model` | Remplacement de `request.setAttribute()` |
| `ViewResolver` | Résolution `"login"` → `WEB-INF/views/login.jsp` |
| `@Autowired` | Injection de dépendances (IoC) |
| `@Configuration` | Classe de configuration Java |
| `HandlerInterceptor` | Équivalent Spring du filtre HTTP |
| `DispatcherServlet` | Front controller unique de Spring MVC |

---

## 🐛 Problèmes Courants et Solutions

| # | Problème | Cause | Solution |
|---|----------|-------|----------|
| 1 | Bean `UserMetier` non trouvé | Classe non annotée `@Service` | Ajouter `@Service` ou `@Component` |
| 2 | ViewResolver ne trouve pas les JSP | Préfixe/suffixe mal configuré | Configurer `/WEB-INF/views/` + `.jsp` |
| 3 | Intercepteur redirige en boucle | `/login` intercepté aussi | `excludePathPatterns("/login", "/signup")` |
| 4 | Caractères français mal affichés | `CharacterEncodingFilter` manquant | Ajouter filtre UTF-8 dans `web.xml` |
| 5 | 404 sur ressources statiques | `DispatcherServlet` intercepte tout | Configurer `DefaultServletHandler` |

---

## ⚠️ Notes Importantes

- Données stockées en **mémoire** (volatiles)
- Les couches **DAO et métier** sont réutilisées de TP4/TP5 **sans modification**
- Ce TP est la base pour **Spring Boot** (auto-configuration) et **Spring Security**
- Prochain niveau : Spring Boot + Spring Data JPA + base de données persistante

---

*TP6 – Java EE | ENSAH | TDIA2 S4 | © 2026 Zaynab AIT ADDI*
