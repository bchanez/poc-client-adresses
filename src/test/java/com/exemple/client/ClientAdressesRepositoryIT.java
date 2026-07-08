package com.exemple.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.exemple.client.domain.Adresse;
import com.exemple.client.domain.Client;
import com.exemple.client.persistence.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * NIVEAU 1 — la persistance. La question du collègue : « un client peut avoir
 * plusieurs adresses, comment je teste que l'adresse est bien là ? »
 *
 * <p>Réponse : on écrit un client + 2 adresses, on le RELIT depuis une vraie base,
 * et on vérifie que les 2 adresses reviennent avec les bonnes valeurs.
 *
 * <p>C'est LE test critique d'une migration Hibernate : le lazy loading, le
 * mapping {@code @OneToMany}, la jointure — c'est ici que ça change de
 * comportement entre versions. Si une adresse « disparaît » après migration,
 * c'est ce test qui l'attrape (et il tourne sur Postgres, pas H2 : les
 * différences de dialecte ne passent pas à travers).
 */
@SpringBootTest
class ClientAdressesRepositoryIT extends AbstractPostgresIT {

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

        // relecture DEPUIS la base (save a committé ; ceci est une nouvelle transaction)
        Client relu = clients.findByIdAvecAdresses(id).orElseThrow();

        assertThat(relu.getAdresses())
                .extracting(Adresse::getRue, Adresse::getVille)
                .containsExactlyInAnyOrder(
                        tuple("1 rue de la Paix", "Lyon"),
                        tuple("42 avenue des Tests", "Paris"));
    }
}
