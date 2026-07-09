package com.exemple.client.adapters.secondary.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Le {@code JpaRepository} Spring Data — package-private, confiné au package persistence.
 * Il ne sort jamais vers le domaine ; seul {@link SqlClientRepository} l'utilise et traduit.
 */
interface SpringDataClientRepository extends JpaRepository<ClientJpaEntity, Long> {

    /**
     * WRITE side : ramène l'agrégat entier en UNE requête ({@code join fetch}, donc pas de
     * N+1). {@code left join} pour qu'un client sans adresse remonte quand même.
     */
    @Query("select distinct c from ClientJpaEntity c left join fetch c.adresses where c.id = :id")
    Optional<ClientJpaEntity> findByIdAvecAdresses(@Param("id") Long id);

    /**
     * READ side : projette {@code id, nom} SANS toucher la table ADRESSE (aucune jointure).
     * SQL natif volontaire — heuristique Mihalcea : JPQL pour ce qu'on modifie, SQL pour les
     * lectures/projections (note Obsidian « CQRS »).
     */
    @Query(value = "select id, nom from client where id = :id", nativeQuery = true)
    Optional<ClientSummaryRow> findSummaryById(@Param("id") Long id);
}
