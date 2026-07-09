package com.exemple.client.persistence;

import com.exemple.client.domain.Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Recherche par id EN RAMENANT les adresses en une seule requête. Le fetch est piloté
     * par un {@link EntityGraph} plutôt qu'un {@code join fetch} écrit à la main : c'est la
     * façon JPA/Spring Data de déclarer « charge aussi cette association », plus lisible et
     * composable. Résultat identique — pas de lazy à risque, pas de N+1 — mais idiomatique.
     *
     * <p>Le {@code distinct} garde un seul {@code Client} en résultat malgré le left join
     * sur la collection. {@code findById} (hérité, sans graphe) reste lazy : c'est ce que
     * verrouille {@code ClientLazyLoadingIT}.
     */
    @EntityGraph(attributePaths = "adresses")
    @Query("select distinct c from Client c where c.id = :id")
    Optional<Client> findByIdAvecAdresses(@Param("id") Long id);
}
