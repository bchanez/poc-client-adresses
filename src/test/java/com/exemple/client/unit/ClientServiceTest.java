package com.exemple.client.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.exemple.client.domain.Adresse;
import com.exemple.client.domain.Client;
import com.exemple.client.service.ClientService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * NIVEAU UNIT — le service, SANS base, avec un {@link InMemoryClientDao} à la place du
 * repository JPA (instantané, sans Docker, sans Spring).
 *
 * <p>On teste le domaine EN LE TRAVERSANT par le service (son API publique), pas via une
 * classe de test dédiée à {@code Client} : pas de test 1:1 couplé à la structure. Construire
 * l'agrégat ({@code ajouterAdresse}) et lire ses adresses ({@code getAdresses}) est exercé ici,
 * comme comportement observable du cas d'usage.
 */
class ClientServiceTest {

    @Test
    void chercherParId_remonte_le_client_et_TOUTES_ses_adresses() {
        InMemoryClientDao dao = new InMemoryClientDao();
        Client alice = new Client("Alice");
        alice.ajouterAdresse(new Adresse("1 rue A", "Lyon"));
        alice.ajouterAdresse(new Adresse("2 rue B", "Paris"));
        dao.enregistrer(1L, alice);

        Optional<Client> trouve = new ClientService(dao).chercherParId(1L);

        assertThat(trouve).isPresent();
        assertThat(trouve.get().getAdresses())
                .extracting(Adresse::getRue, Adresse::getVille)
                .containsExactly(tuple("1 rue A", "Lyon"), tuple("2 rue B", "Paris"));
    }

    @Test
    void un_client_sans_adresse_remonte_une_liste_vide() {
        InMemoryClientDao dao = new InMemoryClientDao();
        dao.enregistrer(1L, new Client("Sans adresse"));

        Optional<Client> trouve = new ClientService(dao).chercherParId(1L);

        assertThat(trouve).isPresent();
        assertThat(trouve.get().getAdresses()).isEmpty();
    }

    @Test
    void chercherParId_est_vide_quand_le_client_est_inconnu() {
        assertThat(new ClientService(new InMemoryClientDao()).chercherParId(999L)).isEmpty();
    }
}
