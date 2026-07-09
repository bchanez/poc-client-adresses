package com.exemple.client.application.domain.models;

/**
 * Identité d'un {@link Client}, en Value Object plutôt qu'un {@code Long} nu.
 *
 * <p>Un {@code Long} qui traîne partout ne dit pas ce qu'il identifie : on peut le
 * confondre avec un id d'adresse, un montant, un index. Le VO rend le type parlant
 * et interdit les mélanges à la compilation (cf. note Obsidian
 * « Generic types are for arguments, specific types are for return values »).
 */
public record ClientId(Long value) {

    public ClientId {
        if (value == null) {
            throw new IllegalArgumentException("ClientId ne peut pas être null");
        }
    }
}
