package com.exemple.client.integration;

import com.exemple.client.support.PostgresContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base des tests d'INTÉGRATION : contexte Spring complet ({@code @SpringBootTest}) branché sur
 * le vrai PostgreSQL du {@link PostgresContainer}. On teste ici les adapters (repository,
 * service) contre la base, pas le HTTP.
 */
@SpringBootTest
public abstract class AbstractIntegrationIT {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", PostgresContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password", PostgresContainer.INSTANCE::getPassword);
    }
}
