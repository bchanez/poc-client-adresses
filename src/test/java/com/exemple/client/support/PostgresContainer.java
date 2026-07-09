package com.exemple.client.support;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Le conteneur PostgreSQL partagé par TOUS les tests qui touchent la base (intégration et
 * e2e) : un vrai Postgres via Testcontainers, jamais H2. Singleton démarré une seule fois
 * et réutilisé — pas un conteneur par classe de test.
 */
public final class PostgresContainer {

    public static final PostgreSQLContainer<?> INSTANCE = new PostgreSQLContainer<>("postgres:16");

    static {
        INSTANCE.start();
    }

    private PostgresContainer() {
    }
}
