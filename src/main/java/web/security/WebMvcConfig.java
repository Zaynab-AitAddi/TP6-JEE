package web.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Enregistrement de l'intercepteur auprès de Spring MVC
 *
 * EQUIVALENT MVC2 :
 *   Dans MVC2, le filtre était déclaré dans web.xml :
 *     <filter>AuthenticationFilter</filter>
 *     <filter-mapping>/*</filter-mapping>
 *
 *   Dans Spring MVC, on enregistre l'intercepteur ici,
 *   et Spring l'applique automatiquement à toutes les requêtes.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor())
                .addPathPatterns("/**")           // s'applique à toutes les URLs
                .excludePathPatterns(             // sauf les ressources statiques
                        "/css/**", "/js/**", "/images/**", "/favicon.ico"
                );
    }
}
