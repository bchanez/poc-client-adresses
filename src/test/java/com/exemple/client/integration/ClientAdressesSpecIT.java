package com.exemple.client.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.exemple.client.domain.Adresse;
import com.exemple.client.domain.Client;
import com.exemple.client.persistence.ClientRepository;
import com.exemple.client.web.ClientDtos.AdresseView;
import com.exemple.client.web.ClientDtos.ClientView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * NIVEAU 2 — le mapping vers le DTO front, et le fond de la question : « et si la spec est
 * ambiguë ? ». Le test FORCE à trancher : on renvoie TOUTES les adresses, liste vide (jamais
 * {@code null}) quand il n'y en a pas. Le test devient la spec exécutable.
 */
class ClientAdressesSpecIT extends AbstractIntegrationIT {

    @Autowired
    private ClientRepository clients;

    @BeforeEach
    void clean() {
        clients.deleteAll();
    }

    private ClientView relire(Client c) {
        Long id = clients.save(c).getId();
        return ClientView.de(clients.findByIdAvecAdresses(id).orElseThrow());
    }

    @Test
    void zero_adresse_renvoie_une_liste_vide_pas_null() {
        ClientView vue = relire(new Client("Sans adresse"));

        assertThat(vue.adresses()).isNotNull().isEmpty();
    }

    @Test
    void une_seule_adresse() {
        Client c = new Client("Mono");
        c.ajouterAdresse(new Adresse("1 rue Unique", "Nantes"));

        ClientView vue = relire(c);

        assertThat(vue.adresses()).containsExactly(new AdresseView("1 rue Unique", "Nantes"));
    }

    @Test
    void plusieurs_adresses_sont_TOUTES_presentes() {
        Client c = new Client("Multi");
        c.ajouterAdresse(new Adresse("1 rue A", "Lyon"));
        c.ajouterAdresse(new Adresse("2 rue B", "Paris"));
        c.ajouterAdresse(new Adresse("3 rue C", "Lille"));

        ClientView vue = relire(c);

        assertThat(vue.adresses())
                .containsExactlyInAnyOrder(
                        new AdresseView("1 rue A", "Lyon"),
                        new AdresseView("2 rue B", "Paris"),
                        new AdresseView("3 rue C", "Lille"));
    }
}
