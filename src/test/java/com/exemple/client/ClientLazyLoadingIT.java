package com.exemple.client;

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
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Le PIÈGE, et pourquoi ce test protège la migration.
 *
 * <p>Une recherche « client par id » naïve ({@code findById}) ne charge PAS les
 * adresses (relation lazy). Deux façons de s'en apercevoir :
 * <ol>
 *   <li>y accéder hors session → {@link LazyInitializationException} : l'adresse
 *       n'est PAS là (le bug que le collègue redoute) ;</li>
 *   <li>y accéder DANS une transaction ouverte par requête → N+1 requêtes.</li>
 * </ol>
 * La bonne réponse, {@code findByIdAvecAdresses} (join fetch), ramène tout en
 * UNE requête. Ce test verrouille les deux comportements — donc toute régression
 * de fetch introduite par la migration Hibernate fera rougir la CI.
 */
@SpringBootTest
class ClientLazyLoadingIT extends AbstractPostgresIT {

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
        // session fermée à la sortie du repo (open-in-view=false) => accès lazy interdit
        Client c = clients.findById(id).orElseThrow();

        assertThatThrownBy(() -> c.getAdresses().size())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void join_fetch_charge_les_adresses_en_UNE_seule_requete() {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        Client c = clients.findByIdAvecAdresses(id).orElseThrow();

        assertThat(c.getAdresses()).hasSize(2);          // les adresses sont là
        assertThat(stats.getPrepareStatementCount())     // …et en une seule requête SQL
                .isEqualTo(1);
    }
}
