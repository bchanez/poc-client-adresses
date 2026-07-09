package com.exemple.client.e2e;

import com.exemple.client.support.PostgresContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base des tests E2E : serveur HTTP réel ({@code webEnvironment = RANDOM_PORT}) + vrai
 * PostgreSQL. On traverse toute la chaîne contrôleur → service → repository → base, comme
 * un vrai appel du front.
 *
 * <p>Comme en intégration : pas de transaction, donc chaque test nettoie la base dans son
 * {@code @BeforeEach} pour rester indépendant des autres.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractE2EIT {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", PostgresContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password", PostgresContainer.INSTANCE::getPassword);
    }
}
