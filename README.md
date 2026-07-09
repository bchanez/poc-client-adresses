# POC — Recherche client par id avec plusieurs adresses

> Objection en réunion : *« une recherche de client par id, on récupère la table
> en base, affichage côté front… un client peut avoir plusieurs adresses. S'il y a
> un souci de spec, comment je teste que l'adresse est bien là ? »*

Ce repo répond en **séparant trois niveaux** — parce que « l'adresse est bien là »
veut dire trois choses différentes selon la couche — sur un vrai PostgreSQL
(Testcontainers, jamais H2). Et il le fait sur une **archi hexagonale + CQRS**, pas
sur une entité JPA qui sert aussi de modèle métier.

## Le modèle — hexagonal (ports & adapters)

`Client` est un **agrégat pur** qui possède ses `Adresse` (Value Objects) : aucune
annotation JPA, aucun `@OneToMany` dans le domaine. La persistance vit à part.

```
application/domain/
  models/   Client (agrégat), Adresse (VO), ClientId (VO), ClientSnapshot
  ports/    ClientRepository (write), GetClientSummary (read)
  views/    ClientSummaryView (read model, sans adresses)
adapters/
  secondary/persistence/  ClientJpaEntity, AdresseJpaEntity  <- package-private,
                          SpringDataClientRepository, SqlClientRepository (mapper),
                          SqlGetClientSummary
  primary/web/            ClientController, ClientDtos
```

Le **pourquoi de cette archi** (vs entité-unique « à la Spring », vs charger Client et
Adresse séparément), avec les sources, est documenté dans
[`package-info.java`](src/main/java/com/exemple/client/package-info.java).

Point clé : les `*JpaEntity` sont **package-private** → le compilateur interdit qu'elles
« parcourent l'app ». Et le mapper matérialise l'agrégat entier pendant que la session
est ouverte → **le lazy loading n'est plus un piège, il n'existe plus**.

## Les tests, par niveau

| Niveau | Test | Ce qu'il prouve |
|---|---|---|
| **1 · Persistance** | `ClientAdressesRepositoryIT` | On sauve un client + 2 adresses via le port, on le relit → les 2 adresses reviennent en objets domaine. **LE** test critique de migration : si une adresse « disparaît » dans le mapping, il rougit. |
| **2 · Mapping / spec** | `ClientAdressesSpecIT` | 0, 1 et N adresses. Force à trancher la spec : on renvoie toutes les adresses, liste vide (jamais `null`) si aucune. |
| **3 · Bout en bout** | `ClientApiE2EIT` | `GET /clients/{id}` sur un vrai serveur HTTP → les adresses sont dans le JSON servi au front. Plus le 404 si id inconnu. |
| **Plus de piège** | `ClientAgregatFetchIT` | L'agrégat charge ses adresses **hors transaction sans `LazyInitializationException`** (le mapper a tout matérialisé), et en **une seule** requête (`join fetch`, compteur Hibernate → pas de N+1). |
| **Client sans adresses** | `ClientReadModelIT` | Le read side CQRS : `GetClientSummary` projette `id, nom` **sans toucher ADRESSE** (1 requête, pas de jointure). La réponse à « et si ailleurs je veux le client sans ses adresses ? ». |

Le fil rouge : on teste le **comportement observable** (ce qui est en base, ce qui
remonte dans le DTO/JSON), pas l'implémentation.

## Le point « millions de lignes »

À 40M+ lignes, deux choses tiennent la recherche :

1. **La recherche par `id`** = clé primaire indexée → scale toute seule.
2. **Le `join fetch` des adresses** repose sur la colonne `ADRESSE.CLIENT_ID`, qui est
   **explicitement indexée** (`idx_adresse_client_id`, sur `AdresseJpaEntity`). Postgres
   n'indexe pas automatiquement une colonne de clé étrangère : sans cet index, retrouver
   les adresses d'un client ferait un *seq scan* de toute la table ADRESSE.
3. **Le N+1** (charger N clients puis leurs adresses une par une) est éliminé par le
   `join fetch` — verrouillé par `ClientAgregatFetchIT`.

Et quand on n'a pas besoin des adresses du tout, on passe par le **read model**
(`GetClientSummary`) qui ne charge que ce que l'écran demande.

## Lancer

```bash
./run-tests.sh      # mvn verify dans un conteneur JDK 17 + Postgres via Testcontainers
```

Prérequis : Docker. Le POC démarre lui-même sa base. Aucun H2, aucune base à installer.
