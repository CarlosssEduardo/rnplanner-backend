package br.com.rnplanner.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // O segredo está aqui: permitimos qualquer subdomínio do vercel e qualquer porta do localhost
                .allowedOriginPatterns(
                        "https://rnplanner-*.vercel.app",
                        "http://localhost:[*]",
                        "http://127.0.0.1:[*]"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}