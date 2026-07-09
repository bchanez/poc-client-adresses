# POC — Recherche client par id avec plusieurs adresses

> Objection en réunion : *« une recherche de client par id, on récupère la table
> en base, affichage côté front… un client peut avoir plusieurs adresses. S'il y a
> un souci de spec, comment je teste que l'adresse est bien là ? »*

Ce repo répond en **séparant trois niveaux** — parce que « l'adresse est bien là »
veut dire trois choses différentes selon la couche — sur un vrai PostgreSQL
(Testcontainers, jamais H2).

## Le modèle

Un `Client` a une liste d'`Adresse` (`@OneToMany`, **lazy** par défaut). C'est
exactement le point qui bouge lors d'une migration Hibernate : fetch, jointure,
lazy loading.

## Les tests, par niveau

| Niveau | Test | Ce qu'il prouve |
|---|---|---|
| **1 · Persistance** | `ClientAdressesRepositoryIT` | On écrit un client + 2 adresses, on le relit depuis la base → les 2 adresses reviennent. **LE** test critique de la migration : si une adresse « disparaît », il rougit. |
| **2 · Mapping / spec** | `ClientAdressesSpecIT` | 0, 1 et N adresses. Le test **force à trancher la spec** : on renvoie toutes les adresses, liste vide (jamais `null`) si aucune. Le trou de spec se révèle en écrivant l'assertion. |
| **3 · Bout en bout** | `ClientApiE2EIT` | `GET /clients/{id}` sur un vrai serveur HTTP → les adresses sont bien dans le JSON servi au front. Plus le 404 si id inconnu. |
| **Le piège** | `ClientLazyLoadingIT` | `findById` naïf → `LazyInitializationException` (l'adresse n'est **pas** là). `findByIdAvecAdresses` (`@EntityGraph`) → tout chargé en **une seule** requête SQL (compteur Hibernate à l'appui, donc pas de N+1). |

Le fil rouge : on teste le **comportement observable** (ce qui est en base, ce qui
remonte dans le DTO/JSON), pas l'implémentation.

## Le point « millions de lignes »

Deux choses tiennent la recherche quand les tables grossissent :

1. **Le N+1** : charger 1 000 clients puis leurs adresses une par une = 1 001 requêtes.
   `ClientLazyLoadingIT` verrouille le fetch par `@EntityGraph` (1 requête) : c'est ce qui
   garde la recherche tenable. (Recherche par `id` = clé primaire indexée : elle scale toute seule.)
2. **L'index sur `ADRESSE.CLIENT_ID`** (`idx_adresse_client_id`, déclaré sur `Adresse`) :
   Postgres n'indexe pas automatiquement une colonne de clé étrangère. Sans lui, retrouver
   les adresses d'un client ferait un *seq scan* de toute la table ADRESSE — le vrai risque
   perf à des dizaines de millions de lignes.

## Lancer

```bash
./run-tests.sh      # mvn verify dans un conteneur JDK 17 + Postgres via Testcontainers
```

Prérequis : Docker. Le POC démarre lui-même sa base. Aucun H2, aucune base à installer.
