#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

JAVA_17_HOME="/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
export JAVA_HOME="${JAVA_HOME:-$JAVA_17_HOME}"
export PATH="$JAVA_HOME/bin:$PATH"
APP_PORT="${SERVER_PORT:-8080}"

if ! command -v java >/dev/null 2>&1; then
  echo "Java 17 is required. Set JAVA_HOME to a Java 17 installation."
  exit 1
fi

java_version="$(java -version 2>&1 | head -n 1)"
if ! echo "$java_version" | grep -E -q '"17| 17'; then
  echo "Java 17 is required. Set JAVA_HOME to a Java 17 installation."
  exit 1
fi

if command -v lsof >/dev/null 2>&1; then
  existing_pid="$(lsof -t -iTCP:"$APP_PORT" -sTCP:LISTEN 2>/dev/null | head -n 1 || true)"
  if [[ -n "$existing_pid" ]]; then
    existing_cmd="$(ps -p "$existing_pid" -o command= 2>/dev/null || true)"
    if [[ "$existing_cmd" == *"$ROOT_DIR"* && "$existing_cmd" == *"RestAppGeneratorApplication"* ]]; then
      echo "Stopping existing local RestAppGeneratorApplication on port $APP_PORT (PID $existing_pid)"
      kill "$existing_pid"
      for _ in {1..20}; do
        if ! lsof -t -iTCP:"$APP_PORT" -sTCP:LISTEN >/dev/null 2>&1; then
          break
        fi
        sleep 1
      done
    fi
  fi
fi

mvn -U -f parent/pom.xml -DskipTests \
  -pl com.src:common,com.src:communication,com.src:rbac,com.src:auth,com.src:state-machine,com.src:swagger \
  -am clean install

ASSET_SOURCE_DIR="$ROOT_DIR/src/ux/src/assets"
ASSET_TARGET_ROOT="$ROOT_DIR/target/classes/static"
rm -rf "$ASSET_TARGET_ROOT"
while IFS= read -r asset_dir; do
  rel_path="${asset_dir#"$ASSET_SOURCE_DIR"/}"
  if [[ "$asset_dir" == "$ASSET_SOURCE_DIR" ]]; then
    mkdir -p "$ASSET_TARGET_ROOT/assets"
  else
    mkdir -p "$ASSET_TARGET_ROOT/assets/$rel_path"
  fi
done < <(find "$ASSET_SOURCE_DIR" -type d | sort)

mvn -f "$ROOT_DIR/pom.xml" -DskipTests clean process-resources compile spring-boot:run \
  -Dspring-boot.run.jvmArguments='-Dspring.devtools.restart.enabled=false' \
  -Dspring-boot.run.arguments="--server.port=$APP_PORT"
