package com.exemple.client.web;

import com.exemple.client.persistence.ClientRepository;
import com.exemple.client.web.ClientDtos.ClientView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code GET /clients/{id}} → le client et TOUTES ses adresses.
 *
 * <p>On passe par {@code findByIdAvecAdresses} (join fetch) : les adresses sont
 * chargées dans la requête, donc pas de {@code LazyInitializationException} au
 * moment du mapping vers le DTO, même avec {@code open-in-view=false}.
 */
@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientRepository clients;

    public ClientController(ClientRepository clients) {
        this.clients = clients;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientView> parId(@PathVariable Long id) {
        return clients.findByIdAvecAdresses(id)
                .map(ClientView::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
