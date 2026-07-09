package com.exemple.client.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Une adresse d'un client. Un client peut en avoir plusieurs (voir {@link Client}).
 *
 * <p>C'est le <b>côté propriétaire</b> de la relation : c'est {@code Adresse} qui porte la
 * clé étrangère {@code CLIENT_ID} ({@code @ManyToOne} + {@code @JoinColumn}), et
 * {@link Client} référence en {@code mappedBy}. Conséquence perf : à l'insertion, le
 * {@code client_id} est écrit directement dans l'{@code INSERT} — plus d'{@code UPDATE}
 * supplémentaire comme avec un {@code @OneToMany} unidirectionnel + {@code @JoinColumn}.
 *
 * <p>La colonne {@code CLIENT_ID} est INDEXÉE : Postgres n'indexe pas automatiquement une
 * colonne de clé étrangère ; sans cet index, retrouver les adresses d'un client force un
 * seq scan de toute la table ADRESSE — insoutenable à des dizaines de millions de lignes.
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CLIENT_ID", nullable = false)
    private Client client;

    protected Adresse() {
    }

    public Adresse(String rue, String ville) {
        this.rue = rue;
        this.ville = ville;
    }

    /** Rattache cette adresse à son client (appelé par {@link Client#ajouterAdresse}). */
    void attacherA(Client client) {
        this.client = client;
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
