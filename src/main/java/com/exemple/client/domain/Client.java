package com.exemple.client.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Un client et ses adresses. La relation {@code @OneToMany} est LAZY par défaut :
 * les adresses ne sont chargées que si on y accède pendant que la session est
 * ouverte. C'est LE piège de la recherche « client par id » — et exactement ce
 * que les tests verrouillent (voir {@code ClientAdressesRepositoryIT} et
 * {@code ClientLazyLoadingIT}).
 *
 * <p>La relation est <b>bidirectionnelle</b> : {@code Adresse} est le côté propriétaire
 * (il porte la FK {@code CLIENT_ID}), et ce côté-ci est en {@code mappedBy}. C'est ce qui
 * évite les {@code UPDATE} superflus à l'écriture (voir {@link Adresse}). {@link #ajouterAdresse}
 * synchronise les deux bouts : sans ça, la FK resterait nulle.
 */
@Entity
@Table(name = "CLIENT")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOM", nullable = false)
    private String nom;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Adresse> adresses = new ArrayList<>();

    protected Client() {
    }

    public Client(String nom) {
        this.nom = nom;
    }

    public void ajouterAdresse(Adresse adresse) {
        adresse.attacherA(this);
        this.adresses.add(adresse);
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public List<Adresse> getAdresses() {
        return adresses;
    }
}
