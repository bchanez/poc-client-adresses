package com.exemple.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.exemple.client.adapters.primary.web.ClientDtos.AdresseView;
import com.exemple.client.adapters.primary.web.ClientDtos.ClientView;
import com.exemple.client.application.domain.models.Adresse;
import com.exemple.client.application.domain.models.Client;
import com.exemple.client.application.domain.models.ClientId;
import com.exemple.client.application.domain.ports.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * NIVEAU 3 — bout en bout : vrai serveur HTTP → contrôleur → port → adapter → Postgres.
 * On prouve que les adresses arrivent bien jusque dans la réponse JSON servie au front.
 * C'est la réponse littérale à « comment je teste que l'adresse est bien là, côté front ? ».
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClientApiE2EIT extends AbstractPostgresIT {

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private ClientRepository clients;

    @Test
    void get_client_par_id_renvoie_toutes_ses_adresses() {
        Client alice = Client.nouveau("Alice");
        alice.ajouterAdresse(new Adresse("1 rue de la Paix", "Lyon"));
        alice.ajouterAdresse(new Adresse("42 avenue des Tests", "Paris"));
        ClientId id = clients.save(alice);

        ClientView vue = http.getForObject("/clients/" + id.value(), ClientView.class);

        assertThat(vue.nom()).isEqualTo("Alice");
        assertThat(vue.adresses())
                .containsExactlyInAnyOrder(
                        new AdresseView("1 rue de la Paix", "Lyon"),
                        new AdresseView("42 avenue des Tests", "Paris"));
    }

    @Test
    void get_client_inconnu_renvoie_404() {
        ResponseEntity<ClientView> reponse =
                http.getForEntity("/clients/999999", ClientView.class);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
