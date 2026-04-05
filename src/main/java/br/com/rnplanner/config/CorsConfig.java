package br.com.rnplanner.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Libera para todos os endpoints da sua API
                .allowedOrigins(
                        "https://kind-meadow-0c396230f.6.azurestaticapps.net", // Seu Web App
                        "https://nice-smoke-03a138b0f.4.azurestaticapps.net",  // Seu Admin
                        "http://localhost:5173", // Libera o localhost do Vite para você testar na sua máquina!
                        "http://localhost:3000"  // Caso você use a porta 3000 também
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}