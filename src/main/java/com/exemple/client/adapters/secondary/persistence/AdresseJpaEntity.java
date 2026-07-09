package com.exemple.client.adapters.secondary.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Modèle de PERSISTANCE d'une adresse — le pendant technique du VO domaine
 * {@code Adresse}. {@code class} (pas {@code public class}) : <b>package-private</b>,
 * donc le compilateur INTERDIT d'y toucher hors de ce package. C'est ça qui empêche
 * l'entité JPA de « parcourir toute l'app ».
 *
 * <p>La colonne {@code CLIENT_ID} (créée par le {@code @JoinColumn} côté
 * {@link ClientJpaEntity}) est INDEXÉE : sans ça, retrouver les adresses d'un client
 * force Postgres à un seq scan de toute la table ADRESSE — insoutenable à 40M+ lignes.
 * Postgres n'indexe pas automatiquement une colonne de FK, il faut le déclarer.
 */
@Entity
@Table(name = "ADRESSE", indexes = @Index(name = "idx_adresse_client_id", columnList = "CLIENT_ID"))
class AdresseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "RUE", nullable = false)
    private String rue;

    @Column(name = "VILLE", nullable = false)
    private String ville;

    protected AdresseJpaEntity() {
    }

    AdresseJpaEntity(String rue, String ville) {
        this.rue = rue;
        this.ville = ville;
    }

    String getRue() {
        return rue;
    }

    String getVille() {
        return ville;
    }
}
