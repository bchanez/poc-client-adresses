package com.exemple.client.adapters.secondary.persistence;

/**
 * Projection Spring Data pour le read model : Hibernate mappe les colonnes
 * {@code id} / {@code nom} du {@code SELECT} natif sur ces getters, sans charger
 * d'entité. Package-private : détail d'implémentation de l'adapter.
 */
interface ClientSummaryRow {

    Long getId();

    String getNom();
}
