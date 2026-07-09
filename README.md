# POC — Recherche client par id avec plusieurs adresses

> Objection en réunion : *« une recherche de client par id, on récupère la table
> en base, affichage côté front… un client peut avoir plusieurs adresses. S'il y a
> un souci de spec, comment je teste que l'adresse est bien là ? »*

Ce repo répond en **séparant les niveaux de test** — parce que « l'adresse est bien là »
veut dire plusieurs choses selon la couche — sur un vrai PostgreSQL (Testcontainers, jamais H2).

## L'architecture

Layering Spring classique : `controller → service → dao (repository) → base`.

- `Client` a une liste d'`Adresse`, relation **bidirectionnelle** (`Adresse` porte la FK
  `CLIENT_ID`), chargée en une requête via `@EntityGraph`.
- L'entité JPA **est** le modèle (domaine anémique assumé — voir plus bas).
- Le service porte la **transaction** (`@Transactional`) ; il dépend d'un contrat étroit
  `ClientDao`, ce qui le rend testable en unit sans base.
- Le contrôleur mappe l'entité vers un **DTO web** (`ClientView`) — le domaine n'est pas exposé.

## Les tests, par niveau

Chaque niveau répond à un **problème distinct** ; le mix dépend de l'app.

| Niveau | Test | Runner | Base | Ce qu'il prouve |
|---|---|---|---|---|
| **unit** | `ClientServiceTest` | surefire | ❌ in-memory | Le service (et le domaine qu'il traverse), sans Docker : `InMemoryClientDao` remplace la base. |
| **integration** | `ClientAdressesRepositoryIT` | failsafe | ✅ Postgres | Un client + 2 adresses survivent au round-trip DB. *(fusionnable avec le suivant)* |
| **integration** | `ClientAdressesSpecIT` | failsafe | ✅ Postgres | 0, 1 et N adresses. Force à trancher la spec : toutes les adresses, liste vide (jamais `null`). |
| **integration** | `ClientLazyLoadingIT` | failsafe | ✅ Postgres | Le **piège** : `findById` naïf → `LazyInitializationException`. `findByIdAvecAdresses` (`@EntityGraph`) → **1 requête** (pas de N+1). |
| **integration** | `ClientEcritureIT` | failsafe | ✅ Postgres | Perf écriture : sauver = **3 inserts, 0 update** (mapping bidirectionnel). |
| **e2e** | `ClientApiE2EIT` | failsafe | ✅ Postgres + HTTP | `GET /clients/{id}` → adresses dans le JSON servi au front. Plus le 404. |

Le fil rouge : on teste le **comportement observable** (base, DTO, JSON), pas l'implémentation
— donc pas de test 1:1 par classe : le domaine est exercé **à travers** le service.

## Le point « millions de lignes »

À 40M+ clients / encore plus d'adresses, ce qui tient la recherche :

1. **Recherche par `id`** = clé primaire indexée → scale toute seule.
2. **Index sur `ADRESSE.CLIENT_ID`** (`idx_adresse_client_id`) : Postgres n'indexe pas
   automatiquement une FK. Sans lui, retrouver les adresses = *seq scan* de toute la table
   ADRESSE — le vrai risque perf à l'échelle.
3. **Pas de N+1** : `@EntityGraph` charge tout en 1 requête (`ClientLazyLoadingIT`).
4. **Pas d'UPDATE superflu** à l'écriture (relation bidirectionnelle, `ClientEcritureIT`).

## Anémique ou riche ? Un curseur, pas un dogme

Le vrai message de ce repo. Séparer le **modèle de domaine** du **modèle de persistance**
(domaine pur + entité JPA + mapper) apporte de la **testabilité** et de la **pureté**… au prix
d'un mapper. Ce n'est ni tout blanc ni tout noir :

- **Ce n'est pas une décision globale** — c'est **par agrégat / par contexte**. Une même app
  peut mélanger du CRUD anémique (référentiel, config… ici `Client`) et un cœur métier riche
  (règles complexes). On investit la richesse là où la complexité le justifie (*core domain*
  vs *generic subdomain*).
- **Domaine complexe → riche = plus de testabilité, moins de coût de changement.** Le mapper a
  un coût initial, mais il s'amortit dès qu'il y a de la vraie logique à tester vite (unit, sans
  base) et à protéger par des invariants. Sur du CRUD, il ne s'amortit jamais.
- **Domaine anémique → un mapping entité↔domaine ne sert à rien** (on mappe une structure sur
  une structure identique). ⚠️ En revanche, le mapping **entité→DTO web** reste utile même en
  anémique : il découple le contrat d'API JSON du schéma de base (`ClientView`).

Ici, `Client` est **anémique** (un nom + une liste, aucun invariant) → l'impl couplée (entité =
modèle) est le bon choix. Le jour où une partie devient vraiment complexe, **cette partie-là**
gagnerait à passer en modèle riche découplé.

## Lancer

```bash
./run-tests.sh      # mvn verify dans un conteneur JDK 17 + Postgres via Testcontainers
```

Prérequis : Docker. Le POC démarre lui-même sa base. Aucun H2, aucune base à installer.

Stack : Spring Boot 3 / Hibernate 6 / Jakarta / Java 17.
