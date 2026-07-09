package com.exemple.client.persistence;

import com.exemple.client.domain.Client;
import java.util.Optional;

/**
 * Le contrat DAO dont le service a besoin — au sens du layering classique (controller →
 * service → DAO). Le service dépend de CETTE interface étroite, pas du gros
 * {@link org.springframework.data.jpa.repository.JpaRepository} : c'est ce qui le rend
 * testable en pur unitaire, avec un faux en mémoire (voir {@code InMemoryClientDao}), sans
 * base ni contexte Spring.
 *
 * <p>En production, c'est {@link ClientRepository} (Spring Data JPA) qui l'implémente.
 */
public interface ClientDao {

    Optional<Client> findByIdAvecAdresses(Long id);
}
