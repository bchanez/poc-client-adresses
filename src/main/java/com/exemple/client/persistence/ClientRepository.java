package com.exemple.client.persistence;

import com.exemple.client.domain.Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Recherche par id EN RAMENANT les adresses en une seule requête ({@code join fetch}).
     * C'est la bonne façon de servir « client + ses adresses » : pas de lazy à
     * risque, pas de N+1. {@code left join} pour qu'un client SANS adresse remonte
     * quand même.
     */
    @Query("select distinct c from Client c left join fetch c.adresses where c.id = :id")
    Optional<Client> findByIdAvecAdresses(@Param("id") Long id);
}
