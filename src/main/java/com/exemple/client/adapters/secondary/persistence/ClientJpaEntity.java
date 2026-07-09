package com.exemple.client.adapters.secondary.persistence;

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
 * Modèle de PERSISTANCE d'un client — pendant technique de l'agrégat domaine
 * {@code Client}. Package-private : il ne sort jamais de ce package. Le
 * {@code @OneToMany} lazy, le {@code @JoinColumn}, le cascade : toute la « plomberie »
 * JPA est confinée ICI, pas dans le domaine.
 *
 * <p>Le lazy n'est plus un piège : le mapper ({@link SqlClientRepository}) consomme la
 * collection PENDANT que la session est ouverte (via le {@code join fetch}) et la
 * recopie en objets domaine. L'agrégat qui ressort n'a plus aucun proxy.
 */
@Entity
@Table(name = "CLIENT")
class ClientJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOM", nullable = false)
    private String nom;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "CLIENT_ID")
    private List<AdresseJpaEntity> adresses = new ArrayList<>();

    protected ClientJpaEntity() {
    }

    ClientJpaEntity(Long id, String nom, List<AdresseJpaEntity> adresses) {
        this.id = id;
        this.nom = nom;
        this.adresses = new ArrayList<>(adresses);
    }

    Long getId() {
        return id;
    }

    String getNom() {
        return nom;
    }

    List<AdresseJpaEntity> getAdresses() {
        return adresses;
    }
}
