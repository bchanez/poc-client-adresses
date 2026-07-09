package com.exemple.client.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** Une adresse d'un client. Un client peut en avoir plusieurs (voir {@link Client}). */
@Entity
@Table(name = "ADRESSE")
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
