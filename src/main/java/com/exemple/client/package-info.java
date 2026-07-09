/**
 * <h2>Pourquoi cette archi (hexagonale + CQRS) plutôt que les deux alternatives « naturelles »</h2>
 *
 * Ce POC répond à « recherche client par id, le client a plusieurs adresses, comment je
 * teste que l'adresse est bien là ? ». Trois façons de structurer ça — on a retenu la 3ᵉ.
 * Ce fichier explique le pourquoi, avec les sources.
 *
 * <h3>Alternative A — « archi globale à la Spring » : une seule classe {@code @Entity} qui
 * sert à la fois de modèle métier ET de modèle de persistance</h3>
 *
 * C'est le POC d'origine : {@code Client} annoté {@code @Entity}, {@code @OneToMany} dans le
 * domaine, renvoyé jusqu'au contrôleur. Ça marche, mais un seul modèle sert deux maîtres :
 * <ul>
 *   <li><b>JPA impose ses contraintes au domaine</b> : constructeur no-arg, accès par
 *       réflexion, mutabilité — l'inverse d'un agrégat riche et immuable ;</li>
 *   <li><b>le lazy loading devient un piège</b> : accéder à {@code getAdresses()} hors
 *       session lève {@code LazyInitializationException}. « L'adresse n'est pas là » n'est
 *       plus un bug métier mais un accident d'infrastructure qui fuit dans tout le code ;</li>
 *   <li><b>le domaine n'est plus testable sans démarrer JPA</b>, et le schéma BDD se met à
 *       dicter les invariants métier.</li>
 * </ul>
 * Sources : Mihai Mogosanu, <i>Just Stop It! The Domain Model Is Not The Persistence Model</i>
 * (2012) ; Jimmy Nilsson, <i>Applying DDD and Patterns</i> (2006, <i>Persistence Ignorance</i>) ;
 * Martin Fowler, <i>PoEAA</i> (2002, <i>Data Mapper</i>) ; Udi Dahan, <i>Fear those Tiers</i>.
 *
 * <h3>Alternative B — charger « un objet Client » et « un objet Adresse » séparément</h3>
 *
 * Tentant : deux requêtes, deux objets, on recolle. Mais une adresse a exactement le même
 * cycle de vie que son client (créée/supprimée avec lui) et porte un invariant commun — c'est
 * <b>une seule unité de cohérence</b>, donc UN agrégat, pas deux. Les séparer coûte :
 * <ul>
 *   <li><b>N+1</b> : charger N clients puis leurs adresses une par une = N+1 requêtes
 *       (le fameux « 1 transaction pour chaque ») ;</li>
 *   <li><b>trous de cohérence</b> : deux chargements = deux instants, l'un peut avoir changé ;</li>
 *   <li>pas de frontière transactionnelle claire pour garantir l'invariant.</li>
 * </ul>
 * Un agrégat est « a cluster of associated objects that we treat as a unit for the purpose of
 * data changes ». Sources : Eric Evans, <i>DDD</i> (2003, p. 126) ; Vaughn Vernon,
 * <i>Effective Aggregate Design</i> I-III (2011).
 *
 * <h3>Ce qu'on a retenu — hexagonal (Data Mapper + Snapshot) + read side CQRS</h3>
 *
 * Deux modèles distincts, traduits à UN seul endroit (le mapper {@code SqlClientRepository}) :
 * <ul>
 *   <li><b>domaine pur</b> — {@code Client}/{@code Adresse}/{@code ClientId} sans aucune
 *       annotation JPA ({@code application.domain.models}) ;</li>
 *   <li><b>modèle de persistance</b> — {@code *JpaEntity} <i>package-private</i>, confinés à
 *       {@code adapters.secondary.persistence} : le compilateur INTERDIT qu'ils « parcourent
 *       l'app ».</li>
 * </ul>
 * Résultat, les deux problèmes de A/B disparaissent <i>par construction</i> :
 * <ul>
 *   <li>le mapper matérialise l'agrégat entier (adresses comprises) pendant que la session
 *       est ouverte → l'agrégat renvoyé n'a plus aucun proxy : {@code LazyInitializationException}
 *       est impossible ({@code ClientAgregatFetchIT}) ;</li>
 *   <li>l'agrégat se charge d'un bloc en UNE requête ({@code join fetch}) → pas de N+1 ;</li>
 *   <li>le domaine se teste sans JPA ; changer de mapping ne touche que l'adapter.</li>
 * </ul>
 *
 * <p><b>Le prix, et la question « et si je veux le client SANS ses adresses ? »</b> — Charger
 * toujours l'agrégat entier sacrifie un peu de performance en lecture (le « trilemme du DDD » :
 * pureté + complétude au prix de la perf). On paie ce prix UNIQUEMENT côté écriture. Pour lire
 * le client sans ses adresses, on ne mutile pas l'agrégat : on projette un <b>read model</b>
 * dédié ({@code GetClientSummary} → {@code ClientSummaryView}, un {@code SELECT id, nom} qui ne
 * touche même pas ADRESSE — {@code ClientReadModelIT}). Même concept « client », deux modèles
 * selon qu'on écrit ou qu'on lit. Sources : Vladimir Khorikov, <i>Domain model purity vs.
 * completeness — the DDD Trilemma</i> (2020) et <i>Domain model purity and lazy loading</i>
 * (2020) ; Greg Young / Martin Fowler, <i>CQRS</i> ; Vlad Mihalcea, <i>High-Performance Java
 * Persistence</i> (JPQL pour ce qu'on modifie, SQL pour les lectures/projections).
 */
package com.exemple.client;
