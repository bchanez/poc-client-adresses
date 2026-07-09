package com.exemple.client.application.domain.models;

/**
 * Une adresse d'un client — <b>Value Object</b> immuable, entièrement contenu dans
 * l'agrégat {@link Client}.
 *
 * <p>Aucune annotation JPA ici : c'est un objet <i>métier pur</i>. La table ADRESSE
 * et sa colonne CLIENT_ID vivent dans {@code AdresseJpaEntity}, côté persistence,
 * invisibles du domaine. Un VO stocké « avec » son entité hôte, immuable → pas de
 * cohérence à maintenir, pas d'id propre à gérer (cf. note Obsidian « Modeling
 * Relationships in a DDD Way » : les one-to-one/composés se modélisent en VO).
 */
public record Adresse(String rue, String ville) {

    public Adresse {
        if (rue == null || rue.isBlank()) {
            throw new IllegalArgumentException("rue obligatoire");
        }
        if (ville == null || ville.isBlank()) {
            throw new IllegalArgumentException("ville obligatoire");
        }
    }
}
