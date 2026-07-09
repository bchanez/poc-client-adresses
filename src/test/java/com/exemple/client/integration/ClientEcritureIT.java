package com.exemple.client.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.exemple.client.domain.Adresse;
import com.exemple.client.domain.Client;
import com.exemple.client.persistence.ClientRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Perf en ÉCRITURE. Verrouille le gain du mapping bidirectionnel : sauver un client avec
 * 2 adresses = 3 {@code INSERT} et <b>0 {@code UPDATE}</b>.
 *
 * <p>Avec l'ancien {@code @OneToMany} unidirectionnel + {@code @JoinColumn}, Hibernate
 * insérait les adresses avec {@code client_id} à NULL puis émettait un {@code UPDATE} par
 * adresse. En rendant {@code Adresse} propriétaire de la relation, la FK part directement
 * dans l'{@code INSERT}. Ce test rougit si quelqu'un revient en arrière.
 */
class ClientEcritureIT extends AbstractIntegrationIT {

    @Autowired
    private ClientRepository clients;

    @Autowired
    private EntityManagerFactory emf;

    @BeforeEach
    void clean() {
        clients.deleteAll();
    }

    @Test
    void sauver_un_client_avec_deux_adresses_fait_trois_inserts_et_zero_update() {
        Client c = new Client("Alice");
        c.ajouterAdresse(new Adresse("1 rue A", "Lyon"));
        c.ajouterAdresse(new Adresse("2 rue B", "Paris"));

        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        clients.saveAndFlush(c);

        assertThat(stats.getEntityInsertCount()).isEqualTo(3);   // 1 client + 2 adresses
        assertThat(stats.getEntityUpdateCount()).isEqualTo(0);   // plus d'UPDATE de FK
    }
}
