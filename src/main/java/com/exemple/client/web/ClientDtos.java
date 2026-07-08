package com.exemple.client.web;

import com.exemple.client.domain.Client;
import java.util.List;

/** DTOs web : ce que le front reçoit (le domaine n'est pas exposé tel quel). */
public final class ClientDtos {

    private ClientDtos() {
    }

    public record AdresseView(String rue, String ville) {
    }

    /**
     * Vue d'un client. Point de spec explicite : {@code adresses} est une LISTE —
     * on renvoie TOUTES les adresses, jamais une seule « principale » implicite.
     * Le test {@code ClientAdressesSpecIT} verrouille ce choix pour 0, 1 et N adresses.
     */
    public record ClientView(Long id, String nom, List<AdresseView> adresses) {

        public static ClientView de(Client c) {
            List<AdresseView> vues = c.getAdresses().stream()
                    .map(a -> new AdresseView(a.getRue(), a.getVille()))
                    .toList();
            return new ClientView(c.getId(), c.getNom(), vues);
        }
    }
}
