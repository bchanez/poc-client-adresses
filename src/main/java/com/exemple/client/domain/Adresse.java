package com.exemple.client.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Une adresse d'un client. Un client peut en avoir plusieurs (voir {@link Client}).
 *
 * <p>La colonne {@code CLIENT_ID} (créée par le {@code @JoinColumn} côté {@link Client})
 * est INDEXÉE. Postgres n'indexe pas automatiquement une colonne de clé étrangère :
 * sans cet index, retrouver les adresses d'un client force un seq scan de toute la
 * table ADRESSE — insoutenable quand elle atteint des dizaines de millions de lignes.
 */
@Entity
@Table(name = "ADRESSE", indexes = @Index(name = "idx_adresse_client_id", columnList = "CLIENT_ID"))
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "RUE", nullable = false)
    private String rue;

    @Column(name = "VILLE", nullable = false)
    private String ville;

    protected Adresse() {
    }

    public Adresse(String rue, String ville) {
        this.rue = rue;
        this.ville = ville;
    }

    public Long getId() {
        return id;
    }

    public String getRue() {
        return rue;
    }

    public String getVille() {
        return ville;
    }
}
