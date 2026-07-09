package com.exemple.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.exemple.client.application.domain.models.Adresse;
import com.exemple.client.application.domain.models.Client;
import com.exemple.client.application.domain.models.ClientId;
import com.exemple.client.application.domain.ports.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * NIVEAU 1 — la persistance, à travers le PORT d'écriture (pas l'entité JPA, qui est
 * package-private et invisible d'ici). La question du collègue : « un client peut avoir
 * plusieurs adresses, comment je teste que l'adresse est bien là ? »
 *
 * <p>Réponse : on sauve un agrégat {@link Client} + 2 adresses, on le RELIT depuis une
 * vraie base, et on vérifie que les 2 adresses reviennent — en objets domaine, pas en
 * entités JPA. C'est LE test critique d'une migration Hibernate : si une adresse
 * « disparaît » dans le mapping, il rougit (et il tourne sur Postgres, pas H2).
 */
@SpringBootTest
class ClientAdressesRepositoryIT extends AbstractPostgresIT {

    @Autowired
    private ClientRepository clients;

    @Test
    void un_client_avec_plusieurs_adresses_les_ramene_toutes() {
        Client alice = Client.nouveau("Alice");
        alice.ajouterAdresse(new Adresse("1 rue de la Paix", "Lyon"));
        alice.ajouterAdresse(new Adresse("42 avenue des Tests", "Paris"));
        ClientId id = clients.save(alice);

        // relecture DEPUIS la base (save a committé ; ceci est une nouvelle transaction)
        Client relu = clients.findById(id).orElseThrow();

        assertThat(relu.adresses())
                .extracting(Adresse::rue, Adresse::ville)
                .containsExactlyInAnyOrder(
                        tuple("1 rue de la Paix", "Lyon"),
                        tuple("42 avenue des Tests", "Paris"));
    }
}
