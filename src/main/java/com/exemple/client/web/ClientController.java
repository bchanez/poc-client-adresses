package com.exemple.client.web;

import com.exemple.client.service.ClientService;
import com.exemple.client.web.ClientDtos.ClientView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code GET /clients/{id}} → le client et TOUTES ses adresses.
 *
 * <p>Le contrôleur ne fait que du HTTP : il délègue au {@link ClientService} (qui porte la
 * transaction) et mappe l'entité vers le DTO. Les adresses sont chargées par l'EntityGraph
 * du repository, donc pas de {@code LazyInitializationException} au mapping, même avec
 * {@code open-in-view=false}.
 */
@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clients;

    public ClientController(ClientService clients) {
        this.clients = clients;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientView> parId(@PathVariable Long id) {
        return clients.chercherParId(id)
                .map(ClientView::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
