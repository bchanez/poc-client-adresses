package com.exemple.client.adapters.primary.web;

import com.exemple.client.adapters.primary.web.ClientDtos.ClientView;
import com.exemple.client.application.domain.models.ClientId;
import com.exemple.client.application.domain.ports.ClientRepository;
import com.exemple.client.application.domain.ports.GetClientSummary;
import com.exemple.client.application.domain.views.ClientSummaryView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adapter primaire HTTP. Il montre les DEUX côtés CQRS sur le même concept « client » :
 * <ul>
 *   <li>{@code GET /clients/{id}} → l'agrégat complet (write port), le client ET ses
 *       adresses. Pas de {@code LazyInitializationException} : le mapper a tout matérialisé ;</li>
 *   <li>{@code GET /clients/{id}/resume} → le read model (read port), le client SANS ses
 *       adresses, projeté sans jointure.</li>
 * </ul>
 */
@RestController
@RequestMapping("/clients")
class ClientController {

    private final ClientRepository clients;
    private final GetClientSummary summaries;

    ClientController(ClientRepository clients, GetClientSummary summaries) {
        this.clients = clients;
        this.summaries = summaries;
    }

    @GetMapping("/{id}")
    ResponseEntity<ClientView> parId(@PathVariable Long id) {
        return clients.findById(new ClientId(id))
                .map(ClientView::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/resume")
    ResponseEntity<ClientSummaryView> resume(@PathVariable Long id) {
        return summaries.byId(new ClientId(id))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
