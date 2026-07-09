package com.exemple.client.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.exemple.client.domain.Adresse;
import com.exemple.client.domain.Client;
import com.exemple.client.persistence.ClientRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Le PIÈGE, et pourquoi ce test protège la migration.
 *
 * <p>Une recherche « client par id » naïve ({@code findById}) ne charge PAS les adresses
 * (relation lazy) : y accéder hors session lève {@link LazyInitializationException}. La bonne
 * réponse, {@code findByIdAvecAdresses} ({@code @EntityGraph}), ramène tout en UNE requête.
 * Ce test verrouille les deux comportements.
 */
class ClientLazyLoadingIT extends AbstractIntegrationIT {

    @Autowired
    private ClientRepository clients;

    @Autowired
    private EntityManagerFactory emf;

    private Long id;

    @BeforeEach
    void seed() {
        clients.deleteAll();
        Client c = new Client("Alice");
        c.ajouterAdresse(new Adresse("1 rue A", "Lyon"));
        c.ajouterAdresse(new Adresse("2 rue B", "Paris"));
        id = clients.save(c).getId();
    }

    @Test
    void findById_naif_ne_charge_pas_les_adresses_lazy() {
        Client c = clients.findById(id).orElseThrow();

        assertThatThrownBy(() -> c.getAdresses().size())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void entity_graph_charge_les_adresses_en_UNE_seule_requete() {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        Client c = clients.findByIdAvecAdresses(id).orElseThrow();

        assertThat(c.getAdresses()).hasSize(2);
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
    }
}
