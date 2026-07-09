package com.exemple.client.application.domain.ports;

import com.exemple.client.application.domain.models.Client;
import com.exemple.client.application.domain.models.ClientId;
import java.util.Optional;

/**
 * Port d'ÉCRITURE (write side). Il ne parle QUE domaine : {@link Client}, {@link ClientId}.
 * Aucune fuite de JPA, aucun {@code JpaRepository} visible ici — l'implémentation vit dans
 * {@code adapters/secondary/persistence} et fait la traduction.
 *
 * <p>{@link #findById} ramène l'agrégat ENTIER (client + ses adresses) en une requête :
 * c'est la porte à utiliser quand on va <b>modifier</b> le client (ajouter/retirer une
 * adresse), là où les invariants comptent. Pour du simple affichage sans adresses, ne pas
 * passer par ici → voir {@link GetClientSummary}.
 */
public interface ClientRepository {

    /** Persiste le client et renvoie son identité (générée en base si nouveau). */
    ClientId save(Client client);

    /** Charge l'agrégat complet — adresses comprises — ou vide si l'id est inconnu. */
    Optional<Client> findById(ClientId id);
}
