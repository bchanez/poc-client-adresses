package com.exemple.client.application.domain.ports;

import com.exemple.client.application.domain.models.ClientId;
import com.exemple.client.application.domain.views.ClientSummaryView;
import java.util.Optional;

/**
 * Port de LECTURE (read side) — le client SANS ses adresses.
 *
 * <p>Séparé du {@link ClientRepository} d'écriture exprès : ici on ne veut ni l'agrégat, ni
 * ses invariants, ni ses adresses. L'implémentation fait un {@code SELECT id, nom} qui ne
 * touche même pas la table ADRESSE — donc aucune jointure, aucun coût inutile. C'est le
 * pendant CQRS de {@link ClientRepository} : « le même concept métier a plusieurs
 * représentations selon qu'on écrit ou qu'on lit » (note Obsidian « CQRS »).
 */
public interface GetClientSummary {

    Optional<ClientSummaryView> byId(ClientId id);
}
