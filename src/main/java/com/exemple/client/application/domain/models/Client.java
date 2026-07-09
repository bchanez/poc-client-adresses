package com.exemple.client.application.domain.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Agrégat <b>Client</b> — racine qui possède ses {@link Adresse}. Objet métier PUR :
 * aucune annotation JPA, aucune dépendance à Hibernate, aucun {@code @OneToMany}.
 *
 * <p>C'est LA différence avec le POC « à la Spring » d'origine, où la classe était à la
 * fois l'entité JPA ET le modèle métier. Ici :
 * <ul>
 *   <li>la liste {@code adresses} est un vrai {@link ArrayList} rempli par le mapper —
 *       pas un proxy Hibernate {@code PersistentBag}. Donc {@code LazyInitializationException}
 *       est <i>structurellement impossible</i> : il n'y a rien à charger paresseusement ;</li>
 *   <li>l'agrégat se charge et se sauve d'un bloc (Adresse a le même cycle de vie que le
 *       Client), donc pas de N+1 : une requête ramène le tout.</li>
 * </ul>
 *
 * <p>Le prix de ce choix (charger toujours les adresses) est payé côté <i>lecture</i> :
 * quand on veut le client SANS ses adresses, on ne passe pas par cet agrégat mais par un
 * read model dédié (voir {@code GetClientSummary} / CQRS).
 */
public class Client {

    /** {@code null} tant que le client n'a pas été persisté (id auto-généré en base). */
    private final ClientId id;
    private final String nom;
    private final List<Adresse> adresses;

    private Client(ClientId id, String nom, List<Adresse> adresses) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("nom obligatoire");
        }
        this.id = id;
        this.nom = nom;
        this.adresses = new ArrayList<>(adresses);
    }

    /** Un nouveau client, pas encore persisté (sans id). */
    public static Client nouveau(String nom) {
        return new Client(null, nom, List.of());
    }

    /** Reconstitue un client existant depuis sa photo de persistance. */
    public static Client fromSnapshot(ClientSnapshot snapshot) {
        return new Client(snapshot.id(), snapshot.nom(), snapshot.adresses());
    }

    public void ajouterAdresse(Adresse adresse) {
        this.adresses.add(adresse);
    }

    public ClientId id() {
        return id;
    }

    public String nom() {
        return nom;
    }

    /** Copie défensive : l'invariant de l'agrégat ne se modifie que via ses méthodes. */
    public List<Adresse> adresses() {
        return List.copyOf(adresses);
    }

    public ClientSnapshot toSnapshot() {
        return new ClientSnapshot(id, nom, List.copyOf(adresses));
    }
}
