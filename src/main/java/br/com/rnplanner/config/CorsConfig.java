package br.com.rnplanner.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Libera todos os caminhos da API
                .allowedOrigins(
                        // 1. URLs Locais (Para você testar no seu PC)
                        "http://localhost:5173",
                        "http://localhost:3000",

                        // 2. URLs da Vercel (Substitua pelos links que a Vercel te der)
                        "https://rnplanner-admin.vercel.app",
                        "https://rnplanner-web.vercel.app"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}