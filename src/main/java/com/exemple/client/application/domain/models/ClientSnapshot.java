package com.exemple.client.application.domain.models;

import java.util.List;

/**
 * Photo plate et immuable de l'état d'un {@link Client} : l'UNIQUE point de passage
 * entre le domaine et la persistance.
 *
 * <p>Le mapper (côté adapter) construit un {@code ClientSnapshot} depuis l'entité JPA,
 * puis {@link Client#fromSnapshot} reconstitue l'agrégat — et inversement via
 * {@link Client#toSnapshot}. Résultat : l'agrégat n'expose pas un getter par champ
 * « pour la plomberie », et la traduction se fait à un seul endroit (Snapshot Pattern,
 * cf. note Obsidian « Domain Model vs Persistence Model »).
 */
public record ClientSnapshot(ClientId id, String nom, List<Adresse> adresses) {
}
