package com.exemple.client.adapters.primary.web;

import com.exemple.client.application.domain.models.Client;
import java.util.List;

/** DTOs web : ce que le front reçoit. Le domaine n'est jamais exposé tel quel. */
public final class ClientDtos {

    private ClientDtos() {
    }

    public record AdresseView(String rue, String ville) {
    }

    /**
     * Vue « fiche client » AVEC ses adresses (write side / agrégat). Point de spec explicite :
     * {@code adresses} est une LISTE — on renvoie TOUTES les adresses, jamais une « principale »
     * implicite, et une liste vide (jamais {@code null}) s'il n'y en a pas. Le test
     * {@code ClientAdressesSpecIT} verrouille ce choix pour 0, 1 et N adresses.
     */
    public record ClientView(Long id, String nom, List<AdresseView> adresses) {

        public static ClientView de(Client c) {
            List<AdresseView> vues = c.adresses().stream()
                    .map(a -> new AdresseView(a.rue(), a.ville()))
                    .toList();
            Long id = c.id() == null ? null : c.id().value();
            return new ClientView(id, c.nom(), vues);
        }
    }
}
