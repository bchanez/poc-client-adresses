package com.exemple.client.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.exemple.client.domain.Adresse;
import com.exemple.client.domain.Client;
import com.exemple.client.persistence.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * NIVEAU 1 — la persistance. On écrit un client et ses 2 adresses, on le RELIT depuis une
 * vraie base PostgreSQL (pas H2), et on vérifie que les 2 adresses reviennent bien. C'est le
 * premier test qui casse si une migration Hibernate abîme le mapping : une adresse « perdue »
 * le fait rougir.
 *
 * <p>Ce cas ressemble à « plusieurs adresses » de {@link ClientAdressesSpecIT}, et c'est
 * voulu : on garde les deux pour raconter deux étapes différentes — ici « ça persiste »
 * (Niveau 1), là-bas « ça remonte bien dans le DTO envoyé au front » (Niveau 2).
 */
class ClientAdressesRepositoryIT extends AbstractIntegrationIT {

    @Autowired
    private ClientRepository clients;

    @BeforeEach
    void clean() {
        clients.deleteAll();
    }

    @Test
    void un_client_avec_plusieurs_adresses_les_ramene_toutes() {
        Client alice = new Client("Alice");
        alice.ajouterAdresse(new Adresse("1 rue de la Paix", "Lyon"));
        alice.ajouterAdresse(new Adresse("42 avenue des Tests", "Paris"));
        Long id = clients.save(alice).getId();

        Client relu = clients.findByIdAvecAdresses(id).orElseThrow();

        assertThat(relu.getAdresses())
                .extracting(Adresse::getRue, Adresse::getVille)
                .containsExactlyInAnyOrder(
                        tuple("1 rue de la Paix", "Lyon"),
                        tuple("42 avenue des Tests", "Paris"));
    }
}
