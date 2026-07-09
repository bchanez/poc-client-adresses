package com.exemple.client.application.domain.views;

/**
 * Read model : un client vu « à plat », SANS ses adresses.
 *
 * <p>C'est la réponse à « et si ailleurs je veux le client sans ses adresses ? ». On ne
 * charge PAS l'agrégat pour l'amputer — on projette exactement les colonnes voulues via
 * une requête dédiée (voir {@code GetClientSummary}). Ce n'est pas un agrégat, ça ne porte
 * aucun invariant, c'est un DTO optimisé pour l'écran (CQRS read side, cf. note Obsidian
 * « CQRS — Séparer lecture et écriture »).
 */
public record ClientSummaryView(Long id, String nom) {
}
