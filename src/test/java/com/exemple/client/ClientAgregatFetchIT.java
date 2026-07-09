package com.exemple.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.exemple.client.application.domain.models.Adresse;
import com.exemple.client.application.domain.models.Client;
import com.exemple.client.application.domain.models.ClientId;
import com.exemple.client.application.domain.ports.ClientRepository;
import javax.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Là où l'ancien POC avait « le piège » ({@code LazyInitializationException} + N+1), l'archi
 * hexagonale prouve que le piège N'EXISTE PLUS — par construction, pas par discipline.
 *
 * <ol>
 *   <li>{@code findById} renvoie un agrégat dont on lit les adresses HORS de toute
 *       transaction, sans exception : le mapper a déjà tout matérialisé en objets domaine
 *       (aucun proxy Hibernate ne subsiste). Le lazy leak est impossible ;</li>
 *   <li>l'agrégat entier est chargé en UNE requête ({@code join fetch}) : compteur SQL
 *       Hibernate à l'appui, donc pas de N+1.</li>
 * </ol>
 */
@SpringBootTest
class ClientAgregatFetchIT extends AbstractPostgresIT {

    @Autowired
    private ClientRepository clients;

    @Autowired
    private EntityManagerFactory emf;

    private ClientId id;

    @BeforeEach
    void seed() {
        Client c = Client.nouveau("Alice");
        c.ajouterAdresse(new Adresse("1 rue A", "Lyon"));
        c.ajouterAdresse(new Adresse("2 rue B", "Paris"));
        id = clients.save(c);
    }

    @Test
    void l_agregat_charge_ses_adresses_hors_transaction_sans_lazy_exception() {
        // session fermée à la sortie de l'adapter (open-in-view=false)
        Client c = clients.findById(id).orElseThrow();

        // dans l'ancien POC, cet accès levait LazyInitializationException. Plus ici :
        assertThatCode(() -> assertThat(c.adresses()).hasSize(2)).doesNotThrowAnyException();
    }

    @Test
    void join_fetch_charge_l_agregat_en_UNE_seule_requete() {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        Client c = clients.findById(id).orElseThrow();

        assertThat(c.adresses()).hasSize(2);              // les adresses sont là
        assertThat(stats.getPrepareStatementCount())      // …et en une seule requête SQL
                .isEqualTo(1);
    }
}
