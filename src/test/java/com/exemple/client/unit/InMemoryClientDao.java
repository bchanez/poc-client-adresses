package com.exemple.client.unit;

import com.exemple.client.domain.Client;
import com.exemple.client.persistence.ClientDao;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Faux DAO EN MÉMOIRE : implémente le contrat {@link ClientDao} avec une simple {@link Map}.
 * Il remplace la base pour tester {@code ClientService} en pur unitaire — aucun conteneur,
 * aucun PostgreSQL, aucun contexte Spring. C'est le pendant « unit » de nos {@code *IT}.
 */
class InMemoryClientDao implements ClientDao {

    private final Map<Long, Client> store = new HashMap<>();

    void enregistrer(Long id, Client client) {
        store.put(id, client);
    }

    @Override
    public Optional<Client> findByIdAvecAdresses(Long id) {
        return Optional.ofNullable(store.get(id));
    }
}
