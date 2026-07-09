package com.exemple.client.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Un client et ses adresses. La relation {@code @OneToMany} est LAZY par défaut :
 * les adresses ne sont chargées que si on y accède pendant que la session est
 * ouverte. C'est LE piège de la recherche « client par id » — et exactement ce
 * que les tests verrouillent (voir {@code ClientAdressesRepositoryIT} et
 * {@code ClientLazyLoadingIT}).
 */
@Entity
@Table(name = "CLIENT")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOM", nullable = false)
    private String nom;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "CLIENT_ID")
    private List<Adresse> adresses = new ArrayList<>();

    protected Client() {
    }

    public Client(String nom) {
        this.nom = nom;
    }

    public void ajouterAdresse(Adresse adresse) {
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
