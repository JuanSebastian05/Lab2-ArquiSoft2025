package com.petstore.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.when;

class ProfileAwareRunnerTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private ProfileAwareRunner runner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("run() con perfil default imprime mensaje de desarrollo")
    void run_defaultProfile() throws Exception {
        when(environment.getActiveProfiles()).thenReturn(new String[]{});
        when(environment.getDefaultProfiles()).thenReturn(new String[]{"default"});

        runner.run();
        // Verifica que no lanza excepciones (output va a System.out)
    }

    @Test
    @DisplayName("run() con perfil dev imprime mensaje de desarrollo")
    void run_devProfile() throws Exception {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        when(environment.getDefaultProfiles()).thenReturn(new String[]{"default"});

        runner.run();
    }

    @Test
    @DisplayName("run() con perfil prod imprime mensaje de producción")
    void run_prodProfile() throws Exception {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getDefaultProfiles()).thenReturn(new String[]{"default"});

        runner.run();
    }

    @Test
    @DisplayName("run() con perfil test imprime mensaje custom")
    void run_testProfile() throws Exception {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        when(environment.getDefaultProfiles()).thenReturn(new String[]{"default"});

        runner.run();
    }

    @Test
    @DisplayName("run() con múltiples perfiles activos imprime correctamente")
    void run_multipleProfiles() throws Exception {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev", "custom"});
        when(environment.getDefaultProfiles()).thenReturn(new String[]{"default"});

        runner.run();
    }
}
