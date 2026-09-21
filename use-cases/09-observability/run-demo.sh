#!/usr/bin/env bash
#
# End-to-end demo for use case 09 (observing AI operations).
#
# Brings up the backing services (Qdrant + Prometheus + Grafana), makes sure Ollama has the models,
# builds the three applications and runs them from their built jars with `java` (no Maven forking, so the
# fixed ports 8080/8081/8082 always stick), then exercises each with curl and leaves everything running so
# you can open Grafana. Press Ctrl-C to stop the apps.
#
#   ./run-demo.sh              build (if needed), start everything, run the curls, hold until Ctrl-C
#   ./run-demo.sh --rebuild    force a rebuild of the app jars first
#   ./run-demo.sh --once       run the curls once, then stop the apps (keeps the containers up)
#   ./run-demo.sh stop         stop the apps and tear down the containers
# (--rebuild and --once can be combined.)
#
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/../.." && pwd)"
COMPOSE="$HERE/docker-compose.yml"
LOGDIR="${TMPDIR:-/tmp}/09-obs-demo"
mkdir -p "$LOGDIR"

CHAT_MODEL="qwen2.5:0.5b"
EMBED_MODEL="nomic-embed-text"
OLLAMA_URL="http://localhost:11434"

SPRING_AI_DIR="use-cases/09-observability/09-observability-spring-ai-boot"
LC4J_BOOT_DIR="use-cases/09-observability/09-observability-langchain4j-boot"
QUARKUS_DIR="use-cases/09-observability/09-observability-langchain4j-quarkus"

# name|port|health-path|metrics-path  (the three applications)
APPS=(
  "spring-ai|8080|/actuator/health|/actuator/prometheus"
  "langchain4j-boot|8081|/actuator/health|/actuator/prometheus"
  "langchain4j-quarkus|8082|/q/metrics|/q/metrics"
)

PIDS=()

blue()  { printf '\n\033[1;34m==> %s\033[0m\n' "$*"; }
green() { printf '\033[0;32m%s\033[0m\n' "$*"; }
red()   { printf '\033[0;31m%s\033[0m\n' "$*"; }

# Stop the apps this demo may have started (built jars, and a lingering `quarkus:dev`).
kill_apps() {
  pkill -f '09-observability-.*-exec\.jar' 2>/dev/null || true
  pkill -f '09-observability-langchain4j-quarkus/target/quarkus-app/quarkus-run\.jar' 2>/dev/null || true
  pkill -f '09-observability-langchain4j-quarkus-dev\.jar' 2>/dev/null || true
}

start_app() { # name jar
  local name="$1" jar="$2"
  java -jar "$jar" > "$LOGDIR/$name.log" 2>&1 &
  PIDS+=($!)
  green "  $name (pid $!) -> $LOGDIR/$name.log"
}

wait_ready() { # url
  for _ in $(seq 1 60); do
    [[ "$(curl -s -o /dev/null -w '%{http_code}' "$1" 2>/dev/null)" == "200" ]] && return 0
    sleep 2
  done
  return 1
}

# ---- parse flags -----------------------------------------------------------
STOP=0; REBUILD=0; ONCE=0
for arg in "$@"; do
  case "$arg" in
    stop)      STOP=1 ;;
    --rebuild) REBUILD=1 ;;
    --once)    ONCE=1 ;;
    *) red "Unknown argument: $arg"; exit 2 ;;
  esac
done

# ---- stop mode -------------------------------------------------------------
if [[ "$STOP" == 1 ]]; then
  blue "Stopping applications"; kill_apps
  blue "Tearing down containers"; docker compose -f "$COMPOSE" --profile with-ollama down
  green "Stopped."
  exit 0
fi

cd "$ROOT"

# ---- prerequisites ---------------------------------------------------------
for tool in docker java mvn curl; do
  command -v "$tool" >/dev/null 2>&1 || { red "Missing required tool: $tool"; exit 1; }
done

# Clear our own previous demo apps, then make sure the fixed ports are actually free.
kill_apps; sleep 1
busy=0
for app in "${APPS[@]}"; do
  IFS='|' read -r name port _ _ <<< "$app"
  if ss -ltn 2>/dev/null | grep -q ":$port "; then red "Port $port ($name) is in use by another process."; busy=1; fi
done
[[ "$busy" == 1 ]] && { red "Free the ports (or './run-demo.sh stop') and retry."; exit 1; }

# ---- backing services ------------------------------------------------------
blue "Starting Qdrant, Prometheus and Grafana"
docker compose -f "$COMPOSE" up -d qdrant prometheus grafana

# Ensure Ollama is reachable on 11434 (use a native one if present, otherwise start it in Compose).
if curl -sf "$OLLAMA_URL/api/tags" >/dev/null 2>&1; then
  green "Using the Ollama already listening on $OLLAMA_URL"
else
  blue "No local Ollama; starting it in Compose"
  docker compose -f "$COMPOSE" --profile with-ollama up -d ollama
  for _ in $(seq 1 30); do curl -sf "$OLLAMA_URL/api/tags" >/dev/null 2>&1 && break; sleep 2; done
fi

blue "Pulling models (skipped if already present)"
for m in "$CHAT_MODEL" "$EMBED_MODEL"; do
  green "  $m"
  curl -sf "$OLLAMA_URL/api/pull" -d "{\"name\":\"$m\"}" -o /dev/null || { red "  failed to pull $m"; exit 1; }
done

# ---- build the application jars --------------------------------------------
spring_jar()  { ls "$SPRING_AI_DIR"/target/*-exec.jar 2>/dev/null | head -1 || true; }
lc4j_jar()    { ls "$LC4J_BOOT_DIR"/target/*-exec.jar 2>/dev/null | head -1 || true; }
quarkus_jar() { echo "$QUARKUS_DIR/target/quarkus-app/quarkus-run.jar"; }

if [[ "$REBUILD" == 1 ]] || [[ -z "$(spring_jar)" ]] || [[ -z "$(lc4j_jar)" ]] || [[ ! -f "$(quarkus_jar)" ]]; then
  blue "Building the application jars (mvn package)"
  mvn -q -pl "$SPRING_AI_DIR,$LC4J_BOOT_DIR,$QUARKUS_DIR" -am package -DskipTests
else
  green "Reusing existing jars (pass --rebuild to force a rebuild)"
fi

# ---- run the apps with java ------------------------------------------------
blue "Starting the three applications"

start_app spring-ai            "$(spring_jar)"
start_app langchain4j-boot     "$(lc4j_jar)"
start_app langchain4j-quarkus  "$(quarkus_jar)"

blue "Waiting for the applications to become ready"
for app in "${APPS[@]}"; do
  IFS='|' read -r name port health _ <<< "$app"
  if wait_ready "http://localhost:$port$health"; then green "  $name is up on :$port"; else red "  $name did not start (see $LOGDIR/$name.log)"; fi
done

# ---- exercise each app with curl -------------------------------------------
for app in "${APPS[@]}"; do
  IFS='|' read -r name port _ metrics <<< "$app"
  blue "$name (:$port)"
  echo   "  POST /ask 'Where is Aurora Labs based?'"
  printf '    -> '; curl -s "http://localhost:$port/ask" -H 'Content-Type: application/json' \
    -d '{"question":"Where is Aurora Labs based? Answer with the city only."}'; echo
  echo   "  GET  /vectorstore/count"
  printf '    -> '; curl -s "http://localhost:$port/vectorstore/count"; echo
  echo   "  GET  $metrics  (token-usage metric)"
  printf '    -> '; curl -s "http://localhost:$port$metrics" | grep -m1 'gen_ai_client_token_usage' | cut -c1-100 || echo "(metric not present yet)"
done

blue "Observability endpoints"
green "  Prometheus : http://localhost:9090"
green "  Grafana    : http://localhost:3000  (anonymous, 'GenAI operations' dashboard)"

if [[ "$ONCE" == 1 ]]; then
  blue "Stopping the applications (--once); containers left up"
  kill_apps
  exit 0
fi

blue "Applications are running. Make more calls, then open Grafana. Press Ctrl-C to stop the apps."
trap 'echo; blue "Stopping applications"; kill_apps; exit 0' INT TERM
wait
