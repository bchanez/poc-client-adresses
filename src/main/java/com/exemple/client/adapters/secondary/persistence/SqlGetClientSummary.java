package com.exemple.client.adapters.secondary.persistence;

import com.exemple.client.application.domain.models.ClientId;
import com.exemple.client.application.domain.ports.GetClientSummary;
import com.exemple.client.application.domain.views.ClientSummaryView;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Adapter du port de LECTURE {@link GetClientSummary}. Projette la ligne {@code (id, nom)}
 * en {@link ClientSummaryView} — jamais un agrégat. La table ADRESSE n'est pas touchée :
 * c'est tout l'intérêt du read model quand on veut le client sans ses adresses.
 */
@Repository
class SqlGetClientSummary implements GetClientSummary {

    private final SpringDataClientRepository springData;

    SqlGetClientSummary(SpringDataClientRepository springData) {
        this.springData = springData;
    }

    @Override
    public Optional<ClientSummaryView> byId(ClientId id) {
        return springData.findSummaryById(id.value())
                .map(row -> new ClientSummaryView(row.getId(), row.getNom()));
    }
}
