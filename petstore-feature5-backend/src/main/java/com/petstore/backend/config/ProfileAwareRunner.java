package com.petstore.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ProfileAwareRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProfileAwareRunner.class);
    
    private final Environment environment;

    public ProfileAwareRunner(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();
        
        // Debug logging
        log.debug("Active profiles: {}", Arrays.toString(activeProfiles));
        log.debug("Default profiles: {}", Arrays.toString(defaultProfiles));
        
        log.info("============================================");
        log.info("PETSTORE BACKEND - PERFIL DE SEGURIDAD");
        log.info("============================================");
        
        if (activeProfiles.length == 0) {
            log.info("✓ Perfiles activos: {} (default)", Arrays.toString(defaultProfiles));
            log.info("✓ Modo: DESARROLLO");
            log.info("✓ GraphiQL: http://localhost:8080/graphiql");
            log.info("✓ GraphQL: http://localhost:8080/graphql (PÚBLICO)");
            log.info("✓ H2 Console: Habilitado");
            log.info("✓ Actuator: Todos los endpoints");
        } else {
            log.info("✓ Perfiles activos: {}", Arrays.toString(activeProfiles));
            
            boolean isDev = Arrays.asList(activeProfiles).contains("dev");
            boolean isProd = Arrays.asList(activeProfiles).contains("prod");
            
            if (isDev) {
                log.info("✓ Modo: DESARROLLO");
                log.info("✓ GraphiQL: http://localhost:8080/graphiql");
                log.info("✓ GraphQL: http://localhost:8080/graphql (PÚBLICO)");
                log.info("✓ H2 Console: Habilitado");
                log.info("✓ Actuator: Todos los endpoints");
            } else if (isProd) {
                log.info("✓ Modo: PRODUCCIÓN");
                log.info("✓ GraphiQL: DESHABILITADO");
                log.info("✓ GraphQL: http://localhost:8080/graphql (REQUIERE JWT)");
                log.info("✓ H2 Console: DESHABILITADO");
                log.info("✓ Actuator: Solo /health público");
            } else {
                log.info("✓ Modo: DESARROLLO (perfil custom)");
            }
        }
        
        log.info("============================================");
        log.info("Para usar GraphQL en producción:");
        log.info("  1. POST /api/auth/login");
        log.info("  2. Usar token en header: Authorization: Bearer <token>");
        log.info("============================================");
    }
}
