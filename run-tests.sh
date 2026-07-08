#!/usr/bin/env bash
#
# Lance `mvn verify` du POC dans un conteneur JDK 17 (Hibernate 5.6 n'est pas
# fiable sur des JDK trop récents).
#
# Le POC démarre LUI-MÊME son PostgreSQL via Testcontainers — exactement comme
# dans un vrai `mvn verify`. Pour ça, on monte le socket Docker dans le conteneur
# de build (« sibling containers ») et on indique à Testcontainers comment
# joindre les ports publiés (host.docker.internal sur Docker Desktop).
#
set -euo pipefail
cd "$(dirname "$0")"

docker run --rm \
  -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal \
  -e DOCKER_HOST=unix:///var/run/docker.sock \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v "$PWD":/app \
  -v poc-client-adresses-m2:/root/.m2 \
  -w /app \
  maven:3.9-eclipse-temurin-17 \
  mvn -B verify
