package com.exemple.client.adapters.secondary.persistence;

import com.exemple.client.application.domain.models.Adresse;
import com.exemple.client.application.domain.models.Client;
import com.exemple.client.application.domain.models.ClientId;
import com.exemple.client.application.domain.models.ClientSnapshot;
import com.exemple.client.application.domain.ports.ClientRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Adapter du port {@link ClientRepository} : LA frontière domaine ↔ JPA. Tout le mapping
 * (agrégat → entité, entité → agrégat) se fait ici, à un seul endroit — c'est le Data Mapper
 * de Fowler / la Persistence Ignorance de Nilsson (note Obsidian « Domain Model vs
 * Persistence Model »).
 *
 * <p>Point clé anti-lazy : {@link #toAggregate} consomme {@code entity.getAdresses()}
 * pendant que la session est encore ouverte (le {@code join fetch} a déjà tout ramené), puis
 * recopie en {@link Adresse} domaine. L'agrégat renvoyé ne contient donc AUCUN proxy
 * Hibernate — {@code LazyInitializationException} ne peut plus se produire côté domaine.
 */
@Repository
class SqlClientRepository implements ClientRepository {

    private final SpringDataClientRepository springData;

    SqlClientRepository(SpringDataClientRepository springData) {
        this.springData = springData;
    }

    @Override
    public ClientId save(Client client) {
        ClientJpaEntity saved = springData.save(toEntity(client));
        return new ClientId(saved.getId());
    }

    @Override
    public Optional<Client> findById(ClientId id) {
        return springData.findByIdAvecAdresses(id.value()).map(SqlClientRepository::toAggregate);
    }

    private static ClientJpaEntity toEntity(Client client) {
        Long id = client.id() == null ? null : client.id().value();
        List<AdresseJpaEntity> adresses = client.adresses().stream()
                .map(a -> new AdresseJpaEntity(a.rue(), a.ville()))
                .toList();
        return new ClientJpaEntity(id, client.nom(), adresses);
    }

    private static Client toAggregate(ClientJpaEntity entity) {
        List<Adresse> adresses = entity.getAdresses().stream()
                .map(a -> new Adresse(a.getRue(), a.getVille()))
                .toList();
        return Client.fromSnapshot(
                new ClientSnapshot(new ClientId(entity.getId()), entity.getNom(), adresses));
    }
}
