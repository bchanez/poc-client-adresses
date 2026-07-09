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
 * NIVEAU 1 — la persistance. « Un client peut avoir plusieurs adresses, comment je teste
 * que l'adresse est bien là ? » On écrit un client + 2 adresses, on le RELIT depuis une
 * vraie base, on vérifie que les 2 adresses reviennent. LE test critique d'une migration
 * Hibernate : si une adresse « disparaît », il rougit (et il tourne sur Postgres, pas H2).
 *
 * <p>NOTE : le cas « plusieurs adresses reviennent » est un sous-ensemble du round-trip N de
 * {@link ClientAdressesSpecIT}. On pourrait <b>fusionner</b> les deux. On le garde distinct
 * ici pour le fil narratif « Niveau 1 persistance » vs « Niveau 2 mapping/spec ».
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
