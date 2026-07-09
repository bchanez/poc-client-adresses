package com.exemple.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.exemple.client.application.domain.models.Adresse;
import com.exemple.client.application.domain.models.Client;
import com.exemple.client.application.domain.models.ClientId;
import com.exemple.client.application.domain.ports.ClientRepository;
import com.exemple.client.application.domain.ports.GetClientSummary;
import com.exemple.client.application.domain.views.ClientSummaryView;
import java.util.Optional;
import javax.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * La réponse à « et si ailleurs je veux le client SANS ses adresses ? » — le read side CQRS.
 *
 * <p>On ne charge PAS l'agrégat pour l'amputer : on projette {@code id, nom} via un port de
 * lecture dédié qui ne touche même pas la table ADRESSE. Le test prouve que :
 * <ul>
 *   <li>le résumé remonte le nom sans les adresses ;</li>
 *   <li>une seule requête SQL est émise — pas de jointure sur ADRESSE.</li>
 * </ul>
 * C'est le même concept « client », mais un modèle de LECTURE distinct de l'agrégat d'écriture.
 */
@SpringBootTest
class ClientReadModelIT extends AbstractPostgresIT {

    @Autowired
    private ClientRepository clients;

    @Autowired
    private GetClientSummary summaries;

    @Autowired
    private EntityManagerFactory emf;

    @Test
    void le_resume_ramene_le_client_sans_ses_adresses_en_une_requete() {
        Client c = Client.nouveau("Alice");
        c.ajouterAdresse(new Adresse("1 rue A", "Lyon"));
        c.ajouterAdresse(new Adresse("2 rue B", "Paris"));
        ClientId id = clients.save(c);

        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        ClientSummaryView resume = summaries.byId(id).orElseThrow();

        assertThat(resume.id()).isEqualTo(id.value());
        assertThat(resume.nom()).isEqualTo("Alice");
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1);   // pas de jointure ADRESSE
    }

    @Test
    void resume_d_un_client_inconnu_est_vide() {
        Optional<ClientSummaryView> resume = summaries.byId(new ClientId(999999L));

        assertThat(resume).isEmpty();
    }
}
