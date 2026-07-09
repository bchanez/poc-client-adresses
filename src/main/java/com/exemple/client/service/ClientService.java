package com.exemple.client.service;

import com.exemple.client.domain.Client;
import com.exemple.client.persistence.ClientDao;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Couche service : la frontière transactionnelle du métier (controller → service → repository).
 *
 * <p>C'est ici que vit la transaction — pas dans le contrôleur, pas seulement dans le repo.
 * {@code @Transactional(readOnly = true)} sur la lecture : Hibernate désactive le dirty
 * checking et le flush automatique, la session est optimisée pour lire. L'{@code @EntityGraph}
 * du repository charge les adresses AVANT la fermeture de la transaction, donc le mapping vers
 * le DTO (côté contrôleur, hors transaction) reste sûr malgré {@code open-in-view=false}.
 *
 * <p>Une écriture suivrait le même schéma avec {@code @Transactional} (sans {@code readOnly}) :
 * plusieurs opérations du même cas d'usage seraient alors atomiques dans une seule transaction.
 */
@Service
public class ClientService {

    private final ClientDao clients;

    public ClientService(ClientDao clients) {
        this.clients = clients;
    }

    @Transactional(readOnly = true)
    public Optional<Client> chercherParId(Long id) {
        return clients.findByIdAvecAdresses(id);
    }
}
