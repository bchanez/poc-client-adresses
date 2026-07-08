package com.exemple.client.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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
